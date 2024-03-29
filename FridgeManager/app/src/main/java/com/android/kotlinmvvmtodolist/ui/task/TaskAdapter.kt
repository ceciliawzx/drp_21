package com.android.kotlinmvvmtodolist.ui.task

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlinmvvmtodolist.data.local.ShopItemEntry
import com.android.kotlinmvvmtodolist.data.local.TaskEntry
import com.android.kotlinmvvmtodolist.databinding.RowLayoutBinding

class TaskAdapter(
    private val clickListener: TaskClickListener):
    ListAdapter<TaskEntry, TaskAdapter.ViewHolder>(TaskDiffCallback) {

    companion object TaskDiffCallback : DiffUtil.ItemCallback<TaskEntry>(){
        override fun areItemsTheSame(oldItem: TaskEntry, newItem: TaskEntry) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: TaskEntry, newItem: TaskEntry) = oldItem == newItem
    }

    class ViewHolder(private val binding: RowLayoutBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(taskEntry: TaskEntry, clickListener: TaskClickListener){
            binding.taskEntry = taskEntry
            binding.clickListener = clickListener
            binding.btnShare.setOnClickListener {
                clickListener.onShareClick(taskEntry)
            }
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(RowLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val current = getItem(position)
        if (current != null) {
            holder.bind(current, clickListener)
        }
    }
}

class TaskClickListener(
    val clickListener: (taskEntry: TaskEntry) -> Unit,
    val shareClickListener: (taskEntry: TaskEntry) -> Unit
){
    fun onClick(taskEntry: TaskEntry) = clickListener(taskEntry)
    fun onShareClick(taskEntry: TaskEntry) = shareClickListener(taskEntry)
}