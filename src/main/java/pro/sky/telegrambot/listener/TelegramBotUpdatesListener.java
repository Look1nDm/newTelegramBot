package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repositories.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
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
            logger.info("Processing update: {}", update);
            switch (update.message().text()) {
                case ("/start"):
                    SendMessage massage = new SendMessage(update.message().chat().id(), "Привет пользователь " +
                            update.message().chat().firstName() + ". Добавим кого-нибудь в список поздравлений?!" + " Список доступных команд:\n" +
                            "/да\n" +
                            "/добавим\n");
                    SendResponse response = telegramBot.execute(massage);
                    break;
                case ("/добавим"):
                case ("/да"):
                    SendMessage massageDa = new SendMessage(update.message().chat().id(), "Отлично, введи Имя, Фамилию и " +
                            "дату рождения.");
                    SendResponse responseDa = telegramBot.execute(massageDa);
                    notificationTaskRepository.save(treatmentMessage(update.message().text(), update));
                    break;
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
    private static NotificationTask treatmentMessage (String message, Update update){
        // паттерн для вычленния даты из сообщения
        Pattern patternDate = Pattern.compile("[0-9\\.\\s:]+");
        // паттерн для вычленения текстовой части в сообщении
        Pattern patternString = Pattern.compile("[А-Я|а-я]+");
        NotificationTask notificationTaskCopyInDB = new NotificationTask();
        notificationTaskCopyInDB.setId(update.message().chat().id());
        if (update.message().text().contains("[0-9\\.\\s:]+")) {
            Matcher matcherDate = patternDate.matcher(update.message().text().trim());
            LocalDateTime dateTime = LocalDateTime.parse(matcherDate.group(), DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            notificationTaskCopyInDB.setDatetime(dateTime);
        }

        if (message.contains("[А-Я|а-я]+")) {
            Matcher matcherString = patternString.matcher(update.message().text().trim());
            String text = matcherString.group();
            notificationTaskCopyInDB.setMessage(text);
        }
        return notificationTaskCopyInDB;
    }
}