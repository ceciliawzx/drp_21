package com.android.kotlinmvvmtodolist.ui.shopList


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlinmvvmtodolist.data.local.ShopItemEntry
import com.android.kotlinmvvmtodolist.databinding.ShoplistRowLayoutBinding

class ShopItemAdapter(private val clickListener: ShopItemClickListener):
    ListAdapter<ShopItemEntry, ShopItemAdapter.ViewHolder>(ShopItemDiffCallback) {

    companion object ShopItemDiffCallback : DiffUtil.ItemCallback<ShopItemEntry>(){
        override fun areItemsTheSame(oldItem: ShopItemEntry, newItem: ShopItemEntry) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ShopItemEntry, newItem: ShopItemEntry) = oldItem == newItem
    }

    class ViewHolder(private val binding: ShoplistRowLayoutBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(shopItemEntry: ShopItemEntry, clickListener: ShopItemClickListener){
            binding.shopItemEntry = shopItemEntry
            binding.shopClickListener = clickListener
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ShoplistRowLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val current = getItem(position)
        if(current != null){
            holder.bind(current, clickListener)
        }
    }
}

class ShopItemClickListener(val clickListener: (shopItemEntry: ShopItemEntry) -> Unit){
    fun onClick(shopItemEntry: ShopItemEntry) = clickListener(shopItemEntry)
}