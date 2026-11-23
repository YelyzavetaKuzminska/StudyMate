package com.example.helloworld

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val taskInput = findViewById<EditText>(R.id.taskInput)
        val analyzeButton = findViewById<Button>(R.id.analyzeButton)
        val resultText = findViewById<TextView>(R.id.resultText)
        val loadingIndicator = findViewById<ProgressBar>(R.id.loadingIndicator)

        analyzeButton.setOnClickListener {
            val task = taskInput.text.toString().trim()

            if (task.isEmpty()) {
                resultText.text = "Будь ласка, введіть навчальне завдання."
                return@setOnClickListener
            }

            loadingIndicator.visibility = View.VISIBLE
            analyzeButton.isEnabled = false
            resultText.text = "Аналізуємо завдання..."

            Thread {
                var connection: HttpURLConnection? = null

                try {
                    val url = URL("http://10.0.2.2:8000/analyze")
                    connection = url.openConnection() as HttpURLConnection

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
                            requestBody.toString().toByteArray(Charsets.UTF_8)
                        )
                    }

                    val statusCode = connection.responseCode

                    if (statusCode != 200) {
                        throw Exception("Помилка сервера: $statusCode")
                    }

                    val response = connection.inputStream
                        .bufferedReader(Charsets.UTF_8)
                        .use { it.readText() }

                    val json = JSONObject(response)

                    val category = json.getString("category")
                    val recommendation = json.getString("recommendation")

                    runOnUiThread {
                        resultText.text =
                            "Категорія: $category\n\nРекомендація: $recommendation"
                    }

                } catch (e: Exception) {
                    runOnUiThread {
                        resultText.text =
                            "Не вдалося проаналізувати завдання.\n" +
                                    "Перевір, чи запущений сервер, і спробуй ще раз."
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
}
