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
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
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
import com.android.kotlinmvvmtodolist.data.local.TaskEntry
import com.android.kotlinmvvmtodolist.databinding.FragmentShoplistBinding
import com.android.kotlinmvvmtodolist.ui.add.calculateDaysLeft
import com.android.kotlinmvvmtodolist.ui.add.getNotificationTime
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.integration.android.IntentIntegrator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
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

        mAdapter = ShopItemAdapter(ShopItemClickListener {
//            findNavController().navigate(ShopListFragmentDirections.actionShopListFragmentToAddFragment())
        })

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

        ItemTouchHelper(object  : ItemTouchHelper.SimpleCallback(0,
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



        setHasOptionsMenu(true)

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

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
//        inflater.inflate(R.menu.task_menu, menu)
//
//        val searchItem = menu.findItem(R.id.action_search)
//        val searchView = searchItem.actionView as SearchView
//        val barcodeItem = menu.findItem(R.id.action_barcode)
//
//        searchView.setOnQueryTextListener(object  : SearchView.OnQueryTextListener{
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                return true
//            }

//            override fun onQueryTextChange(newText: String?): Boolean {
//                if(newText != null){
//                    runQuery(newText)
//                }
//                return true
//            }
//        })
//
//        barcodeItem.setOnMenuItemClickListener {
//            startBarcodeScanner(savedInstanceState)
//            true
//        }
    }

    private fun startBarcodeScanner(savedInstanceState: Bundle?) {

        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.setPrompt("Scan a barcode")
        integrator.setOrientationLocked(false)
        integrator.initiateScan()
    }
//
//    fun runQuery(query: String){
//        val searchQuery = "%$query%"
//        viewModel.searchDatabase(searchQuery).observe(viewLifecycleOwner) { tasks ->
//            mAdapter.submitList(tasks)
//        }
//    }

//    @Deprecated("Deprecated in Java")
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when(item.itemId){
//            R.id.action_priority -> {
//                lifecycleScope.launch{
//                    repeatOnLifecycle(Lifecycle.State.STARTED){
//                        viewModel.getAllPriorityTasks.collectLatest { tasks ->
//                            mAdapter.submitList(tasks)
//                        }
//                    }
//                }
//            }
//            R.id.action_delete_all -> deleteAllItem()
//        }
//        return super.onOptionsItemSelected(item)
//    }

//    private fun deleteAllItem() {
//        AlertDialog.Builder(requireContext())
//            .setTitle("Delete All")
//            .setMessage("Are you sure?")
//            .setPositiveButton("Yes"){dialog, _ ->
//                viewModel.deleteAll()
//                dialog.dismiss()
//            }.setNegativeButton("No"){dialog, _ ->
//                dialog.dismiss()
//            }.create().show()
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            val scannedBarcode = result.contents
            // Process the scanned barcode
            processScannedBarcode(scannedBarcode)
        }
    }

    private fun processScannedBarcode(scannedBarcode: String) {
        // TODO: need a database, probably cannot implement before ddl :(
    }
}