package com.android.kotlinmvvmtodolist.ui.contacts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.android.kotlinmvvmtodolist.databinding.FragmentAddContactBinding
import com.android.kotlinmvvmtodolist.util.User
import com.google.firebase.auth.FirebaseAuth

class AddContactFragment : Fragment() {
    private var _binding: FragmentAddContactBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.saveButton.setOnClickListener {
            val contactId = binding.contactIdEditText.text.toString()
            val userID = FirebaseAuth.getInstance().currentUser?.uid
            if (userID != null) {
                Log.d("AddContactFragment", "userID = $userID")
                viewModel.fetchContact(userID, contactId)
            }
            navigateBackToContactsFragment()
        }
    }

    private fun navigateBackToContactsFragment() {
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
