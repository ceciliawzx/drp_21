package com.android.kotlinmvvmtodolist.ui.chat

class Message {
    var message: String? = null
    var senderId: String? = null
    var timestamp: Long = 0
    constructor(){}
    constructor(message: String?, senderId: String?) {
        this.message = message
        this.senderId = senderId
        this.timestamp = System.currentTimeMillis()
    }
}