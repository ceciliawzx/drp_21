package com.android.kotlinmvvmtodolist.ui.contacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

    val contactsLiveData: LiveData<List<Contact>> = MutableLiveData()

    init {
        fetchContacts()
    }

    private fun fetchContacts() {
        database.child("contacts").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val contacts: MutableList<Contact> = mutableListOf()
                for (contactSnapshot in snapshot.children) {
                    val contact = contactSnapshot.getValue(Contact::class.java)
                    contact?.let { contacts.add(it) }
                }
                (contactsLiveData as MutableLiveData).value = contacts
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    fun addContact(userID: String, contact: Contact) {
        val contactsRef = database.child("User").child(userID).child("Contacts")
        val newContactRef = contactsRef.push()
        contact.userID?.let {
            newContactRef.child(it).setValue(contact)
        }
    }


    fun updateContact(userID: String, contact: Contact) {
        contact.userID?.let {
            database.child("User").child(userID).child("Contacts").child(it).setValue(contact)
        }
    }

    fun deleteContact(userID: String, contact: Contact) {
        contact.userID?.let {
            database.child("User").child(userID).child("Contacts").child(it).removeValue()
        }
    }
}
