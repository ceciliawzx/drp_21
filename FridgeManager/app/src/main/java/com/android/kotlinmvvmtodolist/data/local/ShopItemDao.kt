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
    suspend fun insert(taskEntry: TaskEntry)

    @Delete
    suspend fun delete(taskEntry: TaskEntry)

    @Update
    suspend fun update(taskEntry: TaskEntry)

    @Query("SELECT * FROM shopping_list_table ORDER BY timestamp DESC")
    fun getAllItems(): LiveData<List<ShopItemEntry>>

    @Query("DELETE FROM task_table")
    suspend fun deleteAll()

}