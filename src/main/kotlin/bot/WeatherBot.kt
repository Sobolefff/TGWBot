package org.example.bot

import com.github.kotlintelegrambot.Bot import com.github.kotlintelegrambot.bot import com.github.kotlintelegrambot.dispatch import com.github.kotlintelegrambot.dispatcher.* import com.github.kotlintelegrambot.entities.ChatAction import com.github.kotlintelegrambot.entities.ChatId import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup import com.github.kotlintelegrambot.entities.TelegramFile import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton import com.github.kotlintelegrambot.logging.LogLevel import kotlinx.coroutines.CoroutineScope import kotlinx.coroutines.Dispatchers import kotlinx.coroutines.launch import org.example.bot.session.SessionManager import org.example.bot.utils.chatId import org.example.data.remote.repository.WeatherRepository

private const val GIF_WAITING_URL = "https://media.tenor.com/OBEfKgDoCogAAAAC/pulp-fiction-john-travolta.gif" private const val BOT_ANSWER_TIMEOUT = 30 private const val METRIC = "metric"

class WeatherBot( private val weatherRepository: WeatherRepository, private val sessionManager: SessionManager, ) { private val botToken = System.getenv("BOT_TOKEN") ?: error("BOT_TOKEN env var is missing") private val apiKey = System.getenv("API_KEY") ?: error("API_KEY env var is missing")

fun createBot(): Bot {
    return bot {
        timeout = BOT_ANSWER_TIMEOUT
        token = botToken
        logLevel = LogLevel.Error

        dispatch {
            setupCallbacks()
            setupCommands()
        }
    }
}

private fun Dispatcher.setupCommands() {
    command("start") {
        sessionManager.getOrCreateSession(chatId.chatId)
        bot.sendMessage(
            chatId = chatId,
            text = "Привет! Я бот, который показывает погоду в вашем городе!\nДля запуска бота введите /weather",
        ).fold({ message ->
            bot.deleteMessage(chatId, message.messageId)
        }, {})
    }

    command("weather") {
        sessionManager.getOrCreateSession(chatId.chatId)
        val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
            listOf(
                InlineKeyboardButton.CallbackData(
                    text = "Определить мой город по геолокации(для мобильных устройств)",
                    callbackData = "getMyLocation",
                )
            ),
            listOf(
                InlineKeyboardButton.CallbackData(
                    text = "Ввести город вручную",
                    callbackData = "enterManually",
                )
            )
        )

        bot.sendMessage(
            chatId = chatId,
            text = "Мне нужно знать твой город!",
            replyMarkup = inlineKeyboardMarkup,
        ).fold({ message ->
            bot.deleteMessage(chatId, message.messageId)
        }, {})
    }
}

private fun Dispatcher.setupCallbacks() {
    callbackQuery("getMyLocation") {
        sessionManager.getOrCreateSession(chatId.chatId)
        callbackQuery.message?.messageId?.let {
            bot.deleteMessage(chatId, it)
        }

        bot.sendMessage(
            chatId = chatId,
            text = """
            \uD83D\uDCCD Отправь мне свою геопозицию через Telegram.

            Нажми на \uD83D\uDCCE (или ➕ в поле ввода) → «Местоположение» → «Отправить».
            """.trimIndent(),
        ).fold({ message ->
            sessionManager.getOrCreateSession(chatId.chatId).let {
                it.country = "__WAITING_LOCATION__"
            }
            bot.deleteMessage(chatId, message.messageId)
        }, {})
    }

    location {
        val currentChatId = ChatId.fromId(message.chat.id)
        val userId = message.chat.id
        val latitude = location.latitude.toString()
        val longitude = location.longitude.toString()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = weatherRepository.getReverseGeocodingCountryName(
                    latitude,
                    longitude,
                    "json"
                )

                val country = response.address.city ?: "Неизвестно"
                sessionManager.getOrCreateSession(userId).country = country

                val keyboard = InlineKeyboardMarkup.create(
                    listOf(InlineKeyboardButton.CallbackData("Да, верно", "yes_label"))
                )

                bot.sendMessage(
                    chatId = currentChatId,
                    text = "Твой город: $country. Верно?\nЕсли неверно — отправь локацию ещё раз",
                    replyMarkup = keyboard
                ).fold({ message ->
                    bot.deleteMessage(currentChatId, message.messageId)
                }, {})
            } catch (e: Exception) {
                bot.sendMessage(
                    chatId = currentChatId,
                    text = "Не удалось определить местоположение. Попробуйте снова или введите вручную."
                )
            }
        }
    }

    callbackQuery("enterManually") {
        sessionManager.getOrCreateSession(chatId.chatId)
        callbackQuery.message?.messageId?.let {
            bot.deleteMessage(chatId, it)
        }

        bot.sendMessage(
            chatId = chatId,
            text = "Введи свой город вручную",
        ).fold({ message ->
            bot.deleteMessage(chatId, message.messageId)
        }, {})
    }

    callbackQuery("yes_label") {
        val session = sessionManager.getOrCreateSession(chatId.chatId)
        callbackQuery.message?.messageId?.let {
            bot.deleteMessage(chatId, it)
        }

        bot.sendAnimation(
            chatId = chatId,
            animation = TelegramFile.ByUrl(GIF_WAITING_URL)
        )

        bot.sendMessage(
            chatId = chatId,
            text = "Узнаем вашу погоду..."
        ).fold({ loadingMessage ->
            bot.sendChatAction(chatId = chatId, action = ChatAction.TYPING)

            CoroutineScope(Dispatchers.IO).launch {
                val currentWeather = weatherRepository.getCurrentWeather(
                    session.country,
                    apiKey,
                    METRIC
                )
                bot.sendMessage(
                    chatId = chatId,
                    text = """
                        \uD83C\uDF24 Сейчас в ${session.country}:
                         ☁ Облачность: ${currentWeather.clouds.all}%
                        \uD83C\uDF21 Температура (градусы): ${currentWeather.main.temp}°C
                        🙎 ‍Ощущается как: ${currentWeather.main.feels_like}°C
                        \uD83D\uDCA7 Влажность: ${currentWeather.main.humidity}%
                        \uD83C\uDF2A Скорость ветра: ${currentWeather.wind.speed}м/с
                        \uD83E\uDDEB Давление: ${(currentWeather.main.pressure) * 0.75} мм рт. ст.
                    """.trimIndent()
                )

                bot.deleteMessage(chatId = chatId, messageId = loadingMessage.messageId)
                sessionManager.clearSession(chatId.chatId)
            }
        }, {})
    }
}

}

