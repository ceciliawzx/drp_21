package com.android.kotlinmvvmtodolist.ui.contacts

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.android.kotlinmvvmtodolist.databinding.FragmentContactsBinding
import com.android.kotlinmvvmtodolist.util.User


class ContactsFragment: Fragment() {

    private lateinit var binding: FragmentContactsBinding // Replace with the actual binding class name

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactsBinding.inflate(inflater, container, false)

        return binding.root
    }

}