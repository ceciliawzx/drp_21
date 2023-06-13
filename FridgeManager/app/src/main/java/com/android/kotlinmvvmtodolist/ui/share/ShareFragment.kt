package com.android.kotlinmvvmtodolist.ui.share

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
import com.android.kotlinmvvmtodolist.R
import com.android.kotlinmvvmtodolist.databinding.FragmentShareBinding
import com.android.kotlinmvvmtodolist.util.User

class ShareFragment: Fragment() {

    private val viewModel: ShareViewModel by activityViewModels()
    lateinit var mAdapter: ShareAdaptor
    private val selectedContacts: MutableList<User> = mutableListOf()


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
            // TODO: Jump to the chat fragment
//            findNavController().navigate(R.id.)
            true
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

}