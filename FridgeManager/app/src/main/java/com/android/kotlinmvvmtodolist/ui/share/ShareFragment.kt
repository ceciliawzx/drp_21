package com.android.kotlinmvvmtodolist.ui.share

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.android.kotlinmvvmtodolist.R
import com.android.kotlinmvvmtodolist.databinding.FragmentShareBinding
import com.android.kotlinmvvmtodolist.ui.chat.ChatUtil.pullMessage
import com.android.kotlinmvvmtodolist.ui.chat.Message
import com.android.kotlinmvvmtodolist.ui.chat.MessageAdapter
import com.android.kotlinmvvmtodolist.util.Constants
import com.android.kotlinmvvmtodolist.util.User
import com.google.firebase.auth.FirebaseAuth

class ShareFragment: Fragment() {

    private val viewModel: ShareViewModel by activityViewModels()
    lateinit var mAdapter: ShareAdaptor
    private val selectedContacts: MutableList<User> = mutableListOf()

    private var _binding: FragmentShareBinding? = null
    val binding get() = _binding!!
    private var savedInstanceState: Bundle? = null
    private var message: String = ""
    private var isSharing: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val args = ShareFragmentArgs.fromBundle(requireArguments())
        // Generate message based on arguments
        val taskName = args.taskName
        val expireDate = args.expireDate
        val amount = args.amount
        val unit = args.unit

        val requestName = args.requestName

        // Sharing
        if (taskName != "") {
            message = "I have $amount $unit of $taskName which will expire on $expireDate, do you want?"
        }
        // Request
        else if (requestName != "") {
            isSharing = false
            message = "I need some $requestName, do you have?"
        }

        // TODO: send this message to all selected contacts, and jump back

        this.savedInstanceState = savedInstanceState

        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_share, container, false)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        mAdapter = ShareAdaptor(this)
        viewModel.setAdapter(mAdapter)

        binding.apply {
            recyclerShareView.adapter = mAdapter
        }

        viewModel.contactsLiveData.observe(viewLifecycleOwner) { contacts ->
            mAdapter.submitList(contacts)
        }
        viewModel.filteredContactsLiveData.observe(viewLifecycleOwner) { filteredContacts ->
            mAdapter.submitList(filteredContacts)
        }

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.share_menu, menu)

        val searchItem = menu.findItem(R.id.share_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    runQuery(newText)
                }
                return true
            }
        })

        val sharTo = menu.findItem(R.id.share_to)
        sharTo.setOnMenuItemClickListener {
            // alert the user of the message
            // TODO: send the message to all selected contacts
            showMessageAlert(message, requireContext())

            true
        }

        val sendShare = binding.btnSendShare
        sendShare.setOnClickListener {
            showMessageAlert(message, requireContext())
        }

    }


    fun runQuery(query: String) {
        viewModel.searchDatabase(query)
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchContacts()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.contactsLiveData.observe(viewLifecycleOwner) { contacts ->
            mAdapter.submitList(contacts)
            updateSelectedContacts()
        }
    }

    fun updateSelectedContacts() {
        selectedContacts.clear()
        for ((contact, isSelected) in mAdapter.selectedContactsMap) {
            if (isSelected) {
                selectedContacts.add(contact)
            }
        }
        Log.d("Sharing", "checked size = ${selectedContacts.size}")
        // TODO: Update your selected contacts list based on selectedContacts
    }

    private fun showMessageAlert(message: String, context: Context) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid!!

        // after alert, jump back to fragment
        AlertDialog.Builder(context)
            .setTitle("Send to friends")
            .setMessage(message)
            .setPositiveButton("Send") {  _, _ ->
                Log.d("Message", "contacts size = ${selectedContacts.size}")
                for (opp in selectedContacts) {
                    val messageList = mutableListOf<Message>()
                    val messageAdapter = MessageAdapter(requireContext(), messageList)

                    val myRef = Constants.USER_DATABASE_REFERENCE
                        .child("User").child(myUid)
                        .child("Contacts").child(opp.userID)
                        .child("Message")

                    val oppRef = Constants.USER_DATABASE_REFERENCE
                        .child("User").child(opp.userID)
                        .child("Contacts").child(myUid)
                        .child("Message")

                    // pull message
                    pullMessage(oppRef, messageList, messageAdapter)
                    Log.d("Message", "messageList size = ${messageList.size}")

                    // create new message
                    val newMessage = Message(message, myUid)
                    messageList.add(newMessage)

                    // set new message list
                    myRef.setValue(messageList)
                    oppRef.setValue(messageList)
                }
                if (isSharing) {
                    val action = ShareFragmentDirections.actionShareFragmentToTaskFragment()
                    findNavController().navigate(action)
                } else {
                    val action = ShareFragmentDirections.actionShareFragmentToContactsFragment()
                    findNavController().navigate(action)
                }
            }
            .show()
    }

}