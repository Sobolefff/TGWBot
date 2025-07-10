package org.example.bot.utils

import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.MessageHandlerEnvironment
import com.github.kotlintelegrambot.entities.ChatId

val CommandHandlerEnvironment.chatId: ChatId
    get() = ChatId.fromId(message.chat.id)

val CallbackQueryHandlerEnvironment.chatId: ChatId
    get() = ChatId.fromId(callbackQuery.message?.chat?.id ?: error("ChatId not found"))

val MessageHandlerEnvironment.chatId: ChatId
    get() = ChatId.fromId(message.chat.id)

val ChatId.chatId: Long
    get() = (this as ChatId.Id).id