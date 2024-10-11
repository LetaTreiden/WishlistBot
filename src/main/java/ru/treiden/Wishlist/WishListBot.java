package ru.treiden.Wishlist;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.logging.Logger;

public class WishListBot extends TelegramLongPollingBot {
    private static final long ADMIN_ID = 12345678L; //замените на свой номер чата
    private static final Logger logger = Logger.getLogger(WishListBot.class.getName());



    private List<Gift> availableGifts = new ArrayList<>(); //список всех доступных подарков
    private Map<Long, List<Gift>> userGifts = new HashMap<>(); //список подарков для каждого пользователя
    private Map<Long, String> users = new HashMap<>(); //список пользователей
    private int giftCounter = 0; //счетчик подарков


    @Override
    public void onUpdateReceived(Update update) { //обработка сообщений от пользователя
        if (update.hasMessage() && update.getMessage().hasText() || update.hasCallbackQuery()) {
            String messageText = null;
            long chatId = 0;
            if (update.hasMessage()) {
                messageText = update.getMessage().getText();
                chatId = update.getMessage().getChatId();
                if (!update.getMessage().getFrom().getFirstName().isEmpty() &&
                        update.getMessage().getFrom().getFirstName() != null) {
                    users.put(chatId, update.getMessage().getFrom().getFirstName());
                }
                logger.info("Message");
            } else if (update.hasCallbackQuery()){
                messageText = update.getCallbackQuery().getData();
                chatId = update.getCallbackQuery().getMessage().getChatId();
                if (!update.getCallbackQuery().getFrom().getFirstName().isEmpty() &&
                        update.getCallbackQuery().getFrom().getFirstName() != null) {
                    users.put(chatId, update.getCallbackQuery().getFrom().getFirstName());
                }
                logger.info("Button");
                logger.info(messageText);
            }

            switch (messageText) {
                case "/start":
                    sendMessage(new SendMessage(String.valueOf(chatId), "Приветственный текст"));

                    sendMenu(chatId);
                    break;
                case "/mygifts":
                    showMyGifts(chatId);
                    break;
                case "/gifts":
                    showGiftCategories(chatId);
                    break;
                case "category_SMALL":
                    showGiftsInCategory(chatId, "Мелочи");
                    break;
                case "category_ONE_K":
                    showGiftsInCategory(chatId, "В районе 1к");
                    break;
                case "category_EXPENSIVE":
                    showGiftsInCategory(chatId, "Дорого");
                    break;
                case "category_SUPER_EXPENSIVE":
                    showGiftsInCategory(chatId, "Капец дорого");
                    break;


                default:
                    if (messageText.startsWith("choose")) {
                        try {
                            chooseGift(chatId, messageText);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    break;
            }
            if (chatId == ADMIN_ID) {
                if (messageText.startsWith("/send")) {
                    sendAdminMessage(messageText.substring("/send".length()).trim());
                } else if (messageText.startsWith("/add")) {
                    addGift(messageText);
                } else if (messageText.startsWith("/delete")) {
                    deleteGift(messageText.substring("/delete".length()).trim());
                }
            }
        }
    }


    // печать первого меню
    private void sendMenu(long chatId) {

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите команду:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Посмотреть вишлист");
        button.setCallbackData("/gifts");
        row1.add(button);
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Выбранные подарки");
        button1.setCallbackData("/mygifts");
        row2.add(button1);

        buttons.add(row1);
        buttons.add(row2);
        markup.setKeyboard(buttons);

        message.setReplyMarkup(markup);
        sendMessage(message);
    }


    //печать категорий подарков
    private void showGiftCategories(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите категорию:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        for (Category category : Category.values()) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(category.getDisplayName());
            button.setCallbackData("category_" + category.name());
            buttons.add(List.of(button));
        }

        markup.setKeyboard(buttons);
        message.setReplyMarkup(markup);
        sendMessage(message);
        logger.info("Действие пользователя");
    }

    //печать подарков в категории
    private void showGiftsInCategory(long chatId, String categoryName) {
        Category category;
        switch (categoryName) {
            case "Мелочи":
                category = Category.SMALL;
                break;
            case "В районе 1к":
                category = Category.ONE_K;
                break;
            case "Дорого":
                category = Category.EXPENSIVE;
                break;
            default:
                category = Category.SUPER_EXPENSIVE;
                break;
        }
        List<Gift> giftsInCategory = new ArrayList<>();

        for (Gift gift : availableGifts) {
            if (gift.getCategory() == category) {
                giftsInCategory.add(gift);
            }
        }
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        if (giftsInCategory.isEmpty()) {
            sendMessage(new SendMessage(String.valueOf(chatId), "В этой категории пока нет подарков."));
        } else {
            StringBuilder giftList = new StringBuilder("Подарки в категории " + category.getDisplayName() + ":\n");
            for (Gift gift : giftsInCategory) {
                giftList.append("⭐️ ").append(gift.getId()).append(" ").append(gift.getName()).append(":\n")
                        .append(gift.getDescription()).append("\n\n");
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText("Выбрать " + gift.getName());
                button.setCallbackData("choose " + gift.getId());

                buttons.add(List.of(button));
            }
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            markup.setKeyboard(buttons);
            message.setReplyMarkup(markup);
            message.setText(giftList.toString());

            sendMessage(message);
        }
    }
    //выбор подарка пользователем

    private void chooseGift(long chatId, String messageText) throws Exception {
        try {
            int giftId = Integer.parseInt(messageText.split(" ")[1]);

            Gift chosenGift = null;
            for (Gift gift : availableGifts) {
                if (gift.getId() == giftId) {
                    chosenGift = gift;
                    break;
                }
            }

            if (chosenGift != null) {
                availableGifts.remove(chosenGift);
                userGifts.computeIfAbsent(chatId, k -> new ArrayList<>()).add(chosenGift);
                sendMessage(new SendMessage(String.valueOf(chatId), "Вы выбрали : " + chosenGift.getName()));
            } else {
                sendMessage(new SendMessage(String.valueOf(chatId), "Подарок не найден."));
            }
        } catch (NumberFormatException e) {
            sendMessage(new SendMessage(String.valueOf(chatId), "Некорректный мномер подарка."));
        } catch (Exception e) {
            sendMessage(new SendMessage(String.valueOf(chatId), "Кажется, этот подарок кто-то уже выбрал. Давай посмотрим ещё"));
            sendMenu(chatId);
        }
    }
    //просмотр подарокв для пользователя

    private void showMyGifts(long chatId) {
        List<Gift> myGifts = userGifts.getOrDefault(chatId, new ArrayList<>());
        if (myGifts.isEmpty()) {
            sendMessage(new SendMessage(String.valueOf(chatId), "Вы пока не выбрали подарок."));
        } else {
            StringBuilder giftList = new StringBuilder("Ваши подарки:\n");
            for (Gift gift : myGifts) {
                giftList.append("⭐️ ").append(gift.getName()).append(":\n").append(gift.getDescription()).append("\n\n");
            }
            sendMessage(new SendMessage(String.valueOf(chatId), giftList.toString()));
        }
    }
    //отправка сообщения от имени администратора

    private void sendAdminMessage(String message) {
        for (long userId : users.keySet()) {
            sendMessage(new SendMessage(String.valueOf(userId), "Дамы и господа, беспокоит администратор : " + message));
        }
    }

    //добавление подарка админом
    private void addGift(String messageText) {
        // Разделяем сообщение по знаку "!"
        String[] parts = messageText.split("!");

        if (parts.length != 4) {
            sendMessage(new SendMessage(String.valueOf(ADMIN_ID), "Используйте формат: " +
                    "команда!название подарка!описание!категория"));
            return;
        }

        // Извлекаем данные из сообщения
        String name = parts[1].trim();
        String description = parts[2].trim();
        String categoryStr = parts[3].trim();

        // Определяем категорию подарка
        Category category;
        try {
            category = Category.valueOf(categoryStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            sendMessage(new SendMessage(String.valueOf(ADMIN_ID), "Некорректная категория. " +
                    "Доступные категории: SMALL, ONE_K, EXPENSIVE, SUPER_EXPENSIVE."));
            return;
        }

        // Создаём и добавляем подарок с новым ID
        Gift newGift = new Gift(++giftCounter, name, description, category);
        availableGifts.add(newGift);

        sendMessage(new SendMessage(String.valueOf(ADMIN_ID), "Подарок добавлен: " + newGift.getName() + " в категории " + category.getDisplayName()));
    }

    //удалить подарок

    private void deleteGift(String giftName) {
        availableGifts.removeIf(gift -> gift.getName().equalsIgnoreCase(giftName));
        userGifts.values().forEach(gifts -> gifts.removeIf(gift -> gift.getName().equalsIgnoreCase(giftName)));
        sendMessage(new SendMessage(String.valueOf(ADMIN_ID), "Удалено."));
    }

    // отправка сообщения
    private void sendMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "ИМЯ ВАШЕГО БОТА";
    }

    @Override
    public String getBotToken() {
        return "ВАШ ТОКЕН";
    }
}
