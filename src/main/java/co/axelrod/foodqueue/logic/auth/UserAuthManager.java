package co.axelrod.foodqueue.logic.auth;

import co.axelrod.foodqueue.model.User;
import org.springframework.security.core.Authentication;

/**
 * Created by Vadim Axelrod (vadim@axelrod.co) on 16.04.2018.
 */
public interface UserAuthManager {
    User createUserByTelegram(Authentication auth, Long telegramChatId);

    User getOrCreateUser(Authentication auth);

    User getOrUpdateUserByTelegram(String login, Long telegramChatId);
}
