package com.android.kotlinmvvmtodolist.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface ShopItemDao {

    @Insert
    suspend fun insert(shopItemEntry: ShopItemEntry)

    @Delete
    suspend fun delete(shopItemEntry: ShopItemEntry)

    @Update
    suspend fun update(shopItemEntry: ShopItemEntry)

    @Query("SELECT * FROM shopping_list_table ORDER BY timestamp DESC")
    fun getAllItems(): Flow<List<ShopItemEntry>>

    @Query("SELECT * FROM shopping_list_table WHERE title LIKE :searchQuery ORDER BY timestamp DESC")
    fun searchDatabase(searchQuery: String): LiveData<List<ShopItemEntry>>

    @Query("DELETE FROM shopping_list_table")
    suspend fun deleteAll()

    @Query("DELETE FROM shopping_list_table WHERE bought = 1")
    suspend fun deleteAllBought()

    @Query("SELECT * FROM shopping_list_table WHERE id = :id")
    suspend fun getItemById(id: Int): ShopItemEntry?

    @Query("SELECT * FROM shopping_list_table WHERE addRequestId = :addId")
    suspend fun getItemByAddRequestId(addId: Int): ShopItemEntry?
}