package pro.sky.telegrambot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.model.NotificationTask;

@Repository
public interface NotificationTaskRepository extends JpaRepository<NotificationTask,Integer> {
    @Query(value = "SELECT * FROM notification_task WHERE to_char(datetime,'yyyy.MM.dd') LIKE '%?%'", nativeQuery = true)// запрос в БД
    NotificationTask findContains(String str);
}
//SELECT * FROM notification_task WHERE notification_task.datetime = ?1