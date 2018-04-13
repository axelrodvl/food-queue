package co.axelrod.foodqueue.util;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;

/**
 * Created by Vadim Axelrod (vadim@axelrod.co) on 04.03.2018.
 */
public class TelegramUtil {
    private TelegramUtil() {
        // Utility class
    }

    public static SendMessage prepareMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        return message;
    }

    public static SendMessage prepareMessage(Long chatId, String text, ReplyKeyboard keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setReplyMarkup(keyboard);
        return message;
    }
}
