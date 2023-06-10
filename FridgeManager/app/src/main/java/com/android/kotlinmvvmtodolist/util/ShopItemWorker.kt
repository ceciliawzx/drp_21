package com.android.kotlinmvvmtodolist.util

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.android.kotlinmvvmtodolist.data.local.ShopItemEntry
import com.android.kotlinmvvmtodolist.ui.shopList.ShopListViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class ShopItemWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val taskId = inputData.getInt(KEY_TASK_ID, -1)
        val taskTitle = inputData.getString(KEY_TASK_TITLE)
        val taskType = inputData.getInt(KEY_TASK_TYPE, -1)

        if (taskId != -1 && taskTitle != null && taskType != -1) {
            // Retrieve the TaskEntry from the database using the taskId
            val shopItemEntry = ShopItemEntry(
                id = 0,
                title = taskTitle,
                type = taskType,
                timestamp = System.currentTimeMillis(),
                continuous = 1,
                bought = 0
            )
            shopListViewModel.insert(shopItemEntry)
        }
        return Result.success()
    }

    companion object {
        const val KEY_TASK_ID = "task_id"
        const val KEY_TASK_TITLE = "task_title"
        const val KEY_TASK_TYPE = "task_type"

        private lateinit var shopListViewModel: ShopListViewModel

        private fun setShopListViewModel(viewModel: ShopListViewModel) {
            shopListViewModel = viewModel
        }

        fun scheduleShopItemEntry(context: Context,
                                  taskId: Int,
                                  expireDate: String,
                                  taskTitle: String,
                                  taskType: Int,
                                  shopListViewModel: ShopListViewModel) {
            // Add this entry 3 day before the expirationDate
            // TODO
            val notificationDate = LocalDate.parse(expireDate).minusDays(3)

            val inputData = Data.Builder()
                .putInt(KEY_TASK_ID, taskId)
                .putString(KEY_TASK_TITLE, taskTitle)
                .putInt(KEY_TASK_TYPE, taskType)
                .build()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<ShopItemWorker>()
                .setInputData(inputData)
                .setConstraints(constraints)
                .setInitialDelay(calculateDelay(notificationDate), TimeUnit.MILLISECONDS)
                .build()

            val workManager = WorkManager.getInstance(context)

            setShopListViewModel(shopListViewModel)

            workManager.enqueue(request)
        }

        private fun calculateDelay(notificationDate: LocalDate): Long {
            val currentDate = LocalDate.now()
            val daysUntilNotification = ChronoUnit.DAYS.between(currentDate, notificationDate)
            val millisPerDay = 24 * 60 * 60 * 1000L
            // For test purpose, 5s
            return 5000L
//            return daysUntilNotification * millisPerDay
        }
    }
}