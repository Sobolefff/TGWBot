package org.example.bot

import com.github.kotlintelegrambot.Bot import com.github.kotlintelegrambot.bot import com.github.kotlintelegrambot.dispatch import com.github.kotlintelegrambot.dispatcher.* import com.github.kotlintelegrambot.entities.ChatAction import com.github.kotlintelegrambot.entities.ChatId import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup import com.github.kotlintelegrambot.entities.TelegramFile import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton import com.github.kotlintelegrambot.extensions.filters.Filter import com.github.kotlintelegrambot.logging.LogLevel import com.github.kotlintelegrambot.network.fold import kotlinx.coroutines.CoroutineScope import kotlinx.coroutines.Dispatchers import kotlinx.coroutines.launch import org.example.bot.session.SessionManager import org.example.bot.utils.chatId import org.example.data.remote.repository.WeatherRepository

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

private fun Dispatcher.setupCallbacks() {
    callbackQuery("getMyLocation") {
        sessionManager.getOrCreateSession(chatId.chatId)
        val promptMessage = bot.sendMessage(
            chatId = chatId,
            text = """
            📍 Отправь мне свою геопозицию через Telegram.
            
            Нажми на 📎 (или ➕ в поле ввода) → «Местоположение» → «Отправить».
        """.trimIndent()
        )

        val promptMessageId = promptMessage.fold({ it.messageId }, { null })
        sessionManager.getOrCreateSession(chatId.chatId).tempMessageIds.add(promptMessageId)
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
                sessionManager.getOrCreateSession(userId).apply {
                    this.country = country
                }

                val confirmMessage = bot.sendMessage(
                    chatId = currentChatId,
                    text = "Твой город: $country. Верно?\nЕсли неверно — отправь локацию ещё раз",
                    replyMarkup = InlineKeyboardMarkup.create(
                        listOf(InlineKeyboardButton.CallbackData("Да, верно", "yes_label"))
                    )
                )

                val confirmMessageId = confirmMessage.fold({ it.messageId }, { null })
                sessionManager.getOrCreateSession(userId).tempMessageIds.add(confirmMessageId)
            } catch (e: Exception) {
                bot.sendMessage(
                    chatId = currentChatId,
                    text = "Не удалось определить местоположение. Попробуйте снова или введите вручную."
                )
            }

            // Удаляем сообщение с подсказкой про 📎
            val session = sessionManager.getOrCreateSession(userId)
            session.tempMessageIds.forEach { msgId ->
                msgId?.let {
                    bot.deleteMessage(
                        chatId = currentChatId,
                        messageId = it
                    )
                }
            }
            session.tempMessageIds.clear()
        }
    }

    callbackQuery("enterManually") {
        sessionManager.getOrCreateSession(chatId.chatId)
        val manualPrompt = bot.sendMessage(
            chatId = chatId,
            text = "Введи свой город вручную",
        )

        val manualPromptId = manualPrompt.fold({ it.messageId }, { null })
        sessionManager.getOrCreateSession(chatId.chatId).tempMessageIds.add(manualPromptId)

        message(Filter.Text) {
            val session = sessionManager.getOrCreateSession(chatId.chatId)
            session.country = message.text.orEmpty()
            val confirmMessage = bot.sendMessage(
                chatId = chatId,
                text = "Твой город: ${session.country}, верно?\nЕсли неверно, введи город еще раз",
                replyMarkup = InlineKeyboardMarkup.create(
                    listOf(InlineKeyboardButton.CallbackData("Да, верно", "yes_label"))
                )
            )

            val confirmMsgId = confirmMessage.fold({ it.messageId }, { null })
            session.tempMessageIds.forEach { id ->
                id?.let { bot.deleteMessage(chatId = chatId, messageId = it) }
            }
            session.tempMessageIds.clear()
            session.tempMessageIds.add(confirmMsgId)
        }
    }

    callbackQuery("yes_label") {
        val session = sessionManager.getOrCreateSession(chatId.chatId)

        val loadingMsg = bot.sendMessage(
            chatId = chatId,
            text = "Узнаем вашу погоду..."
        )

        val loadingMessageId = loadingMsg.fold({ it.messageId }, { null })

        CoroutineScope(Dispatchers.IO).launch {
            val currentWeather = weatherRepository.getCurrentWeather(
                session.country,
                apiKey,
                METRIC
            )

            bot.sendAnimation(
                chatId = chatId,
                animation = TelegramFile.ByUrl(GIF_WAITING_URL)
            )

            bot.sendMessage(
                chatId = chatId,
                text = """
                    🌤 Сейчас в ${session.country}:
                     ☁ Облачность: ${currentWeather.clouds.all}%
                    🌡 Температура (градусы): ${currentWeather.main.temp}°C
                    🙎 ‍Ощущается как: ${currentWeather.main.feels_like}°C
                    💧 Влажность: ${currentWeather.main.humidity}%
                    🌪 Скорость ветра: ${currentWeather.wind.speed}м/с
                    🧭 Давление: ${(currentWeather.main.pressure)*0.75} мм рт. ст.
            """.trimIndent()
            )

            bot.sendMessage(
                chatId = chatId,
                text = "Если вы хотите узнать погоду в другом городе или еще раз, введите /weather"
            )

            // Удаляем временные сообщения
            session.tempMessageIds.forEach { id ->
                id?.let { bot.deleteMessage(chatId = chatId, messageId = it) }
            }
            loadingMessageId?.let { bot.deleteMessage(chatId = chatId, messageId = it) }

            session.tempMessageIds.clear()
            sessionManager.clearSession(chatId.chatId)
        }
    }
}

private fun Dispatcher.setupCommands() {
    command("start") {
        sessionManager.getOrCreateSession(chatId.chatId)
        bot.sendMessage(
            chatId = chatId,
            text = "Привет! Я бот, который показывает погоду в вашем городе!\nДля запуска бота введите /weather"
        )
    }

    command("weather") {
        val session = sessionManager.getOrCreateSession(chatId.chatId)
        val weatherMessage = bot.sendMessage(
            chatId = chatId,
            text = "Мне нужно знать твой город!",
            replyMarkup = InlineKeyboardMarkup.create(
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "Определить мой город по геолокации(для мобильных устройств)",
                        callbackData = "getMyLocation"
                    )
                ),
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "Ввести город вручную",
                        callbackData = "enterManually"
                    )
                )
            )
        )

        val messageId = weatherMessage.fold({ it.messageId }, { null })
        session.tempMessageIds.add(messageId)
    }
}

}

