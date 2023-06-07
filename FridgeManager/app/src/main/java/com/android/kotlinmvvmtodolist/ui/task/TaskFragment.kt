@file:Suppress("DEPRECATION")

package com.android.kotlinmvvmtodolist.ui.task

import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
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
import com.android.kotlinmvvmtodolist.data.local.TaskEntry
import com.android.kotlinmvvmtodolist.databinding.FragmentTaskBinding
import com.android.kotlinmvvmtodolist.ui.add.calculateDaysLeft
import com.android.kotlinmvvmtodolist.ui.add.getNotificationTime
import com.android.kotlinmvvmtodolist.ui.add.scheduleNotification
import com.android.kotlinmvvmtodolist.util.TrustAllCerts
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.google.zxing.integration.android.IntentIntegrator
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
                    // TODO: define another action for right swipe?
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
            // Process the scanned barcode
            try {
                processScannedBarcode(scannedBarcode)
            } catch (_: Exception) {

            }
        }
    }

    private fun processScannedBarcode(scannedBarcode: String) {
        println(scannedBarcode)
        val apiUrl = "https://world.openfoodfacts.org/api/v0/product/$scannedBarcode.json"

        val request = Request.Builder()
            .url(apiUrl)
            .build()

        val client = OkHttpClient.Builder()
            .sslSocketFactory(TrustAllCerts.createSSLSocketFactory(), TrustAllCerts)
            .hostnameVerifier { _, _ -> true }
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle network failure or error
                Log.d("Requesting", "Request fails")
                e.printStackTrace()
                // Print the error message
                Log.d("Requesting", "Error: ${e.message}")
            }

            var fail = false

            override fun onResponse(call: Call, response: Response) {
                Log.d("Requesting", "Requesting http request")
                val responseBody = response.body?.string()
                // Process the response body
                if (response.isSuccessful && responseBody != null) {
                    // Parse the response JSON
                    val product = parseProductFromJson(responseBody)


//                    val failed = product?.getString("product not found") != null
                    // Retrieve the food name
                    val productName: String = try {
                        product?.getString("product_name") ?: ""
                    } catch (e: JSONException) {
                        ""
                    }

                    // Retrieve the serving quantity

                    val productAmount: Int = try {
                        product?.getString("product_quantity")?.toInt() ?: 1
                    } catch (e: JSONException) {
                        1
                    }

                    val productUnit: String = try {
                        product?.getString("quantity") ?: ""
                    } catch (e: JSONException) {
                        ""
                    }

                    val number = productUnit.split(Regex("\\D+"))
                    val unit = productUnit.split(Regex("\\d+")).get(0)

                    val expirationDateString: String = try {
                        product?.getString("expiration_date") ?: ""
                    } catch (e: JSONException) {
                        ""
                    }

                    val expirationDate: String
                    if (expirationDateString != "") {
                        Log.d("Requesting", "default date problem")
                        val day = expirationDateString.split(' ')[0]
                        val month: Int = when (expirationDateString.split(' ')[1]) {
                            "Jan" -> 1
                            "Feb" -> 2
                            "Mar" -> 3
                            "Apr" -> 4
                            "May" -> 5
                            "Jun" -> 6
                            "Jul" -> 7
                            "Aug" -> 8
                            "Sep" -> 9
                            "Oct" -> 10
                            "Nov" -> 11
                            "Dec" -> 12
                            else -> 0
                        }
                        val year = expirationDateString.split(' ')[2]
                        expirationDate = "$year-$month-$day"
                    } else {
                        val defaultDateString = LocalDate.now().plusDays(1)
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        expirationDate = defaultDateString.format(formatter)
                    }

                    val notificationID = viewModel.getNextNotificationID()

                    fail = product == null || productName == "" || expirationDate == ""

                    if (fail) {
                        Toast.makeText(requireContext(), "Scan failed", Toast.LENGTH_SHORT).show()
                        // TODO: handle the failure
                        Log.d("Requesting", "fail because of some contents")
                    } else {
                        Log.d("Requesting", "contents success, other reasons")
                            val taskEntry = TaskEntry(
                                0,
                                productName,
                                3, // TODO
                                System.currentTimeMillis(),
                                expirationDate,
                                productAmount,
                                0, // TODO
                                notificationID,
                                0
                            )

                            viewModel.insert(taskEntry)
                            Log.d("Requesting", "added success")

                            val notificationTime = getNotificationTime(expirationDate)
                            val daysLeft = calculateDaysLeft(expirationDate)
                            val title = "$productName expire soon"
                            // TODO: notify ? days before expiration
                            val message1 = "Your $productName will expire in $daysLeft days!"
                            val message = "Your $productName will expire tomorrow!!!"
                            scheduleNotification(requireContext(), title, message, notificationTime, notificationID)
                            Toast.makeText(requireContext(), "Successfully added!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d("Requesting", "http request is failed")
                    Toast.makeText(requireContext(), "Scan failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
        )
    }

    private fun parseProductFromJson(json: String): JSONObject? {
        return try {
            JSONObject(json).optJSONObject("product")
        } catch (e: Exception) {
            null
        }
    }
    
}