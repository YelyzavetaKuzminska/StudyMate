from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

app = FastAPI(title="StudyMate AI API")


class TaskRequest(BaseModel):
    task: str


class TaskResponse(BaseModel):
    category: str
    recommendation: str


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/analyze", response_model=TaskResponse)
def analyze_task(request: TaskRequest):
    task = request.task.strip()

    if not task:
        raise HTTPException(
            status_code=400,
            detail="Введіть навчальне завдання"
        )

    text = task.lower()

    if any(word in text for word in ["іспит", "екзамен", "контрольн", "тест"]):
        category = "Підготовка до перевірки знань"
        recommendation = "Розділи матеріал на теми, повторюй щодня та перевіряй себе тестами."

    elif any(word in text for word in ["прочитати", "книга", "стаття", "конспект"]):
        category = "Читання та опрацювання матеріалу"
        recommendation = "Читай невеликими частинами та записуй основні думки."

    elif any(word in text for word in ["код", "програм", "лабораторн", "проєкт"]):
        category = "Практичне завдання"
        recommendation = "Розбий роботу на етапи, почни з простого та перевір результат."

    else:
        category = "Загальне навчальне завдання"
        recommendation = "Визнач мету, склади план і виділи час для виконання."

    return TaskResponse(
        category=category,
        recommendation=recommendation
    )