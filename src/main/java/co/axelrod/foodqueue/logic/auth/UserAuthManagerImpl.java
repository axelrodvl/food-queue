package co.axelrod.foodqueue.logic.auth;

import co.axelrod.foodqueue.model.User;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.stereotype.Component;

import static com.mongodb.client.model.Filters.eq;

/**
 * Created by Vadim Axelrod (vadim@axelrod.co) on 27.02.2018.
 */
@Component
public class UserAuthManagerImpl implements UserAuthManager {
    private final MongoCollection<User> userCollection;

    @Autowired
    public UserAuthManagerImpl(MongoCollection<User> userCollection) {
        this.userCollection = userCollection;
    }

    @Override
    public User createUserByTelegram(Authentication auth, Long telegramChatId) {
        LdapUserDetails ldapUserDetails = (LdapUserDetails) auth.getPrincipal();
        String login = ldapUserDetails.getUsername();

        User user = userCollection.find(eq("login", login)).first();

        if(user == null) {
            user = new User(new ObjectId(), login, telegramChatId, false, null, null);
            userCollection.insertOne(user);
        }

        return user;
    }

    @Override
    public User getOrCreateUser(Authentication auth) {
        LdapUserDetails ldapUserDetails = (LdapUserDetails) auth.getPrincipal();
        String login = ldapUserDetails.getUsername();

        User user = userCollection.find(eq("login", login)).first();

        if(user == null) {
            user = new User(new ObjectId(), login, null, null, null, null);
            userCollection.insertOne(user);
        }

        return user;
    }

    @Override
    public User getOrUpdateUserByTelegram(String login, Long telegramChatId) {
        User user = userCollection.find(eq("telegramChatId", telegramChatId)).first();
        if(user == null) {
            user = userCollection.find(eq("login", login.toLowerCase())).first();
            if(user != null) {
                user.setTelegramChatId(telegramChatId);
                userCollection.replaceOne(eq("_id", user.getId()), user, new UpdateOptions().upsert(true));
            }
        }
        return user;
    }
}
