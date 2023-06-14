package com.android.kotlinmvvmtodolist.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlinmvvmtodolist.data.local.TaskEntry
import com.android.kotlinmvvmtodolist.databinding.ChatRowLayoutBinding
import com.android.kotlinmvvmtodolist.ui.task.TaskClickListener
import com.android.kotlinmvvmtodolist.util.User

class ChatAdapter(private val clickListener: ChatClickListener) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

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
        holder.bind(contacts[position], clickListener)
    }


    override fun getItemCount(): Int = contacts.size

    class ChatViewHolder(private val binding: ChatRowLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: User, clickListener: ChatClickListener) {
            binding.contact = contact
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
    }

    class ChatClickListener(val clickListener: (contact: User) -> Unit) {
        fun onClick(contact: User) = clickListener(contact)
    }
}
