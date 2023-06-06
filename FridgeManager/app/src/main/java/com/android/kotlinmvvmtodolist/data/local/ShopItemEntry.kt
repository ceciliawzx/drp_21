package com.android.kotlinmvvmtodolist.data.local


import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.kotlinmvvmtodolist.util.Constants.SHOPPING_TABLE
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = SHOPPING_TABLE)
data class ShopItemEntry(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var title: String,
    var type: Int,
    var timestamp: Long
    ):Parcelable