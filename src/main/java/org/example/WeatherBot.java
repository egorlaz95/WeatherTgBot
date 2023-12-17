package org.example;

import com.github.prominence.openweathermap.api.OpenWeatherMapClient;
import com.github.prominence.openweathermap.api.enums.Language;
import com.github.prominence.openweathermap.api.enums.UnitSystem;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class WeatherBot extends TelegramLongPollingBot {
    private final String botToken = "6309469188:AAGVP6jzBdJOzNLGaJtuJB2DLlfQPX2SmIg";

    OpenWeatherMapClient openWeatherClient = new OpenWeatherMapClient("fe93767c04240b55487c68d101ee479b");
    String cityMessage;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();
            if (messageText.equals("/weather")) {
                sendWhatsCityMessage(chatId);
            }
            cityMessage = messageText;
            String weatherData;
            try {
                weatherData = getWeatherData();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            sendWeatherMessage(chatId, weatherData);
        }
    }

    private String getWeatherData() throws JSONException {
        String weatherData = null;
        final String weatherJson = openWeatherClient
                .currentWeather()
                .single()
                .byCityName(cityMessage)
                .language(Language.RUSSIAN)
                .unitSystem(UnitSystem.METRIC)
                .retrieve()
                .asJSON();
        System.out.println(weatherJson);
        JSONObject jsonObject = new JSONObject(weatherJson);
        JSONObject mainObject = jsonObject.getJSONObject("main");
        String temperature = mainObject.getString("temp");
        String feelsLike = mainObject.getString("feels_like");
        weatherData = "Температура: " + temperature + "°C \n " +
                "Ощущается как: " + feelsLike + " °C";
        return weatherData;
    }

    private SendMessage sendWeatherMessage(long chatId, String weatherData) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(weatherData);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return sendMessage;
    }

    private SendMessage sendWhatsCityMessage(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Введите название города");
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return sendMessage;
    }

    @Override
    public String getBotUsername() {
        return "YourWeatherBot";
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    private void sendErrorMessage(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Неверная команда. Используйте '/weather' для получения погоды.");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        WeatherBot weatherBot = new WeatherBot();
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(weatherBot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
