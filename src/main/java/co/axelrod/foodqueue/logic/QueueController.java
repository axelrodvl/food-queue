package co.axelrod.foodqueue.logic;

import co.axelrod.foodqueue.logic.enums.KitchenStatus;
import co.axelrod.foodqueue.model.User;

public interface QueueController {
    Integer getCurrentQueueNumber();
    Integer incrementCurrentQueueNumber();

    KitchenStatus getKitchenStatus();
    void setKitchenStatus(KitchenStatus status);

    void enqueue(User user);
    void dequeue(User user);
}
