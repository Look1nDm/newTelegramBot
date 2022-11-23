package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.*;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.exceptions.InvalidDataException;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repositories.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private NotificationTask notificationTask;
    private NotificationTaskRepository notificationTaskRepository;
    private TelegramBot telegramBot;

    public TelegramBotUpdatesListener(NotificationTask notificationTask, NotificationTaskRepository notificationTaskRepository, TelegramBot telegramBot) {
        this.notificationTask = notificationTask;
        this.notificationTaskRepository = notificationTaskRepository;
        this.telegramBot = telegramBot;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            Message message = update.message();
            try {
                if (update.message() == null) {
                    message = update.editedMessage();
                    if (message == null) {
                        return;
                    }
                }
                String text = getText(message.text());
                logger.info("Processing update: {}", update);
                switch (text) {
                    case ("/start"):
                        SendMessage massage = new SendMessage(update.message().chat().id(), "Привет пользователь " +
                                update.message().chat().firstName() + ". Добавим кого-нибудь в список поздравлений?!" + " Список доступных команд:\n" +
                                "/yes\n" +
                                "/no\n");
                        telegramBot.execute(massage);
                        break;
                    case ("/yes"):
                        SendMessage messageDa = new SendMessage(update.message().chat().id(), "Отлично,используй команду /add и введи Имя, Фамилию и " +
                                "дату рождения в формате 01.20.2022.");
                        telegramBot.execute(messageDa);
                        break;
                    case ("/no"):
                        SendMessage messageNo = new SendMessage(update.message().chat().id(), "Ну нет, значит нет, заходи если созреешь.");
                        telegramBot.execute(messageNo);
                        break;
                    case ("/add"):
                        notificationTaskRepository.save(treatmentMessage(message));
                        telegramBot.execute(new SendMessage(message.chat().id(), "Ну красава, братка добавлен в списочек"));
                        break;
                    default:
                        telegramBot.execute(new SendMessage(message.chat().id(),
                                "Rfr nt,t gjyznyj? Вот и мне не понятно, пробуй еще."));
                }
            } catch (Exception e) {
                telegramBot.execute(new SendMessage(message.chat().id(), "Кажется, что введены какие то кривые данные, попробуй сначала"));
            }
        });

        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private String getText(String text) {
        if (text.startsWith("/")) {
            if (!text.contains(" ")) {
                return text;
            }
            return text.substring(0, text.indexOf(" "));
        }
        return Strings.EMPTY;
    }

    private static NotificationTask treatmentMessage(Message message) {
        // паттерн для вычленния даты из сообщения
        Pattern patternDate = Pattern.compile("\\d{2}.\\d{2}.\\d{4}");
        // паттерн для вычленения текстовой части в сообщении
        Pattern patternString = Pattern.compile("([А-Я|а-я])+\\s([А-Я|а-я])+");
        NotificationTask notificationTaskCopyInDB = new NotificationTask();
        notificationTaskCopyInDB.setId(message.chat().id());

        Matcher matcherDate = patternDate.matcher(message.text().trim());
        Matcher matcherString = patternString.matcher(message.text().trim());
        if (!matcherDate.find() || !matcherString.find()) {
            throw new InvalidDataException();
        }
        LocalDate dateTime = LocalDate.parse(matcherDate.group(), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        notificationTaskCopyInDB.setDatetime(dateTime);

        String text = matcherString.group();
        notificationTaskCopyInDB.setMessage(text);

        return notificationTaskCopyInDB;
    }
}