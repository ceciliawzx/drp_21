package com.android.kotlinmvvmtodolist.ui.chat

import com.android.kotlinmvvmtodolist.util.User
import com.google.firebase.database.DatabaseReference

object ChatUtil {

    fun pullMessage(pullFrom: DatabaseReference,
                    messageList: MutableList<Message>,
                    messageAdapter: MessageAdapter?) {
        val temp = pullFrom.get()
        while (!temp.isComplete) {
        }
        val dataSnapshot = temp.result
        for (childSnapshot in dataSnapshot.children) {
            val message = childSnapshot.getValue(Message::class.java)
            message?.let { messageList.add(it) }
        }

        messageAdapter?.notifyDataSetChanged()

    }
}