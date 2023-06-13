package com.android.kotlinmvvmtodolist.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

object Constants {
    const val TASK_DATABASE = "task_database"
    const val TASK_TABLE = "task_table"
    const val SHOPPING_TABLE = "shopping_list_table"
    val USER_DATABASE_REFERENCE = Firebase
        .database("https://drp21-def08-default-rtdb.europe-west1.firebasedatabase.app")
        .reference
    val CUR_USER_ID = FirebaseAuth.getInstance().currentUser?.uid
}