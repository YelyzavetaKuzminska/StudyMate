Android Kotlin XML. Backend Python FastAPI Pydantic. HTTP JSON. AI аналіз тексту за правилами.

## Оновлення Lab 3

Backend визначає пріоритет за текстом завдання.
Android отримує рекомендацію через REST API
та виділяє пріоритет відповідним кольором.

## Архітектура Lab 4

Android (Kotlin/XML) взаємодіє з FastAPI через REST API.
Після успішного аналізу результат зберігається
у локальній базі SQLite через HistoryDatabase.kt.

Потік даних:
Android -> REST API -> FastAPI -> Android -> SQLite.

Перегляд та видалення історії виконуються локально.
Контракт REST API залишається незмінним.
