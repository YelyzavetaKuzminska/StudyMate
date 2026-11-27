# StudyMate — AI Backend

StudyMate — мобільний застосунок для допомоги студентам у виконанні навчальних завдань.

Сервер створений за допомогою Python та FastAPI. Він отримує навчальне завдання, визначає його категорію та повертає рекомендацію.

## Використані технології

- Python 3
- FastAPI
- Uvicorn
- Pydantic
- JSON
- Android (Kotlin)

 Запуск сервера

1. Відкрити Terminal у папці StudyMateBackend.
2. Створити віртуальне середовище командою: `python3 -m venv .venv`
3. Активувати середовище командою: `source .venv/bin/activate`
4. Встановити бібліотеки командою: `python -m pip install -r requirements.txt`
5. Запустити сервер командою: `uvicorn main:app --reload`

Документація API доступна за адресою:

http://127.0.0.1:8000/docs

 API сервера

GET /health — перевірка роботи сервера.

Успішна відповідь: `{"status":"ok"}`

POST /analyze — аналіз навчального завдання.

Приклад запиту:

`{"task":"Підготуватися до контрольної з математики"}`

Приклад відповіді:

`{"category":"Підготовка до перевірки знань","recommendation":"Розділи матеріал на теми, повторюй щодня та перевіряй себе тестами."}`

Якщо завдання порожнє або містить лише пробіли, сервер повертає помилку HTTP 400.

 Принцип роботи AI

У застосунку використовується простий алгоритм класифікації навчальних завдань за ключовими словами.

Система визначає чотири категорії:

1. Підготовка до перевірки знань.
2. Читання та опрацювання матеріалу.
3. Практичне завдання.
4. Загальне навчальне завдання.

Для кожної категорії формується відповідна рекомендація.

 Підключення Android

Android-застосунок надсилає POST-запити на адресу:

http://10.0.2.2:8000/analyze

Адреса 10.0.2.2 дозволяє Android-емулятору підключатися до локального сервера на MacBook.

 Тестування

Успішно перевірено:

- П'ять прикладів навчальних завдань.
- Обробку порожнього поля.
- Повідомлення про недоступність сервера.
- Повторний запит після відновлення сервера.

 Обмеження

- AI працює на основі ключових слів.
- Рекомендації є заздалегідь визначеними.
- Сервер запускається локально.
- Для локального тестування використовується HTTP.

 Подальший розвиток

У майбутньому планується додати дедлайни, синхронізацію історії та розширені AI-рекомендації.

## Laboratory Work 3 — Vibe Coding

### New functionality

- Automatic task priority detection.
- High priority — red.
- Medium priority — orange.
- Low priority — green.
- Existing REST API contract preserved.

### Backend setup

```bash
cd backend
python -m pip install fastapi uvicorn
python -m uvicorn main:app --host 0.0.0.0 --port 8000
```

### Android setup

Open the android directory in Android Studio.
Synchronize Gradle and run the app on an Android emulator.
Backend URL: http://10.0.2.2:8000

### API verification

```bash
curl http://localhost:8000/health
curl -X POST http://localhost:8000/analyze \
  -H "Content-Type: application/json" \
  -d '{"task":"Терміново здати лабораторну"}'
```

### Knowledge Base

- [Project overview](docs/project-overview.md)
- [Requirements](docs/requirements.md)
- [Architecture](docs/architecture.md)
- [API contract](docs/api-contract.md)
- [AI component](docs/ai-component.md)
- [Testing](docs/testing.md)
- [Known limitations](docs/known-limitations.md)
- [Decisions](docs/decisions.md)
- [AI instructions](docs/ai-instructions.md)
- [Plan comparison](docs/plan-comparison.md)
- [AI prompts](docs/prompts.md)

## Лабораторна робота 4 — локальне збереження даних

- Використання SQLite в Android.
- Автоматичне збереження успішних результатів аналізу.
- Перегляд історії завдань.
- Перегляд детальної інформації про запис.
- Видалення записів із підтвердженням.
- Збереження даних після перезапуску застосунку.
- Перегляд історії без підключення до сервера.
- Кольорове відображення пріоритетів.

Реалізація: feature/task-history.
Pull Request: #2.
