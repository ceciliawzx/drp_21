package com.android.kotlinmvvmtodolist.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlinmvvmtodolist.databinding.ChatRowLayoutBinding
import com.android.kotlinmvvmtodolist.databinding.ContactsRowLayoutBinding
import com.android.kotlinmvvmtodolist.util.User

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val contacts: MutableList<User> = mutableListOf()

    fun submitList(newContacts: List<User>) {
        contacts.clear()
        contacts.addAll(newContacts)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ChatRowLayoutBinding.inflate(inflater, parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(contacts[position])
    }

    override fun getItemCount(): Int = contacts.size

    class ChatViewHolder(private val binding: ChatRowLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: User) {
            binding.contact = contact
            binding.executePendingBindings()
        }
    }
}
