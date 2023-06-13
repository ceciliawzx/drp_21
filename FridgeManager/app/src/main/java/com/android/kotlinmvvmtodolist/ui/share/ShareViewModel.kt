package com.android.kotlinmvvmtodolist.ui.share

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.kotlinmvvmtodolist.ui.contacts.ContactsAdapter
import com.android.kotlinmvvmtodolist.util.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class ShareViewModel @Inject constructor(
    private val database: DatabaseReference
) : ViewModel() {

    private val _contactsLiveData: MutableLiveData<List<User>> = MutableLiveData()
    val contactsLiveData: LiveData<List<User>> get() = _contactsLiveData
    val filteredContactsLiveData: MutableLiveData<List<User>> = MutableLiveData()
    private lateinit var mAdapter: ShareAdaptor

    // Set the adapter for the ViewModel
    fun setAdapter(adapter: ShareAdaptor) {
        mAdapter = adapter
    }

    fun fetchContacts() {
        val userID = FirebaseAuth.getInstance().currentUser?.uid
        if (userID != null) {
            val contactsRef = database.child("User").child(userID).child("Contacts")
            contactsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val contacts: MutableList<User> = mutableListOf()
                    for (contactSnapshot in snapshot.children) {
                        val contactID = contactSnapshot.key
                        val contactName = contactSnapshot.child("userName").value.toString()
                        val contactProfileImage = contactSnapshot.child("profileImage").value.toString()
                        val contact = contactID?.let { User(it, contactName, contactProfileImage) }
                        if (contact != null) {
                            contacts.add(contact)
                        }
                    }
                    _contactsLiveData.value = contacts
                    mAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

}
