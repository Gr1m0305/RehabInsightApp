package com.example.rehabinsight.data.network

import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

fun testClientLogin() {

    Thread {

        try {

            val url = URL(
                "http://10.0.2.2:8000/api/client/login"
            )

            val connection =
                url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"

            connection.setRequestProperty(
                "Content-Type",
                "application/json"
            )

            connection.doOutput = true

            val json = JSONObject().apply {

                put(
                    "email",
                    "dummy.client1@example.com"
                )

                put(
                    "password",
                    "dummy_client_hash_1"
                )
            }

            connection.outputStream.use { output ->

                output.write(
                    json
                        .toString()
                        .toByteArray()
                )
            }

            val responseCode =
                connection.responseCode

            val response = if (
                responseCode in 200..299
            ) {

                connection
                    .inputStream
                    .bufferedReader()
                    .use {
                        it.readText()
                    }

            } else {

                connection
                    .errorStream
                    .bufferedReader()
                    .use {
                        it.readText()
                    }
            }

            Log.d(
                "LOGIN_TEST",
                "Code: $responseCode"
            )

            Log.d(
                "LOGIN_TEST",
                "Response: $response"
            )

            connection.disconnect()

        } catch (error: Exception) {

            Log.e(
                "LOGIN_TEST",
                "Login request failed",
                error
            )
        }
    }.start()
}