package org.example.bot.session

class SessionManager {
    private val sessions = mutableMapOf<Long, UserSession>()

    fun getOrCreateSession(chatId: Long): UserSession {
        return sessions.getOrPut(chatId) { UserSession() }
    }

    fun clearSession(chatId: Long) {
        sessions.remove(chatId)
    }

    fun updateSession(chatId: Long, update: UserSession.() -> Unit) {
        val session = getOrCreateSession(chatId)
        session.update()
    }

    fun getSession(chatId: Long): UserSession? = sessions[chatId]
}