package co.axelrod.foodqueue.logic;

import co.axelrod.foodqueue.logic.enums.KitchenStatus;
import co.axelrod.foodqueue.model.User;
import co.axelrod.foodqueue.ui.TelegramBot;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.UpdateOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import static com.mongodb.client.model.Filters.*;

/**
 * Created by Vadim Axelrod (vadim@axelrod.co) on 27.02.2018.
 */
@Service
@Slf4j
public class TelegramNotifier {
    private final QueueController queueController;

    private final MongoCollection<User> userCollection;

    private final TelegramBot telegramBot;

    @Autowired
    public TelegramNotifier(QueueController queueController, MongoCollection<User> userCollection, TelegramBot telegramBot) {
        this.queueController = queueController;
        this.userCollection = userCollection;
        this.telegramBot = telegramBot;
    }

    @Scheduled(fixedRate=3000)
    public final void processAuthorizedUsers() {
        log.trace("Running proccesor for just authorized users");
        try (MongoCursor<User> usersToStartBot = userCollection
                .find(and(
                        exists("telegramChatId"),
                        eq("processedByBot", false)
                        )
                ).iterator()) {
            while (usersToStartBot.hasNext()) {
                User user = usersToStartBot.next();
                try {
                    telegramBot.sendJustAuthorizedMessage(user.getTelegramChatId());
                    user.setProcessedByBot(true);
                    userCollection.replaceOne(eq("_id", user.getId()), user, new UpdateOptions().upsert(true));
                    log.debug("User " + user.getLogin() + " authorized and processed through Telegram");
                } catch (TelegramApiException e) {
                    log.error("Unable to authorize and process user " + user.getLogin() + " through Telegram");
                    e.printStackTrace();
                }
            }
        }
    }

    @Scheduled(fixedRate=10000)
    public final void sendNotifications() {
        if(queueController.getKitchenStatus().equals(KitchenStatus.READY)) {
            log.trace("Running notificator");
            Integer currentQueueNumber = queueController.getCurrentQueueNumber();

            try (MongoCursor<User> usersToNotify = userCollection
                    .find(and(
                            gte("queueNumber", currentQueueNumber),
                            lt("queueNumber", currentQueueNumber + 3),
                            exists("telegramChatId"),
                            exists("notificationSent", false)
                            )
                    ).iterator()) {
                while (usersToNotify.hasNext()) {
                    User user = usersToNotify.next();
                    try {
                        telegramBot.sendGotItMessage(user.getTelegramChatId(), user);
                        user.setNotificationSent(true);
                        userCollection.replaceOne(eq("_id", user.getId()), user, new UpdateOptions().upsert(true));
                        log.debug("User " + user.getLogin() + " notified through Telegram");
                    } catch (TelegramApiException e) {
                        log.error("Unable to notify user " + user.getLogin() + " through Telegram", e);
                    }
                }
            }
        } else {
            log.debug("Kitchen is not ready, skipping");
        }
    }
}