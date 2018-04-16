package co.axelrod.foodqueue.ui;

import co.axelrod.foodqueue.config.token.TelegramTokenStorage;
import co.axelrod.foodqueue.config.token.TelegramTokenStorageImpl;
import co.axelrod.foodqueue.logic.QueueController;
import co.axelrod.foodqueue.logic.auth.UserAuthManager;
import co.axelrod.foodqueue.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

import static co.axelrod.foodqueue.util.TelegramUtil.prepareMessage;

/**
 * Created by Vadim Axelrod (vadim@axelrod.co) on 27.02.2018.
 */
@Controller
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    QueueController queueController;

    @Autowired
    UserAuthManager userAuthManager;

    TelegramTokenStorage telegramTokenStorage = new TelegramTokenStorageImpl();

    private static final String PHOTO_URL = "https://cdn-cf-static.catery.ru/storage/menu/item/3/7/5/37547/preview_preview_image-cdjfMYAbH6olsP1D6jnhTJrWGzy5g-peeQ.jpg";

    private static final String EMOJI = "\uD83C\uDF68";

    @Value("${server.host}")
    private String host;

    @Value("${server.port}")
    private String port;

    private InlineKeyboardMarkup getLoginKeyboard(Long chatId) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        rowInline.add(new InlineKeyboardButton()
                .setText("Авторизоваться")
                .setUrl("http://" + host + ":" + port + "/telegramAuth?telegramChatId=" + chatId));
        rowsInline.add(rowInline);
        keyboardMarkup.setKeyboard(rowsInline);

        return keyboardMarkup;
    }

    private InlineKeyboardMarkup getInqueueKeyboard() {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton()
                .setText("Занять очередь")
                .setCallbackData("enqueueAgain"));
        rowsInline.add(rowInline);
        keyboardMarkup.setKeyboard(rowsInline);

        return keyboardMarkup;
    }

    private InlineKeyboardMarkup getInqueueAgainKeyboard() {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton()
                .setText("Занять очередь заново")
                .setCallbackData("enqueueAgain"));
        rowsInline.add(rowInline);
        keyboardMarkup.setKeyboard(rowsInline);

        return keyboardMarkup;
    }

    private ReplyKeyboardMarkup getWhereIsMyFoodKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add("Где моё " + EMOJI + ", чувак?");
        keyboard.add(keyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboard);
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if ((update.hasMessage() && update.getMessage().hasText())) {
                Long telegramChatId = update.getMessage().getChatId();
                log.debug("New request from Telegram bot with id: " + telegramChatId);

                String login = update.getMessage().getText();
                User user = userAuthManager.getOrUpdateUserByTelegram(login, telegramChatId);
                if (user == null) {
                    sendHelloMessage(telegramChatId);
                } else {
                    sendWaitingMessage(telegramChatId, user);
                }
            } else if (update.hasCallbackQuery()) {
                Long telegramChatId = update.getCallbackQuery().getMessage().getChatId();
                log.debug("New callback query from Telegram bot with id: " + telegramChatId);

                if (update.getCallbackQuery().getData().equals("enqueueAgain")) {
                    String login = update.getCallbackQuery().getMessage().getText();
                    User user = userAuthManager.getOrUpdateUserByTelegram(login, telegramChatId);

                    queueController.dequeue(user);
                    queueController.enqueue(user);

                    AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
                    answerCallbackQuery.setCallbackQueryId(update.getCallbackQuery().getId());
                    answerCallbackQuery.setText("Очередь занята");

                    execute(answerCallbackQuery);
                    sendWaitingMessage(telegramChatId, user);
                }
            }
        } catch (TelegramApiException ex) {
            log.error("Telegram Bot is unable to process update!", ex);
        }
    }

    public void sendHelloMessage(Long chatId) throws TelegramApiException {
        execute(prepareMessage(chatId, "Где " + EMOJI + ", чувак?"));
        execute(prepareMessage(chatId, "Бот займет очередь для тебя и пришлет уведомление, когда пора идти за " + EMOJI + "!"));
        execute(prepareMessage(chatId, "Авторизуйся через свою доменную учетную запись для начала работы" +
                " (важно, для входа необходимо быть в локальной сети компании - рабочий ПК, VPN или Wi-Fi офиса):", getLoginKeyboard(chatId)));
    }

    public void sendJustAuthorizedMessage(Long chatId) throws TelegramApiException {
        execute(prepareMessage(chatId, "Авторизация успешна!", getInqueueKeyboard()));
    }

    public void sendWaitingMessage(Long chatId, User user) throws TelegramApiException {
        sendPhoto(new SendPhoto()
                .setChatId(chatId)
                .setPhoto(PHOTO_URL)
                .setCaption("За пару минут до готовности бот пришлет уведомление."));
        switch (queueController.getKitchenStatus()) {
            case PREPARING: {
                execute(prepareMessage(chatId, "Твой номер в очереди: " + user.getQueueNumber()));
                execute(prepareMessage(chatId, "Кухня готовится."));
            } break;
            case READY: {
                execute(prepareMessage(chatId, "Твой номер в очереди: " + user.getQueueNumber()));
                execute(prepareMessage(chatId, "Текущий номер: " + queueController.getCurrentQueueNumber(), getWhereIsMyFoodKeyboard()));
            } break;
            case CLOSED: {
                execute(prepareMessage(chatId, "Кухня закрылась."));
            } break;
        }
    }

    public void sendGotItMessage(Long chatId, User user) throws TelegramApiException {
        execute(prepareMessage(chatId, EMOJI + "почти готов!"
                + "\nТвой номер: " + user.getQueueNumber()
                + "\nТекущий номер: " + queueController.getCurrentQueueNumber()));
        execute(prepareMessage(chatId, "Хочется добавки?", getInqueueAgainKeyboard()));
    }

    @Override
    public String getBotUsername() {
        return "Где моё " + EMOJI + ", чувак?";
    }

    @Override
    public String getBotToken() {
        return telegramTokenStorage.getBotToken();
    }
}
