# 🌦 TGWBot — Telegram Weather Bot на Kotlin

**TGWBot** — это Telegram-бот на языке Kotlin, который показывает текущую погоду в указанном городе. Он может определить ваш город по геопозиции или запросить ввод вручную. TGWBot использует OpenWeather API и поддерживает взаимодействие через inline-кнопки, сессии пользователей и анимацию загрузки.

---

## 🚀 Возможности

- 🔎 Определение города по геолокации
- ⌨️ Ввод города вручную
- 🧠 Обратное геокодирование (координаты → город)
- ☁ Получение текущей погоды:
  - Облачность, температура, давление, ветер и др.
- 💬 Взаимодействие через inline-кнопки
- 🌐 Поддержка нескольких сессий
- 📦 Готов к развёртыванию в Docker-контейнере

---

## 🧠 Технологии

- Язык: **Kotlin**
- Telegram API: [kotlin-telegram-bot](https://github.com/kotlin-telegram-bot/kotlin-telegram-bot)
- Асинхронность: `kotlinx.coroutines`
- API погоды: [OpenWeather](https://openweathermap.org/)
- API обратного геокодирования: [Nominatim](https://nominatim.openstreetmap.org/)
- Архитектура: `SessionManager` + `WeatherRepository`
- Контейнеризация: **Docker**

---

## 🐳 Развёртывание в Docker

### Шаг 1. Соберите Docker-образ:

```bash
docker build -t tgwbot .
```

### Шаг 2. Запустите контейнер с передачей переменных окружения:

```bash
docker run -d \
  --name tgwbot \
  -e BOT_TOKEN=your_telegram_bot_token \
  -e API_KEY=your_openweather_api_key \
  tgwbot
```


---

## 💬 Поддерживаемые команды

| Команда    | Описание                          |
|------------|-----------------------------------|
| `/start`   | Приветственное сообщение           |
| `/weather` | Запуск сценария определения города|

---

## 🧭 Пользовательский сценарий

1. Пользователь отправляет команду `/weather`
2. TGWBot предлагает:
   - 📍 Отправить геопозицию
   - ✍️ Ввести город вручную
3. После подтверждения города:
   - Показывается анимация ожидания
   - Возвращается текущий прогноз погоды

---

## 🧪 Пример ответа

```
🌤 Сейчас в Санкт-Петербург:
 ☁ Облачность: 52%
🌡 Температура: 22.1°C
🙎 Ощущается как: 21.7°C
💧 Влажность: 63%
🌪 Ветер: 3.2 м/с
🧭 Давление: 748.5 мм рт. ст.
```

---

## 🤖 Ссылка на бота в телеграм

- [Бот в телеграм](https://t.me/mrsoboleffweatherbot)

---

## 📬 Контакты

- Telegram: [@Sobolefff](https://t.me/mr_soboleff)
- GitHub: [github.com/Sobolefff](https://github.com/Sobolefff)

---

> TGWBot — простая погода в пару кликов прямо в Telegram 🌦️
