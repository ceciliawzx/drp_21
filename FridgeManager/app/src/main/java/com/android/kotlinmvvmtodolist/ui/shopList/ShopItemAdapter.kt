package com.android.kotlinmvvmtodolist.ui.shopList

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlinmvvmtodolist.data.local.ShopItemEntry
import com.android.kotlinmvvmtodolist.databinding.ShoplistRowLayoutBinding

class ShopItemAdapter(
    private val clickListener: ShopItemClickListener,
    private val viewModel: ShopListViewModel):
    ListAdapter<ShopItemEntry, ShopItemAdapter.ViewHolder>(ShopItemDiffCallback) {

    companion object ShopItemDiffCallback : DiffUtil.ItemCallback<ShopItemEntry>(){
        override fun areItemsTheSame(oldItem: ShopItemEntry, newItem: ShopItemEntry) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ShopItemEntry, newItem: ShopItemEntry) = oldItem == newItem
    }

    class ViewHolder(private val binding: ShoplistRowLayoutBinding):
        RecyclerView.ViewHolder(binding.root) {
        val checkBox: CheckBox = binding.checkBox
        fun bind(shopItemEntry: ShopItemEntry, clickListener: ShopItemClickListener){
            binding.shopItemEntry = shopItemEntry
            binding.shopClickListener = clickListener
            binding.executePendingBindings()
            checkBox.isChecked = shopItemEntry.bought == 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ShoplistRowLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val shopItemEntry = getItem(position)

        holder.bind(shopItemEntry, clickListener)

        holder.checkBox.isChecked = shopItemEntry.bought == 1

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                clickListener.onCheck(shopItemEntry)
            } else {
                shopItemEntry.bought = 0
                viewModel.update(shopItemEntry)
            }
        }

        val current = getItem(position)
        if(current != null){
            holder.bind(current, clickListener)
        }

    }
}

class ShopItemClickListener(val clickListener: (shopItemEntry: ShopItemEntry) -> Unit) {
    fun onCheck(shopItemEntry: ShopItemEntry) {
        clickListener(shopItemEntry)
    }
}
