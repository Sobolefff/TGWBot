package org.example.bot.session

data class UserSession (
    var country: String = "",
    var confirmationMsgId: Long? = null,
    val tempMessageIds: MutableList<Int?> = mutableListOf()
)