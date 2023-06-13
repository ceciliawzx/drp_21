package com.android.kotlinmvvmtodolist.ui.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.databinding.DataBindingUtil.setContentView
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlinmvvmtodolist.R
import com.google.firebase.auth.FirebaseAuth
import com.android.kotlinmvvmtodolist.util.Constants.USER_DATABASE_REFERENCE

class ConversationFragment : Fragment() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageView
    private val args by navArgs<ConversationFragmentArgs>()
    private val oppUid = args.uid
    private val myUid = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chatRecyclerView = view?.findViewById(R.id.chat_view)!!
        messageBox = view?.findViewById(R.id.message_box)!!
        sendButton = view?.findViewById(R.id.send_button)!!

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val toReturn = inflater.inflate(R.layout.fragment_conversation, container, false)

        sendButton.setOnClickListener {
            if (myUid != null) {
                USER_DATABASE_REFERENCE
                    .child("User").child(myUid).child(oppUid).child("Message")
                    .setValue("Hi")
            }
        }

        return toReturn
    }

    companion object {

    }
}