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
import com.google.firebase.database.ChildEventListener
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
    private lateinit var timeStampListener: ValueEventListener
    private lateinit var messageListener: ChildEventListener
    private lateinit var myTimeStampRef: DatabaseReference
    private lateinit var myMessageRef: DatabaseReference
    private lateinit var oppMessageRef: DatabaseReference

    var latestTimestamp: Long = 0
    var firstIn = true

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
        myTimeStampRef = myOppRef.child("latestTimestamp")

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
        messageAdapter.notifyDataSetChanged()

        // Retrieve Timestamp
        timeStampListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                latestTimestamp = dataSnapshot.getValue(Long::class.java) ?: 0
                println("timestamp: " + latestTimestamp)
                messageAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }

        messageListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val message = dataSnapshot.getValue(Message::class.java)

                if (message != null && message.senderId != myUid) {
                    if (!firstIn) {
                        messageList.add(message)
                        messageAdapter.notifyDataSetChanged()
                    } else {
                        firstIn = false
                    }

                }

                // Update the latest timestamp
                latestTimestamp = message?.timestamp ?: latestTimestamp

            }

            override fun onChildChanged(
                dataSnapshot: DataSnapshot,
                previousChildName: String?
            ) {
                // Handle the case if a child message is changed (optional)
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                // Handle the case if a child message is removed (optional)
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // Handle the case if a child message is moved (optional)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error
            }
        }

        // Bind listener
        myTimeStampRef.addListenerForSingleValueEvent(timeStampListener)

        // Retrieve messages with a greater timestamp
        myMessageRef.orderByChild("timestamp")
            .startAt(latestTimestamp.toDouble())
            .addChildEventListener(messageListener)

        sendButton.setOnClickListener {

            // Add new message to origin list
            val newMessage = Message(messageBox.text.toString(), myUid)
            messageList.add(newMessage)
            messageAdapter.notifyDataSetChanged()

            // set new message list
            myMessageRef.setValue(messageList)
            oppMessageRef.setValue(messageList)

            messageBox.setText("")
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        myTimeStampRef.removeEventListener(timeStampListener)
        myMessageRef.removeEventListener(messageListener)
        messageList.clear()
    }

    companion object {

    }
}