package com.android.kotlinmvvmtodolist.util

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
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
        val addId = inputData.getInt(KEY_TASK_ADD_ID, -1)
        val taskTitle = inputData.getString(KEY_TASK_TITLE)
        val taskType = inputData.getInt(KEY_TASK_TYPE, -1)
        val expireDate = inputData.getString(KEY_EXPIRE_DATE)

        if (addId != -1 && taskTitle != null && taskType != -1 && expireDate != null) {
            val shopItemEntry = ShopItemEntry(
                id = 0,
                title = taskTitle,
                type = taskType,
                timestamp = System.currentTimeMillis(),
                continuous = 1,
                bought = 0,
                addId
            )
            shopListViewModel.insert(shopItemEntry)
        }
        return Result.success()
    }

    companion object {
        const val KEY_TASK_ADD_ID = "task_add_id"
        const val KEY_TASK_TITLE = "task_title"
        const val KEY_TASK_TYPE = "task_type"
        const val KEY_EXPIRE_DATE = "expire_date"
        private val scheduledWorkRequests = HashMap<Int, WorkRequest>()

        private lateinit var shopListViewModel: ShopListViewModel
        private fun setShopListViewModel(viewModel: ShopListViewModel) {
            shopListViewModel = viewModel
        }

        fun scheduleShopItemEntry(context: Context,
                                  addId: Int,
                                  expireDate: String,
                                  taskTitle: String,
                                  taskType: Int,
                                  shopListViewModel: ShopListViewModel) {

            val workManager = WorkManager.getInstance(context)

            // Cancel the original request, if existed
            val oriRequest = scheduledWorkRequests[addId]
            if (oriRequest != null) {
                Log.d("Adding", "reschedule: addId = $addId, requestId = ${oriRequest.id}")
                workManager.cancelWorkById(oriRequest.id)
                scheduledWorkRequests.remove(addId)
            }

            // Add this entry 3 day before the expirationDate
            // TODO
            val notificationDate = LocalDate.parse(expireDate).minusDays(3)

            val inputData = Data.Builder()
                .putInt(KEY_TASK_ADD_ID, addId)
                .putString(KEY_TASK_TITLE, taskTitle)
                .putInt(KEY_TASK_TYPE, taskType)
                .putString(KEY_EXPIRE_DATE, expireDate)
                .build()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<ShopItemWorker>()
                .setInputData(inputData)
                .setConstraints(constraints)
                .setInitialDelay(calculateDelay(notificationDate), TimeUnit.MILLISECONDS)
                .build()

            setShopListViewModel(shopListViewModel)

            scheduledWorkRequests[addId] = request

            workManager.enqueue(request)
            Log.d("Adding", "requestId = ${request.id}")
        }

        fun cancelRequest(context: Context, addId: Int) {
            val workManager = WorkManager.getInstance(context)
            val oriRequest = scheduledWorkRequests[addId]
            if (oriRequest != null) {
                workManager.cancelWorkById(oriRequest.id)
                scheduledWorkRequests.remove(addId)
            }
        }

        private fun calculateDelay(notificationDate: LocalDate): Long {
            val currentDate = LocalDate.now()
            val daysUntilNotification = ChronoUnit.DAYS.between(currentDate, notificationDate)
            val millisPerDay = 24 * 60 * 60 * 1000L
            // For test purpose, 15s
            return 15000L
//            return daysUntilNotification * millisPerDay
        }
    }
}