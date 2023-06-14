package com.android.kotlinmvvmtodolist.ui.chat


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.android.kotlinmvvmtodolist.databinding.FragmentChatBinding
import dagger.hilt.android.AndroidEntryPoint


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

        mAdapter = ChatAdapter(ChatAdapter.ChatClickListener { user ->
            findNavController().navigate(
                ChatFragmentDirections.actionChatFragmentToConversationFragment(
                    user.userID,
                    user.userName
                )
            )
        })

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

}
