package org.example.bot.session

data class UserSession (
    var country: String = "",
    val tempMessageIds: MutableList<Int?> = mutableListOf()
)