package co.axelrod.foodqueue.logic;

import co.axelrod.foodqueue.logic.enums.KitchenStatus;
import co.axelrod.foodqueue.model.Queue;
import co.axelrod.foodqueue.model.User;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.mongodb.client.model.Filters.eq;

@Component
public class QueueControllerImpl implements QueueController {
    private final MongoCollection<Queue> queueCollection;

    private final MongoCollection<User> userCollection;

    @Autowired
    public QueueControllerImpl(MongoCollection<Queue> queueCollection, MongoCollection<User> userCollection) {
        this.queueCollection = queueCollection;
        this.userCollection = userCollection;
    }

    private Queue getOrCreateQueue() {
        Queue queue = queueCollection.find().first();
        if(queue == null) {
            queue = new Queue(new ObjectId(), 0, 0, KitchenStatus.PREPARING);
            queueCollection.insertOne(queue);
        }
        return queue;
    }

    public final Integer getCurrentQueueNumber() {
        return getOrCreateQueue().currentNumber;
    }

    public final Integer incrementCurrentQueueNumber() {
        Queue queue = getOrCreateQueue();
        queue.currentNumber = queue.currentNumber + 1;
        queueCollection.replaceOne(eq("_id", queue.id), queue, new UpdateOptions().upsert(true));
        return queue.currentNumber;
    }

    public final KitchenStatus getKitchenStatus() {
        Queue queue = getOrCreateQueue();
        return queue.kitchenStatus;
    }

    public final void setKitchenStatus(KitchenStatus status) {
        Queue queue = queueCollection.find().first();
        queue.kitchenStatus = status;
        queueCollection.replaceOne(eq("_id", queue.id), queue, new UpdateOptions().upsert(true));
    }

    private Integer incrementQueueNumber() {
        Queue queue = getOrCreateQueue();
        queue.queueNumber = queue.queueNumber + 1;
        queueCollection.replaceOne(eq("_id", queue.id), queue, new UpdateOptions().upsert(true));
        return queue.queueNumber;
    }

    public final void enqueue(User user) {
        user.queueNumber = incrementQueueNumber();
        userCollection.replaceOne(eq("_id", user.id), user, new UpdateOptions().upsert(true));
    }

    public final void dequeue(User user) {
        userCollection.deleteOne(eq("_id", user.id));
        user.notificationSent = null;
    }
}