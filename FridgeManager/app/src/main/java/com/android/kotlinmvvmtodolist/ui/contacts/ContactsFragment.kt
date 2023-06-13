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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.fetchContacts()
        viewModel.contactsLiveData.observe(viewLifecycleOwner) { contacts ->
            mAdapter.submitList(contacts)
        }
        viewModel.filteredContactsLiveData.observe(viewLifecycleOwner) { filteredContacts ->
            mAdapter.submitList(filteredContacts)
        }
    }


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

//        viewModel.filteredContactsLiveData.observe(viewLifecycleOwner) { filteredContacts ->
//            mAdapter.submitList(filteredContacts)
//        }

        // TODO: delete contact?
//        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
//            override fun onMove(
//                recyclerView: RecyclerView,
//                viewHolder: RecyclerView.ViewHolder,
//                target: RecyclerView.ViewHolder
//            ): Boolean {
//                return false
//            }
//
//            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                val position = viewHolder.adapterPosition
//                val contact = mAdapter.contacts[position]
//                viewModel.deleteContact(contact.userID, contact)
//                mAdapter.deleteContact(contact)
//                Snackbar.make(binding.root, "Deleted!", Snackbar.LENGTH_LONG).apply {
//                    setAction("Undo") {
//                        viewModel.addContact(contact.userID, contact)
//                        mAdapter.submitList(viewModel.filteredContactsLiveData.value ?: emptyList())
//                    }
//                    show()
//                }
//            }
//        }
//
//        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
//        itemTouchHelper.attachToRecyclerView(binding.recyclerContactsView)

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
