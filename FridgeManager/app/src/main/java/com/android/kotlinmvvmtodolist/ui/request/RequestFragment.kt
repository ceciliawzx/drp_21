package com.android.kotlinmvvmtodolist.ui.request

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.kotlinmvvmtodolist.databinding.FragmentRequestBinding
import com.android.kotlinmvvmtodolist.ui.shopList.ShopItemClickListener
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RequestFragment : Fragment() {

    private var _binding: FragmentRequestBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRequestBinding.inflate(inflater, container, false)


        binding.apply {
            btnRequest.setOnClickListener {

                if (TextUtils.isEmpty((requestName.text))) {
                    Toast.makeText(requireContext(), "Please enter item name!", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }
                val requestName = requestName.text.toString()
                val action = RequestFragmentDirections.actionRequestFragmentToShareFragment(
                        "",
                        "",
                        -1,
                        "",
                        requestName
                )
                findNavController().navigate(action)
            }
            return binding.root
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
