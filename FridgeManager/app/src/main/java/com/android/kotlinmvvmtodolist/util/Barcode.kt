package com.android.kotlinmvvmtodolist.util

import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.android.kotlinmvvmtodolist.data.local.TaskEntry
import com.android.kotlinmvvmtodolist.databinding.FragmentTaskBinding
import com.android.kotlinmvvmtodolist.ui.task.TaskViewModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object Barcode {
    fun processScannedBarcode(
        scannedBarcode: String,
        binding: FragmentTaskBinding,
        activity: FragmentActivity,
        viewModel: TaskViewModel
    ) {
        val apiUrl = "https://world.openfoodfacts.org/api/v0/product/$scannedBarcode.json"

        val request = Request.Builder()
            .url(apiUrl)
            .build()

        val client = OkHttpClient.Builder()
            .sslSocketFactory(TrustAllCerts.createSSLSocketFactory(), TrustAllCerts)
            .hostnameVerifier { _, _ -> true }
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle network failure or error
                Log.d("Requesting", "Request fails")
                e.printStackTrace()
                // Print the error message
                Log.d("Requesting", "Error: ${e.message}")
            }

            var fail = false

            override fun onResponse(call: Call, response: Response) {
                Log.d("Requesting", "Requesting http request")

                val responseBody = response.body?.string()
                var product: JSONObject? = null
                try {
                    product = responseBody?.let { parseProductFromJson(it) }
                } catch (_: java.lang.Exception) {
                    Log.d("Requesting", "product is null, not found")
                }


                // Process the response body
                if (response.isSuccessful && responseBody != null && product != null) {
                    // Parse the response JSON
                    Log.d("Requesting", "product = $product")

                    try {
                        // Retrieve the food name
                        val productName: String = try {
                            product.getString("product_name") ?: ""
                        } catch (e: JSONException) {
                            ""
                        }

                        // Retrieve the serving quantity

                        val productAmount: Int = try {
                            product.getString("product_quantity").toInt()
                        } catch (e: JSONException) {
                            1
                        }

                        val productUnit: String = try {
                            product.getString("quantity") ?: ""
                        } catch (e: JSONException) {
                            ""
                        }

                        // TODO: extract unit
//                        val number = productUnit.split(Regex("\\D+"))
//                        val unit = productUnit.split(Regex("\\d+"))[0]

                        // Retrieve expiration date
                        // expireDate in the entry should be in format: "yyyy-MM-dd"
                        val expirationDateString: String = try {
                            product.getString("expiration_date") ?: ""
                        } catch (e: JSONException) {
                            ""
                        }

                        // TODO: handle expiration date formats
                        val defaultDateString = LocalDate.now().plusDays(1)
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        var expirationDate = defaultDateString.format(formatter)
                        Log.d("Requesting", "default = $expirationDate")

                        if (expirationDateString != "") {
                            Log.d("Requesting", "dateString = $expirationDateString")
                            try {
                                var year: String = ""
                                var month: String = ""
                                var day: String = ""

                                var times = expirationDateString.split(' ')
                                // case 2 eg: 21 Jul 2023
                                if (times.size == 3) {
                                    val dayValue = times[0].toInt()
                                    day = if (dayValue < 10) "0$dayValue" else "$dayValue"
                                    month = when (times[1]) {
                                        "Jan" -> "01"
                                        "Feb" -> "02"
                                        "Mar" -> "03"
                                        "Apr" -> "04"
                                        "May" -> "05"
                                        "Jun" -> "06"
                                        "Jul" -> "07"
                                        "Aug" -> "08"
                                        "Sep" -> "09"
                                        "Oct" -> "10"
                                        "Nov" -> "11"
                                        "Dec" -> "12"
                                        else -> "0"
                                    }
                                    year = expirationDateString.split(' ')[2]
                                    expirationDate = "$year-$month-$day"
                                }

                                times = expirationDateString.split('/')
                                if (times.size == 3) {
                                    year = times[0]
                                    if (year.length == 2) year = "20$year"
                                    val monthValue = times[1].toInt()
                                    month = if (monthValue < 10) "0$monthValue" else "$monthValue"
                                    val dayValue = times[2].toInt()
                                    day = if (dayValue < 10) "0$dayValue" else "$dayValue"
                                    expirationDate = "$year-$month-$day"
                                }

                            } catch (_: java.lang.Exception) {
                                // handle the expiration date exception
                                Log.d("Requesting", "dateString = $expirationDateString")
                            }
                        } else {
                            Log.d("Requesting", "dateString = null $expirationDateString")
                        }

                        val notificationID = viewModel.getNextNotificationID()

                        fail = productName == "" || expirationDate == ""

                        if (fail) {
                            activity.runOnUiThread {
                                Toast.makeText(activity, "Scan failed", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            // TODO: handle the failure
                            Log.d("Requesting", "fail because of some contents")
                            return
                        } else {
                            val taskEntry = TaskEntry(
                                0,
                                productName,
                                6, // TODO
                                System.currentTimeMillis(),
                                expirationDate,
                                productAmount,
                                0, // TODO
                                notificationID,
                                0,
                                "",
                                "1",
                                viewModel.getNextAddRequestID()
                            )

                            viewModel.insert(taskEntry)
                            Log.d("Requesting", "added success")

                            val notificationTime =
                                NotificationAlert.getNotificationTime(expirationDate, "")
                            val daysLeft = NotificationAlert.calculateDaysLeft(expirationDate)
                            val title = "$productName expire soon"
                            // TODO: notify ? days before expiration
                            val message1 = "Your $productName will expire in $daysLeft days!"
                            val message = "Your $productName will expire tomorrow!!!"
                            Log.d("Requesting", "schedule start")

                            NotificationAlert.scheduleNotification(
                                binding.root.context,
                                activity,
                                title,
                                message,
                                notificationTime,
                                notificationID
                            )

                            activity.runOnUiThread {
                                Toast.makeText(
                                    binding.root.context,
                                    "Successfully Added!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            Log.d("Requesting", "Success")
                        }
                    } catch (e: java.lang.Exception) {
                        Log.d("Requesting", "$e.message")
                    }
                } else {
                    Log.d("Requesting", "Product not found")
                    activity.runOnUiThread {
                        Toast.makeText(
                            binding.root.context,
                            "Product not found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    fun parseProductFromJson(json: String): JSONObject? {
        return try {
            JSONObject(json).optJSONObject("product")
        } catch (e: Exception) {
            null
        }
    }
}