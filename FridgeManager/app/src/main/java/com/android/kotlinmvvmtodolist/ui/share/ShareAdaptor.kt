package com.android.kotlinmvvmtodolist.ui.share

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlinmvvmtodolist.databinding.ShareRowLayoutBinding
import com.android.kotlinmvvmtodolist.util.User


class ShareAdaptor: RecyclerView.Adapter<ShareAdaptor.ShareViewHolder>() {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShareViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ShareRowLayoutBinding.inflate(inflater, parent, false)
        return ShareViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShareViewHolder, position: Int) {
        holder.bind(contacts[position])
    }

    override fun getItemCount(): Int = contacts.size

    class ShareViewHolder(private val binding: ShareRowLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: User) {
            binding.contact = contact
            binding.executePendingBindings()
        }
    }
}
