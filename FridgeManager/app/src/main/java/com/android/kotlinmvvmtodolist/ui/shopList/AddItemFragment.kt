package com.android.kotlinmvvmtodolist.ui.shopList

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.kotlinmvvmtodolist.R
import com.android.kotlinmvvmtodolist.data.local.ShopItemEntry
import com.android.kotlinmvvmtodolist.databinding.FragmentAddItemBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddItemFragment : Fragment() {

    private val viewModel: ShopListViewModel by viewModels()
    private var _binding: FragmentAddItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAddItemBinding.inflate(inflater, container, false)

        val shopListAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            resources.getStringArray(R.array.priorities)
        )

        // 给view绑定数据
        binding.apply {
            shopListSpinner.adapter = shopListAdapter

            // Limits check
            shopListBtnAdd.setOnClickListener {

                if (TextUtils.isEmpty((shopListItemName.text))) {
                    Toast.makeText(requireContext(), "Please enter item name!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val titleTitle = shopListItemName.text.toString()
                val type = shopListSpinner.selectedItemPosition

                val shopItemEntry = ShopItemEntry(
                    0,
                    titleTitle,
                    type,
                    System.currentTimeMillis(),
                    0,
                    0
                )

                viewModel.insert(shopItemEntry)

                Toast.makeText(requireContext(), "Successfully added!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_addItemFragment_to_shopListFragment)
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
