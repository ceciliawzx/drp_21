package com.android.kotlinmvvmtodolist.data.local

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.kotlinmvvmtodolist.util.Constants.TASK_TABLE
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = TASK_TABLE)
data class TaskEntry(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var title: String,
    var type: Int,
    var timestamp: Long,
    var expireDate: String,
    var amount: Int,
    var unit: Int,
    var notificationID: Int,
    var continuousBuying: Int,
    var imagePath: String,
    var notifyDaysBefore: String
):Parcelable