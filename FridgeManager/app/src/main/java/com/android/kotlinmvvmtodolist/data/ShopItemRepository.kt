package com.android.kotlinmvvmtodolist.data

import androidx.lifecycle.LiveData
import com.android.kotlinmvvmtodolist.data.local.ShopItemDao
import com.android.kotlinmvvmtodolist.data.local.ShopItemEntry
import com.android.kotlinmvvmtodolist.data.local.TaskEntry
import javax.inject.Inject

class ShopItemRepository @Inject constructor(private val shopItemDao: ShopItemDao) {

    suspend fun insert(shopItemEntry: ShopItemEntry) = shopItemDao.insert(shopItemEntry)

    suspend fun updateData(shopItemEntry: ShopItemEntry) = shopItemDao.update(shopItemEntry)

    suspend fun deleteItem(shopItemEntry: ShopItemEntry) = shopItemDao.delete(shopItemEntry)

    fun searchDatabase(searchQuery: String): LiveData<List<ShopItemEntry>> {
        return shopItemDao.searchDatabase(searchQuery)
    }
    suspend fun deleteAll() {
        shopItemDao.deleteAll()
    }

    suspend fun deleteAllBought() {
        shopItemDao.deleteAllBought()
    }

    fun getAllItems() = shopItemDao.getAllItems()

    suspend fun getItemById(id: Int): ShopItemEntry? = shopItemDao.getItemById(id)

    suspend fun getItemByAddRequestId(addId: Int): ShopItemEntry? = shopItemDao.getItemByAddRequestId(addId)

}
