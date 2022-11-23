package pro.sky.telegrambot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.model.NotificationTask;
@Repository
public interface NotificationTaskRepository extends JpaRepository<NotificationTask,Integer> {

}
