package com.android.kotlinmvvmtodolist.ui.shopList

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlinmvvmtodolist.R
import com.android.kotlinmvvmtodolist.data.local.ShopItemEntry
import com.android.kotlinmvvmtodolist.databinding.FragmentShoplistBinding
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.integration.android.IntentIntegrator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShopListFragment: Fragment() {

    private val viewModel: ShopListViewModel by viewModels()
    private lateinit var mAdapter: ShopItemAdapter

    private var _binding: FragmentShoplistBinding? = null
    private val binding get() = _binding!!
    private var savedInstanceState: Bundle? = null


    @SuppressLint("UnsafeRepeatOnLifecycleDetector")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        this.savedInstanceState = savedInstanceState

        _binding = FragmentShoplistBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        mAdapter = ShopItemAdapter(
            ShopItemClickListener { shopItemEntry ->


                val action = ShopListFragmentDirections.actionShopListFragmentToAddFragment(
                    shopItemEntry.title,
                    shopItemEntry.type
                )
                findNavController().navigate(action)

            }
        )

        lifecycleScope.launch{
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.getAllItems.collect{ items ->
                    mAdapter.submitList(items)
                }
            }
        }

        binding.apply {
            recyclerShoplistView.adapter = mAdapter
            floatingActionShopListButton.setOnClickListener {
                findNavController().navigate(R.id.action_shopListFragment_to_addItemFragment)
            }

        }

        ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val shopItemEntry = mAdapter.currentList[position]
                viewModel.delete(shopItemEntry)

                // Define different actions depends on swipe direction
                when (direction) {
                    ItemTouchHelper.LEFT -> swipeLeftHelper(shopItemEntry)
                    ItemTouchHelper.RIGHT -> swipeRightHelper(shopItemEntry)
                }

            }

            private fun swipeLeftHelper(shopItemEntry: ShopItemEntry) {
                Snackbar.make(binding.root, "Deleted!", Snackbar.LENGTH_LONG).apply {
                    setAction("Undo"){
                        viewModel.insert(shopItemEntry)
                    }
                    show()
                }
            }

            private fun swipeRightHelper(shopItemEntry: ShopItemEntry) {
                Snackbar.make(binding.root, "Food Used!", Snackbar.LENGTH_LONG).apply {
                    // TODO: define another action for right swipe?
//                    setActionTextColor(ContextCompat.getColor(context, R.color.white))
//                    view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
//                        .setTextColor(ContextCompat.getColor(context, R.color.white))
                    setAction("Undo"){
                        viewModel.insert(shopItemEntry)
                    }
                    show()
                }
            }

        }).attachToRecyclerView(binding.recyclerShoplistView)

        hideKeyboard(requireActivity())


        return binding.root
    }

    private fun hideKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusedView = activity.currentFocus
        currentFocusedView.let {
            inputMethodManager.hideSoftInputFromWindow(
                currentFocusedView?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}