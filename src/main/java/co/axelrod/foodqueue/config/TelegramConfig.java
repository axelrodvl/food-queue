package co.axelrod.foodqueue.config;

import co.axelrod.foodqueue.ui.TelegramBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;

@Configuration
public class TelegramConfig {
    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot();
    }

    @PostConstruct
    public void registerTelegramBot() {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(telegramBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
