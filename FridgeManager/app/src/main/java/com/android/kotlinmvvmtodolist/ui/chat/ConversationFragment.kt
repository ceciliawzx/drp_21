package com.android.kotlinmvvmtodolist.ui.chat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlinmvvmtodolist.R
import com.android.kotlinmvvmtodolist.databinding.FragmentConversationBinding
import com.android.kotlinmvvmtodolist.ui.chat.ChatUtil.pullMessage
import com.android.kotlinmvvmtodolist.ui.task.TaskViewModel
import com.google.firebase.auth.FirebaseAuth
import com.android.kotlinmvvmtodolist.util.Constants.USER_DATABASE_REFERENCE
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener


class ConversationFragment : Fragment() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageView
    private val args by navArgs<ConversationFragmentArgs>()
    private lateinit var oppUid: String
    private val myUid = FirebaseAuth.getInstance().currentUser?.uid!!
    private lateinit var messageAdapter: MessageAdapter
    private val viewModel: TaskViewModel by activityViewModels()

    var receiverRoom: String? = null
    var senderRoom: String? = null

    private var _binding: FragmentConversationBinding? = null
    private val binding get() = _binding!!

    // Message retrieve
    private val messageList: MutableList<Message> = mutableListOf()
    private lateinit var messageListener: ValueEventListener
    private lateinit var myMessageRef: DatabaseReference
    private lateinit var oppMessageRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        oppUid = args.uid
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentConversationBinding.inflate(inflater, container, false)

        senderRoom = oppUid + myUid
        receiverRoom = myUid + oppUid

        chatRecyclerView = binding.chatView
        messageBox = binding.messageBox
        sendButton = binding.sendButton

        val myOppRef = USER_DATABASE_REFERENCE
            .child("User").child(myUid)
            .child("Contacts").child(oppUid)

        myMessageRef = myOppRef.child("Message")

        oppMessageRef = USER_DATABASE_REFERENCE
            .child("User").child(oppUid)
            .child("Contacts").child(myUid)
            .child("Message")

        messageAdapter = MessageAdapter(requireContext(), messageList)
        chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        chatRecyclerView.adapter = messageAdapter

        // Initialise pull message
        pullMessage(myOppRef.child("Message"), messageList, messageAdapter)

        messageListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val tempList: MutableList<Message> = mutableListOf()

                // Add new version of message list
                for (childSnapshot in dataSnapshot.children) {
                    val message = childSnapshot.getValue(Message::class.java)
                    message?.let { tempList.add(it) }
                }

                for (i in messageList.size until tempList.size) {
                    messageList.add(tempList[i])
                    if (tempList[i].senderId != myUid) {
                        context?.let { createNotification(it.applicationContext) }
                    }
                }

                messageAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }

        // Add listener to message
        myMessageRef.addValueEventListener(messageListener)

        sendButton.setOnClickListener {

            // Add new message to origin list
            val newMessage = Message(messageBox.text.toString(), myUid)
            println("Time: " + newMessage.sentTime)

            messageList.add(newMessage)
            messageAdapter.notifyDataSetChanged()

            // set new message list
            myMessageRef.setValue(messageList)
            oppMessageRef.setValue(messageList)

            messageBox.setText("")
        }

        return binding.root

    }

    fun createNotification(context: Context) {
        val channelId = "my_channel_id"
        val channelName = "My Channel"
        val channelDescription = "My Channel Description"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = channelDescription
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle("New Message")
            .setContentText("You have received a new message.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationId = 1
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        messageList.clear()
    }

    companion object {

    }
}