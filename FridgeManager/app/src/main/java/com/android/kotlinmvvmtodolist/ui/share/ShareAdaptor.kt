package com.android.kotlinmvvmtodolist.ui.share

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlinmvvmtodolist.databinding.ShareRowLayoutBinding
import com.android.kotlinmvvmtodolist.util.User


class ShareAdaptor(private val shareFragment: ShareFragment):
    RecyclerView.Adapter<ShareAdaptor.ShareViewHolder>() {

    val selectedContactsMap: MutableMap<User, Boolean> = mutableMapOf()

    private var contacts: List<User> = emptyList()

    fun submitList(newContacts: List<User>) {
        selectedContactsMap.clear()
        for (contact in newContacts) {
            selectedContactsMap[contact] = false
        }
        contacts = newContacts
    }

//    fun toggleContactSelection(contact: User) {
//        val isSelected = selectedContactsMap[contact] ?: false
//        selectedContactsMap[contact] = !isSelected
//        notifyDataSetChanged()
//        shareFragment.updateSelectedContacts()
//    }

    fun toggleContactSelection(contact: User) {
        val isSelected = selectedContactsMap[contact] ?: false
        selectedContactsMap[contact] = !isSelected
        shareFragment.updateSelectedContacts()

        // Post a task to be run after the RecyclerView has finished computing the layout
        shareFragment.binding.recyclerShareView.post {
            notifyDataSetChanged()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShareViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ShareRowLayoutBinding.inflate(inflater, parent, false)
        return ShareViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShareViewHolder, position: Int) {
        val contact = contacts[position]
        holder.bind(contact, selectedContactsMap[contact] ?: false)
    }

    override fun getItemCount(): Int = contacts.size

    inner class ShareViewHolder(private val binding: ShareRowLayoutBinding):
        RecyclerView.ViewHolder(binding.root) {

        private val shareCheckBox = binding.shareCheckBox

        fun bind(contact: User, isSelected: Boolean) {
            binding.contact = contact
            binding.executePendingBindings()

            shareCheckBox.isChecked = isSelected
            shareCheckBox.setOnCheckedChangeListener { _, isChecked ->
                shareFragment.mAdapter.toggleContactSelection(contact)
            }
        }
    }
}
