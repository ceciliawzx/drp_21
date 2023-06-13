package com.android.kotlinmvvmtodolist.ui.contacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.kotlinmvvmtodolist.util.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatVIewModel @Inject constructor(
    private val database: DatabaseReference
) : ViewModel() {

    val contactsLiveData: LiveData<List<User>> = MutableLiveData()

    init {
        fetchContacts()
    }

    private fun fetchContacts() {
        val userID = FirebaseAuth.getInstance().currentUser?.uid
        if (userID != null) {
            val contactsRef = database.child("User").child(userID).child("Contacts")
            contactsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val contacts: MutableList<User> = mutableListOf()
                    for (contactSnapshot in snapshot.children) {
                        val contactID = contactSnapshot.key // Get the contact ID from the snapshot key
                        val contactName = contactSnapshot.child("userName").value.toString()
                        val contactProfileImage = contactSnapshot.child("profileImage").value.toString()
                        val contact = contactID?.let { User(it, contactName, contactProfileImage) }
                        if (contact != null) {
                            contacts.add(contact)
                        }
                    }
                    (contactsLiveData as MutableLiveData).value = contacts
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }



    fun addContact(userID: String, contact: User) {
        val contactID = contact.userID
        val contactsRef = database.child("User").child(userID).child("Contacts")
        val newContactRef = contactsRef.child(contactID)
        newContactRef.setValue(contact)
    }



    fun updateContact(userID: String, contact: User) {
        contact.userID.let {
            database.child("User").child(userID).child("Contacts").child(it).setValue(contact)
        }
    }

    fun deleteContact(userID: String, contact: User) {
        contact.userID.let {
            database.child("User").child(userID).child("Contacts").child(it).removeValue()
        }
    }

    fun fetchContact(userID: String, contactID: String) {
        val contactRef = database.child("User").child(contactID)
        contactRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val contactName = snapshot.child("userName").value.toString()
                val contactProfileImage = snapshot.child("profileImage").value.toString()
                val contact = User(contactID, contactName, contactProfileImage)
                addContact(userID, contact)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

}
