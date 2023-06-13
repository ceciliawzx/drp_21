package com.android.kotlinmvvmtodolist.ui.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.android.kotlinmvvmtodolist.R
import com.android.kotlinmvvmtodolist.databinding.FragmentContactsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactsFragment : Fragment() {

    private val viewModel: ContactsViewModel by activityViewModels()
    private lateinit var mAdapter: ContactsAdapter

    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!
    private var savedInstanceState: Bundle? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        this.savedInstanceState = savedInstanceState

        _binding = FragmentContactsBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        mAdapter = ContactsAdapter()

        binding.apply {
            recyclerContactsView.adapter = mAdapter
        }

        viewModel.filteredContactsLiveData.observe(viewLifecycleOwner) { filteredContacts ->
            mAdapter.submitList(filteredContacts)
        }

        setHasOptionsMenu(true)

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.contacts_menu, menu)

        // TODO
        val searchItem = menu.findItem(R.id.contacts_search)
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

        val addContactItem = menu.findItem(R.id.contacts_add)
        addContactItem.setOnMenuItemClickListener {
            findNavController().navigate(R.id.action_contactsFragment_to_addContactFragment)
            true
        }
    }

    fun runQuery(query: String) {
        viewModel.searchDatabase(query)
    }

}
