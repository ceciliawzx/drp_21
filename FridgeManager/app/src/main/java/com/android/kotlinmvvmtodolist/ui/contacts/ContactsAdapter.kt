package com.android.kotlinmvvmtodolist.ui.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlinmvvmtodolist.databinding.ContactsRowLayoutBinding
import com.android.kotlinmvvmtodolist.ui.chat.ChatAdapter
import com.android.kotlinmvvmtodolist.util.User

class ContactsAdapter(private val clickListener: ChatAdapter.ChatClickListener) :
    RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    val contacts: MutableList<User> = mutableListOf()

    fun submitList(newContacts: List<User>) {
        contacts.clear()
        contacts.addAll(newContacts)
        notifyDataSetChanged()
    }

    fun deleteContact(contact: User) {
        val position = contacts.indexOf(contact)
        if (position != -1) {
            contacts.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ContactsRowLayoutBinding.inflate(inflater, parent, false)
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(contacts[position], clickListener)
    }

    override fun getItemCount(): Int = contacts.size

    class ContactViewHolder(private val binding: ContactsRowLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: User, clickListener: ChatAdapter.ChatClickListener) {
            binding.contact = contact
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
    }
}
