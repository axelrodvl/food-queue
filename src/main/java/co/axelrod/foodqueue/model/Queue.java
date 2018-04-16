package co.axelrod.foodqueue.model;

import co.axelrod.foodqueue.logic.enums.KitchenStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

/**
 * Created by Vadim Axelrod (vadim@axelrod.co) on 27.02.2018.
 */
@NoArgsConstructor
@AllArgsConstructor
@BsonDiscriminator
public class Queue {
    @BsonId
    public ObjectId id;

    public Integer currentNumber;
    public Integer queueNumber;
    public KitchenStatus kitchenStatus;
}
