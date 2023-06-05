@file:Suppress("DEPRECATION")

package com.android.kotlinmvvmtodolist.ui.task

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlinmvvmtodolist.R
import com.android.kotlinmvvmtodolist.data.local.TaskEntry
import com.android.kotlinmvvmtodolist.databinding.FragmentTaskBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.google.zxing.Result
import com.google.zxing.ResultPoint
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView


@AndroidEntryPoint
class TaskFragment : Fragment() {

    private val viewModel: TaskViewModel by viewModels()
    private lateinit var mAdapter: TaskAdapter

    private var _binding: FragmentTaskBinding? = null
    private val binding get() = _binding!!
    private var savedInstanceState: Bundle? = null


    @SuppressLint("UnsafeRepeatOnLifecycleDetector")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        this.savedInstanceState = savedInstanceState

        _binding = FragmentTaskBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        mAdapter = TaskAdapter(TaskClickListener { taskEntry ->
            findNavController().navigate(TaskFragmentDirections.actionTaskFragmentToUpdateFragment(taskEntry))
        })

        lifecycleScope.launch{
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.getAllTasks.collect{ tasks ->
                    mAdapter.submitList(tasks)
                }
            }
        }

        binding.apply {
            recyclerView.adapter = mAdapter
            floatingActionButton.setOnClickListener {
                findNavController().navigate(R.id.action_taskFragment_to_addFragment)
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
                val taskEntry = mAdapter.currentList[position]
                viewModel.delete(taskEntry)

                // Define different actions depends on swipe direction
                when (direction) {
                    ItemTouchHelper.LEFT -> swipeLeftHelper(taskEntry)
                    ItemTouchHelper.RIGHT -> swipeRightHelper(taskEntry)
                }

            }

            private fun swipeLeftHelper(taskEntry: TaskEntry) {
                Snackbar.make(binding.root, "Deleted!", Snackbar.LENGTH_LONG).apply {
                    setAction("Undo"){
                        viewModel.insert(taskEntry)
                    }
                    show()
                }
            }

            private fun swipeRightHelper(taskEntry: TaskEntry) {
                Snackbar.make(binding.root, "Food Used!", Snackbar.LENGTH_LONG).apply {
//                    setActionTextColor(ContextCompat.getColor(context, R.color.white))
//                    view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
//                        .setTextColor(ContextCompat.getColor(context, R.color.white))
                    setAction("Undo"){
                        viewModel.insert(taskEntry)
                    }
                    show()
                }
            }

        }).attachToRecyclerView(binding.recyclerView)



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
        inflater.inflate(R.menu.task_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        val barcodeItem = menu.findItem(R.id.action_barcode)

        searchView.setOnQueryTextListener(object  : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
               if(newText != null){
                   runQuery(newText)
               }
                return true
            }
        })

        barcodeItem.setOnMenuItemClickListener {
            startBarcodeScanner(savedInstanceState)
            true
        }
    }

    private fun startBarcodeScanner(savedInstanceState: Bundle?) {

        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.setPrompt("Scan a barcode")
        integrator.setOrientationLocked(false)
        integrator.initiateScan()
    }

    fun runQuery(query: String){
        val searchQuery = "%$query%"
        viewModel.searchDatabase(searchQuery).observe(viewLifecycleOwner) { tasks ->
            mAdapter.submitList(tasks)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_priority -> {
                lifecycleScope.launch{
                    repeatOnLifecycle(Lifecycle.State.STARTED){
                        viewModel.getAllPriorityTasks.collectLatest { tasks ->
                            mAdapter.submitList(tasks)
                        }
                    }
                }
            }
            R.id.action_delete_all -> deleteAllItem()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteAllItem() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete All")
            .setMessage("Are you sure?")
            .setPositiveButton("Yes"){dialog, _ ->
                viewModel.deleteAll()
                dialog.dismiss()
            }.setNegativeButton("No"){dialog, _ ->
                dialog.dismiss()
            }.create().show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            val scannedBarcode = result.contents
            processScannedBarcode(scannedBarcode)
            // Process the scanned barcode as needed
            // You can use the scannedBarcode here
        }
    }

    private fun processScannedBarcode(scannedBarcode: String) {
        // Implement the logic to extract expiration date, food name, and amount
        val expirationDate = extractExpirationDate(scannedBarcode)
        val foodName = extractFoodName(scannedBarcode)
        val amount = extractAmount(scannedBarcode)

        // Show the extracted information to the user (e.g., using Toast or a TextView)
//        val extractedInfo = "Expiration Date: $expirationDate\nFood Name: $foodName\nAmount: $amount"
        // TODO: Implement this function to extract information, and write to database
        val extractedInfo = scannedBarcode
        Toast.makeText(requireContext(), extractedInfo, Toast.LENGTH_SHORT).show()
    }

    private fun extractExpirationDate(scannedBarcode: String): String {
        // Example: Assume the expiration date is in the format "EXP: 2023-06-30"
        val regex = Regex("""EXP: (\d{4}-\d{2}-\d{2})""")
        val matchResult = regex.find(scannedBarcode)
        return matchResult?.groupValues?.get(1) ?: ""
    }

    private fun extractFoodName(scannedBarcode: String): String {
        // Example: Assume the food name is enclosed in square brackets: "[Food Name]"
        val regex = Regex("""\[(.*?)\]""")
        val matchResult = regex.find(scannedBarcode)
        return matchResult?.groupValues?.get(1) ?: ""
    }

    private fun extractAmount(scannedBarcode: String): String {
        // Example: Assume the amount is specified after "Amount: "
        val regex = Regex("""Amount: (\d+)""")
        val matchResult = regex.find(scannedBarcode)
        return matchResult?.groupValues?.get(1) ?: ""
    }


}