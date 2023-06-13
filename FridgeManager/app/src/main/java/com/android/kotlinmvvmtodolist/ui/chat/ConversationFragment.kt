package com.android.kotlinmvvmtodolist.ui.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.navigation.fragment.navArgs
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

    private var _binding: FragmentConversationBinding? = null
    private val binding get() = _binding!!

    // Message retrieve
    private val messageList: MutableList<Message> = mutableListOf()
    private lateinit var messageListener: ValueEventListener
    private lateinit var messageRef: DatabaseReference

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

        chatRecyclerView = binding.chatView
        messageBox = binding.messageBox
        sendButton = binding.sendButton

        messageRef = USER_DATABASE_REFERENCE
            .child("User").child(myUid)
            .child("Contacts").child(oppUid)
            .child("Message")

        messageListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                // Clear to fetch
                messageList.clear()

                // Add new version of message list
                for (childSnapshot in dataSnapshot.children) {
                    val message = childSnapshot.getValue(Message::class.java)
                    message?.let { messageList.add(it) }
                }

                messageList.forEach { message ->
                    println(message.message)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }

        // Add listener to message
        messageRef.addValueEventListener(messageListener)


        sendButton.setOnClickListener {

            // Add new message to origin list
            val newMessage = Message(messageBox.text.toString(), myUid)
            messageList.add(newMessage)

            // set new message list
            USER_DATABASE_REFERENCE
                .child("User").child(myUid)
                .child("Contacts").child(oppUid)
                .child("Message").setValue(messageList)

            messageBox.setText("")
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        messageRef.removeEventListener(messageListener)
    }

    companion object {

    }
}