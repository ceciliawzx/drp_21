package com.android.kotlinmvvmtodolist.ui.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlinmvvmtodolist.databinding.FragmentConversationBinding
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

    var receiverRoom: String? = null
    var senderRoom: String? = null

    private var _binding: FragmentConversationBinding? = null
    private val binding get() = _binding!!

    // Message retrieve
    private val messageList: MutableList<Message> = mutableListOf()
    private lateinit var messageListener: ValueEventListener
    private lateinit var myMessageRef: DatabaseReference
    private lateinit var oppMessageRef: DatabaseReference

    private var currentTimeStamp: Long = 0

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
        val temp = myOppRef.child("Message").get()
        while (!temp.isComplete) {
        }
        val dataSnapshot = temp.result
        for (childSnapshot in dataSnapshot.children) {
            val message = childSnapshot.getValue(Message::class.java)
            message?.let { messageList.add(it) }
        }
        if (!messageList.isEmpty()) {
            currentTimeStamp = messageList.last().timestamp
        }
        messageAdapter.notifyDataSetChanged()


        messageListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val tempList: MutableList<Message> = mutableListOf()

                // Add new version of message list
                for (childSnapshot in dataSnapshot.children) {
                    val message = childSnapshot.getValue(Message::class.java)
                    message?.let { tempList.add(it) }
                }

                tempList.forEach { message ->
                    if (message.timestamp > currentTimeStamp && message.senderId != myUid) {
                        messageList.add(message)
                    }
                }

                messageAdapter.notifyDataSetChanged()
                if (!messageList.isEmpty()) {
                    currentTimeStamp = messageList.last().timestamp
                }

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
            messageList.add(newMessage)
            messageAdapter.notifyDataSetChanged()

            // set new message list
            myMessageRef.setValue(messageList)
            oppMessageRef.setValue(messageList)

            currentTimeStamp = newMessage.timestamp

            messageBox.setText("")
        }

        return binding.root

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        messageList.clear()
    }

    companion object {

    }
}