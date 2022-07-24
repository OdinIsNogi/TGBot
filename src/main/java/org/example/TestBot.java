package org.example;

import lombok.SneakyThrows;
import org.example.entity.Currency;
import org.example.service.CurrencyConvertService;
import org.example.service.CurrencyModeService;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Hello world!
 */
public class TestBot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return "@Giga";
    }

    @Override
    public String getBotToken() {
        return "5441086722:AAHV5Rbn-K5A2uEC5egKxcVfvpB7rgGjWVE";
    }

    private final CurrencyModeService currencyModeService = CurrencyModeService.getInstance();
    private final CurrencyConvertService currencyConvertService = CurrencyConvertService.getInstance();

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        } else if (update.hasMessage()) {
            handleMessage(update.getMessage());
        }
    }

    @SneakyThrows
    private void handleCallback(CallbackQuery callbackQuery) {
        Message message = callbackQuery.getMessage();
        String[] param = callbackQuery.getData().split(":");
        String action = param[0];
        Currency newCurrency = Currency.valueOf(param[1]);
        switch (action) {
            case "ORIGINAL":
                currencyModeService.setOriginalCurrency(message.getChatId(), newCurrency);
                break;
            case "TARGET":
                currencyModeService.setTargetCurrency(message.getChatId(), newCurrency);
                break;
        }
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        Currency originalCurrency = currencyModeService.getOriginalCurrency(message.getChatId());
        Currency targetCurrency = currencyModeService.getTargetCurrency(message.getChatId());

        //Валюты(кнопки)
        for (Currency currency : Currency.values()) {
            buttons.add(
                    Arrays.asList(
                            InlineKeyboardButton.builder().text(getCurrencyButton(originalCurrency, currency))
                                    .callbackData("ORIGINAL:" + currency).build(),
                            InlineKeyboardButton.builder().text(getCurrencyButton(targetCurrency, currency))
                                    .callbackData("TARGET:" + currency).build()));
        }

        execute(
                EditMessageReplyMarkup.builder()
                        .chatId(message.getChatId().toString())
                        .messageId(message.getMessageId())
                        .replyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons).build()).build());
    }

    @SneakyThrows
    private void handleMessage(Message message) {
        if (message.hasText() && message.hasEntities()) {
            Optional<MessageEntity> commandSetCurrency =
                    message.getEntities()
                            .stream()
                            .filter(e -> "bot_command".equals(e.getType())).findFirst();

            if (commandSetCurrency.isPresent()) {
                String command =
                        message.getText()
                                .substring(commandSetCurrency.get().getOffset(),
                                        commandSetCurrency.get().getLength());

                switch (command) {
                    case "/set_currency":

                        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
                        Currency originalCurrency = currencyModeService.getOriginalCurrency(message.getChatId());
                        Currency targetCurrency = currencyModeService.getTargetCurrency(message.getChatId());

                        //Валюты(кнопки)
                        for (Currency currency : Currency.values()) {
                            buttons.add(
                                    Arrays.asList(
                                            InlineKeyboardButton.builder().text(getCurrencyButton(originalCurrency, currency))
                                                    .callbackData("ORIGINAL:" + currency).build(),
                                            InlineKeyboardButton.builder().text(getCurrencyButton(targetCurrency, currency))
                                                    .callbackData("TARGET:" + currency).build()));
                        }

                        execute(SendMessage
                                .builder()
                                .text("Please enter Original and Target currencies")
                                .chatId(message.getChatId().toString())
                                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons).build())
                                .build());
                        return;
                }
            }

        }
        if (message.hasText()) {
            String messageText = message.getText();
            Optional<Double> value = parseDouble(messageText);
            Currency originalCurrency = currencyModeService.getOriginalCurrency(message.getChatId());
            Currency targetCurrency = currencyModeService.getTargetCurrency(message.getChatId());
            double ratio = currencyConvertService.getConvertRatio(originalCurrency,targetCurrency);
            if(value.isPresent()) {
                execute(SendMessage.builder()
                        .chatId(message.getChatId().toString())
                        .text(String
                                .format("%4.2f %s is %4.2f %s", value.get(),originalCurrency,(value.get()*ratio),targetCurrency))
                        .build());
                return;
            }
        }
    }

    private Optional<Double> parseDouble(String messageText) {
        try {
            return Optional.of(Double.parseDouble(messageText));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String getCurrencyButton(Currency saved, Currency current) {
        return saved == current ? current + "\uD83D\uDC4C" : current.name();
    }


    public static void main(String[] args) throws TelegramApiException {
        TestBot bot = new TestBot();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(bot);
    }
}
