package com.android.kotlinmvvmtodolist.ui.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.android.kotlinmvvmtodolist.R
import com.android.kotlinmvvmtodolist.databinding.FragmentChatBinding
import com.android.kotlinmvvmtodolist.databinding.FragmentContactsBinding
import com.android.kotlinmvvmtodolist.util.Constants.USER_DATABASE_REFERENCE
import com.android.kotlinmvvmtodolist.util.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private val viewModel: ChatVIewModel by activityViewModels()
    private lateinit var mAdapter: ChatAdapter

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private var savedInstanceState: Bundle? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        this.savedInstanceState = savedInstanceState

        _binding = FragmentChatBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        mAdapter = ChatAdapter()

        binding.apply {
            recyclerChatView.adapter = mAdapter
        }

        viewModel.contactsLiveData.observe(viewLifecycleOwner) { contacts ->
            mAdapter.submitList(contacts)
        }

        setHasOptionsMenu(true)

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.contacts_menu, menu)

        val addContactItem = menu.findItem(R.id.contacts_add)
        addContactItem.setOnMenuItemClickListener {
            findNavController().navigate(R.id.action_contactsFragment_to_addContactFragment)
            true
        }
    }

}
