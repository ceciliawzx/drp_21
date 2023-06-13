package com.android.kotlinmvvmtodolist.ui.share

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.android.kotlinmvvmtodolist.R
import com.android.kotlinmvvmtodolist.databinding.FragmentShareBinding

class ShareFragment: Fragment() {

    private val viewModel: ShareViewModel by activityViewModels()
    private lateinit var mAdapter: ShareAdaptor

    private var _binding: FragmentShareBinding? = null
    private val binding get() = _binding!!
    private var savedInstanceState: Bundle? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        this.savedInstanceState = savedInstanceState

        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_share, container, false)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        mAdapter = ShareAdaptor()
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

        val sharTo = menu.findItem(R.id.share_to)
        sharTo.setOnMenuItemClickListener {
            // TODO: Jump to the chat fragment
//            findNavController().navigate(R.id.)
            true
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchContacts()
    }

}