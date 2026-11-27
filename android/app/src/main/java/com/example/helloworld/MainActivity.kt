package com.example.helloworld

import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var historyDatabase: HistoryDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        historyDatabase = HistoryDatabase(applicationContext)

        val taskInput = findViewById<EditText>(R.id.taskInput)
        val analyzeButton = findViewById<Button>(R.id.analyzeButton)
        val historyButton = findViewById<Button>(R.id.historyButton)
        val resultText = findViewById<TextView>(R.id.resultText)
        val loadingIndicator =
            findViewById<ProgressBar>(R.id.loadingIndicator)

        historyButton.setOnClickListener {
            showHistory()
        }

        analyzeButton.setOnClickListener {
            val task = taskInput.text.toString().trim()

            if (task.isEmpty()) {
                resultText.text =
                    "Будь ласка, введіть навчальне завдання."
                return@setOnClickListener
            }

            loadingIndicator.visibility = View.VISIBLE
            analyzeButton.isEnabled = false
            resultText.text = "Аналізуємо завдання..."

            Thread {
                var connection: HttpURLConnection? = null

                try {
                    val url = URL("http://10.0.2.2:8000/analyze")
                    connection =
                        url.openConnection() as HttpURLConnection

                    connection.requestMethod = "POST"
                    connection.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=utf-8"
                    )
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.doOutput = true

                    val requestBody = JSONObject()
                    requestBody.put("task", task)

                    connection.outputStream.use { output ->
                        output.write(
                            requestBody.toString()
                                .toByteArray(Charsets.UTF_8)
                        )
                    }

                    if (connection.responseCode != 200) {
                        throw Exception("Помилка сервера")
                    }

                    val response = connection.inputStream
                        .bufferedReader(Charsets.UTF_8)
                        .use { it.readText() }

                    val json = JSONObject(response)

                    val category = json.opt("category") as? String
                        ?: throw Exception("Некоректна категорія")

                    val recommendation =
                        json.opt("recommendation") as? String
                            ?: throw Exception("Некоректна рекомендація")

                    if (category.isBlank() ||
                        recommendation.isBlank()
                    ) {
                        throw Exception("Неповна відповідь")
                    }

                    val saved = try {
                        historyDatabase.addRecord(
                            task,
                            category,
                            recommendation
                        )
                        true
                    } catch (e: Exception) {
                        false
                    }

                    runOnUiThread {
                        val fullText =
                            "Категорія: $category\n\n" +
                                    "Рекомендація: ${recommendation.replace(" Пріоритет:", "\n\nПріоритет:")}\n\n" +
                                    if (saved) {
                                        "Збережено в історії."
                                    } else {
                                        "Не вдалося зберегти в історії."
                                    }

                        resultText.text = colorPriority(fullText)
                    }

                } catch (e: Exception) {
                    runOnUiThread {
                        resultText.text =
                            "Не вдалося проаналізувати завдання.\n" +
                                    "Перевір, чи запущений сервер, " +
                                    "і спробуй ще раз."
                    }

                } finally {
                    connection?.disconnect()

                    runOnUiThread {
                        loadingIndicator.visibility = View.GONE
                        analyzeButton.isEnabled = true
                    }
                }
            }.start()
        }
    }

    private fun colorPriority(text: String): SpannableString {
        val styledText = SpannableString(text)

        val priorityColors = mapOf(
            "Високий" to 0xFFC62828.toInt(),
            "Середній" to 0xFFE65100.toInt(),
            "Низький" to 0xFF2E7D32.toInt()
        )

        for ((priority, color) in priorityColors) {
            val phrase = "Пріоритет: $priority"
            val start = text.indexOf(phrase)

            if (start >= 0) {
                styledText.setSpan(
                    ForegroundColorSpan(color),
                    start,
                    start + phrase.length,
                    0
                )
            }
        }

        return styledText
    }

    private fun formatDate(time: Long): String {
        return SimpleDateFormat(
            "dd.MM.yyyy HH:mm:ss",
            Locale.getDefault()
        ).format(Date(time))
    }

    private fun showHistory() {
        val loading = AlertDialog.Builder(this)
            .setTitle("Історія завдань")
            .setMessage("Завантаження історії...")
            .setCancelable(false)
            .show()

        Thread {
            try {
                val records = historyDatabase.getAllRecords()

                runOnUiThread {
                    loading.dismiss()

                    if (records.isEmpty()) {
                        AlertDialog.Builder(this)
                            .setTitle("Історія завдань")
                            .setMessage(
                                "Історія поки порожня.\n" +
                                        "Проаналізуйте перше завдання."
                            )
                            .setPositiveButton("OK", null)
                            .show()
                    } else {
                        val items = records.map { record ->
                            val task = record.task.take(80)
                            val result = record.recommendation.replace(
                                " Пріоритет:", "\nПріоритет:"
                            )

                            "$task\n$result\n" +
                                    "${formatDate(record.createdAt)}" +
                                    " · ${record.status}"
                        }.toTypedArray()

                        AlertDialog.Builder(this)
                            .setTitle("Історія завдань")
                            .setItems(items) { _, position ->
                                showDetails(records[position].id)
                            }
                            .setNegativeButton("Закрити", null)
                            .show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    loading.dismiss()
                    showStorageError()
                }
            }
        }.start()
    }

    private fun showDetails(id: Long) {
        Thread {
            try {
                val record = historyDatabase.getRecord(id)

                runOnUiThread {
                    if (record == null) {
                        showHistory()
                        return@runOnUiThread
                    }

                    val details =
                        "Завдання: ${record.task}\n\n" +
                                "Категорія: ${record.category}\n\n" +
                                "Рекомендація: ${record.recommendation.replace(" Пріоритет:", "\n\nПріоритет:")}\n\n" +
                                "Дата: ${formatDate(record.createdAt)}\n\n" +
                                "Статус: ${record.status}"

                    AlertDialog.Builder(this)
                        .setTitle("Збережений результат")
                        .setMessage(colorPriority(details))
                        .setPositiveButton("Назад") { _, _ ->
                            showHistory()
                        }
                        .setNegativeButton("Видалити") { _, _ ->
                            confirmDelete(record.id)
                        }
                        .show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showStorageError()
                }
            }
        }.start()
    }

    private fun confirmDelete(id: Long) {
        AlertDialog.Builder(this)
            .setTitle("Видалення запису")
            .setMessage("Ви дійсно хочете видалити цей запис?")
            .setNegativeButton("Скасувати") { _, _ ->
                showDetails(id)
            }
            .setPositiveButton("Видалити") { _, _ ->
                Thread {
                    try {
                        historyDatabase.deleteRecord(id)

                        runOnUiThread {
                            showHistory()
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            showStorageError()
                        }
                    }
                }.start()
            }
            .show()
    }

    private fun showStorageError() {
        AlertDialog.Builder(this)
            .setTitle("Помилка")
            .setMessage("Не вдалося виконати операцію з історією.")
            .setPositiveButton("OK", null)
            .show()
    }
}
