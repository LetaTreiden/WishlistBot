package ru.treiden.Wishlist;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) {
        try {
            // Инициализируем API
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

            // Регистрируем бота
            botsApi.registerBot(new WishListBot());

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

