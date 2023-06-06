package com.android.kotlinmvvmtodolist.ui.shopList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kotlinmvvmtodolist.data.ShopItemRepository
import com.android.kotlinmvvmtodolist.data.local.ShopItemEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ShopListViewModel @Inject constructor(
    private val repository : ShopItemRepository
) : ViewModel(){

    val getAllItems = repository.getAllItems()

    fun insert(shopItemEntry: ShopItemEntry) = viewModelScope.launch {
        repository.insert(shopItemEntry)
    }

    fun delete(shopItemEntry: ShopItemEntry) = viewModelScope.launch{
        repository.deleteItem(shopItemEntry)
    }

    fun update(shopItemEntry: ShopItemEntry) = viewModelScope.launch{
        repository.updateData(shopItemEntry)
    }

    fun deleteAll() = viewModelScope.launch{
        repository.deleteAll()
    }

}