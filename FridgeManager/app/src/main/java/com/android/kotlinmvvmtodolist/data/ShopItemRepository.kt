package com.android.kotlinmvvmtodolist.data

import com.android.kotlinmvvmtodolist.data.local.ShopItemDao
import com.android.kotlinmvvmtodolist.data.local.ShopItemEntry
import javax.inject.Inject

class ShopItemRepository @Inject constructor(private val shopItemDao: ShopItemDao) {

    suspend fun insert(shopItemEntry: ShopItemEntry) = shopItemDao.insert(shopItemEntry)

    suspend fun updateData(shopItemEntry: ShopItemEntry) = shopItemDao.update(shopItemEntry)

    suspend fun deleteItem(shopItemEntry: ShopItemEntry) = shopItemDao.delete(shopItemEntry)

    suspend fun deleteAll() {
        shopItemDao.deleteAll()
    }

    fun getAllItems() = shopItemDao.getAllItems()
}
