package org.example.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.ChatAction
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.extensions.filters.Filter
import com.github.kotlintelegrambot.logging.LogLevel
import io.github.cdimascio.dotenv.Dotenv
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.bot.session.SessionManager
import org.example.bot.utils.chatId
import org.example.data.remote.repository.WeatherRepository

private const val GIF_WAITING_URL = "https://media.tenor.com/OBEfKgDoCogAAAAC/pulp-fiction-john-travolta.gif"
private const val BOT_ANSWER_TIMEOUT = 30
private const val METRIC = "metric"

class WeatherBot(
    private val weatherRepository: WeatherRepository,
    private val dotenv: Dotenv = Dotenv.load(),
    private val sessionManager: SessionManager = SessionManager(),
) {

    fun createBot(): Bot {
        return bot {
            timeout = BOT_ANSWER_TIMEOUT
            token = dotenv["BOT_TOKEN"]
            logLevel = LogLevel.Error

            dispatch {
                setupCallbacks()
                setupCommands()
            }
        }
    }
    private fun Dispatcher.setupCallbacks() {
        callbackQuery(callbackData = "getMyLocation") {
            sessionManager.getOrCreateSession(chatId.chatId)
            bot.sendMessage(
                chatId = chatId,
                text = "Введи свой город",
            )
            location {
                CoroutineScope(Dispatchers.IO).launch {
                    val session = sessionManager.getOrCreateSession(chatId.chatId)
                    session.country = weatherRepository.getReverseGeocodingCountryName(
                        location.latitude.toString(),
                        location.longitude.toString(),
                        "jsonv2",
                    ).address.country

                    bot.sendMessage(
                        chatId = chatId,
                        text = "Твой город: ${session.country}, верно?\nЕсли неверно, скинь локацию еще раз",
                        replyMarkup = InlineKeyboardMarkup.create(
                            listOf(InlineKeyboardButton.CallbackData("Да, верно", "yes_label"))
                        )
                    )
                }
            }
        }

        callbackQuery("enterManually") {
            sessionManager.getOrCreateSession(chatId.chatId)
            bot.sendMessage(
                chatId = chatId,
                text = "Введи свой город вручную",
            )
            message(Filter.Text) {
                val session = sessionManager.getOrCreateSession(chatId.chatId)
                session.country = message.text.orEmpty()
                bot.sendMessage(
                    chatId = chatId,
                    text = "Твой город: ${session.country}, верно?\nЕсли неверно, введи город еще раз",
                    replyMarkup = InlineKeyboardMarkup.create(
                        listOf(InlineKeyboardButton.CallbackData("Да, верно", "yes_label"))
                    )
                )
            }
        }

        callbackQuery(callbackData = "yes_label") {
            val session = sessionManager.getOrCreateSession(chatId.chatId)
            bot.apply {
                sendAnimation(
                    chatId = chatId,
                    animation = TelegramFile.ByUrl(GIF_WAITING_URL)
                )
                sendMessage(
                    chatId = chatId,
                    text = "Узнаем вашу погоду...",
                )
                sendChatAction(chatId = chatId, action = ChatAction.TYPING)
            }

            CoroutineScope(Dispatchers.IO).launch {
                val currentWeather = weatherRepository.getCurrentWeather(
                    session.country,
                    dotenv["API_KEY"],
                    METRIC
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
                session.country = ""
            }
        }
    }
        private fun Dispatcher.setupCommands() {
            command("start") {
                sessionManager.getOrCreateSession(chatId.chatId)
                bot.sendMessage(
                    chatId = chatId,
                    text = "Привет! Я бот, который показывает погоду в вашем городе!\nДля запуска бота введите /weather",
                )
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
                )
            }
        }

}