package com.android.kotlinmvvmtodolist.ui.chat

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Message {
    var message: String? = null
    var senderId: String? = null
    var sentTime: String? = null

    constructor(){}
    constructor(message: String?, senderId: String?) {
        this.message = message
        this.senderId = senderId
        this.sentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
    }
}