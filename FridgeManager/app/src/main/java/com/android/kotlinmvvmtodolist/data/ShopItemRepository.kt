package com.android.kotlinmvvmtodolist.data

import androidx.lifecycle.LiveData
import com.android.kotlinmvvmtodolist.data.local.ShopItemDao
import com.android.kotlinmvvmtodolist.data.local.TaskDao
import com.android.kotlinmvvmtodolist.data.local.TaskEntry
import javax.inject.Inject

class ShopItemRepository @Inject constructor(private val shopItemDao: ShopItemDao) {

    suspend fun insert(taskEntry: TaskEntry) = shopItemDao.insert(taskEntry)

    suspend fun updateData(taskEntry: TaskEntry) = shopItemDao.update(taskEntry)

    suspend fun deleteItem(taskEntry: TaskEntry) = shopItemDao.delete(taskEntry)

    suspend fun deleteAll() {
        shopItemDao.deleteAll()
    }

    fun getAllItems() = shopItemDao.getAllItems()
}
