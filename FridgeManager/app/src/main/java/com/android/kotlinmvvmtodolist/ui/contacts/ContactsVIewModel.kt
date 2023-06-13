package com.android.kotlinmvvmtodolist.ui.contacts

import android.util.Log
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
class ContactsViewModel @Inject constructor(
    private val database: DatabaseReference
) : ViewModel() {

    val contactsLiveData: MutableLiveData<List<User>> = MutableLiveData()
    val filteredContactsLiveData: MutableLiveData<List<User>> = MutableLiveData()

    fun fetchContacts() {
        val userID = FirebaseAuth.getInstance().currentUser?.uid
        if (userID != null) {
            val contactsRef = database.child("User").child(userID).child("Contacts")
            contactsRef.addListenerForSingleValueEvent(object : ValueEventListener {
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
                    notifyContactsUpdated(contacts)
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
        val contactID = contact.userID
        val contactsRef = database.child("User").child(userID).child("Contacts")
        val contactRef = contactsRef.child(contactID)
        contactRef.removeValue()
    }

    fun fetchContact(userID: String, contactID: String) {
        val contactRef = database.child("User").child(contactID)
        contactRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Add friend to my contact
                val contactName = snapshot.child("userName").value.toString()
                val contactProfileImage = snapshot.child("profileImage").value.toString()
                val contact = User(contactID, contactName, contactProfileImage)
                addContact(userID, contact)

                // Add me to friend's contact
                val userRef = database.child("User").child(userID)
                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userName = snapshot.child("userName").value.toString()
                        val userProfileImage = snapshot.child("profileImage").value.toString()
                        val user = User(userID, userName, userProfileImage)
                        addContact(contactID, user)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                    }
                })
            }


            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    fun searchDatabase(query: String) {
        filterContacts(query)
    }

    private fun filterContacts(query: String) {
        val contacts = contactsLiveData.value
        if (contacts != null) {
            val filteredContacts = if (query.isNotEmpty()) {
                contacts.filter { user ->
                    user.userName.contains(query, ignoreCase = true)
                }
            } else {
                contacts
            }
            filteredContactsLiveData.value = filteredContacts
        }
    }

    private fun notifyContactsUpdated(contacts: List<User>) {
        contactsLiveData.postValue(contacts)
    }

}
