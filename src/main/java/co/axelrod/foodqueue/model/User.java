package co.axelrod.foodqueue.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

/**
 * Created by Vadim Axelrod (vadim@axelrod.co) on 27.02.2018.
 */
@NoArgsConstructor
@AllArgsConstructor
@BsonDiscriminator
public class User {
    @BsonId
    public ObjectId id;

    public String login;
    public Long telegramChatId;
    public Boolean processedByBot;
    public Integer queueNumber;
    public Boolean notificationSent;
}