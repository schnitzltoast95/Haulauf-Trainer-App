package com.example.myapplication.ui

import android.os.Build
import android.content.Context
import com.example.myapplication.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

internal object BugReportClient {
    const val issueRepository = "schnitzltoast95/Haulauf-Trainer-App"

    suspend fun submit(
        context: Context,
        title: String,
        description: String,
        reportContext: String
    ): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val token = context.getString(R.string.bug_report_token)
                require(token.isNotBlank()) { "Bug reporting is not configured" }
                val body = buildString {
                    appendLine("<!-- haulauf-app-bug-report:v1 -->")
                    appendLine("## Bericht")
                    appendLine(description.trim())
                    appendLine()
                    appendLine("## App-Informationen")
                    appendLine(reportContext)
                    val version = context.packageManager
                        .getPackageInfo(context.packageName, 0).versionName ?: "unbekannt"
                    appendLine("- App-Version: $version")
                    appendLine("- Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                    appendLine("- Gerät: ${Build.MANUFACTURER} ${Build.MODEL}")
                    appendLine()
                    appendLine("_Aus der App gemeldet; ohne GitHub-Konto der meldenden Person._")
                }
                val connection = (URL("https://api.github.com/repos/$issueRepository/issues")
                    .openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 15_000
                    readTimeout = 15_000
                    doOutput = true
                    setRequestProperty("Accept", "application/vnd.github+json")
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
                    setRequestProperty("Authorization", "Bearer $token")
                }
                try {
                    val payload = JSONObject()
                        .put("title", "[App-Fehler] ${title.trim()}")
                        .put("body", body)
                        .toString()
                    connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
                    if (connection.responseCode != HttpURLConnection.HTTP_CREATED) {
                        error("GitHub returned HTTP ${connection.responseCode}")
                    }
                    JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
                        .getString("html_url")
                } finally {
                    connection.disconnect()
                }
            }
        }
}
