package com.android.kotlinmvvmtodolist.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.android.kotlinmvvmtodolist.R
import com.android.kotlinmvvmtodolist.ui.chat.ChatFragmentDirections
import com.android.kotlinmvvmtodolist.ui.chat.ChatUtil.pullMessage
import com.android.kotlinmvvmtodolist.ui.chat.Message
import com.android.kotlinmvvmtodolist.ui.profile.ProfileFragment
import com.android.kotlinmvvmtodolist.ui.profile.ProfileFragmentDirections
import com.android.kotlinmvvmtodolist.ui.task.TaskViewModel
import com.android.kotlinmvvmtodolist.util.Constants
import com.android.kotlinmvvmtodolist.util.Constants.USER_DATABASE_REFERENCE
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import dagger.hilt.android.AndroidEntryPoint
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomNavigationView: BottomNavigationView
    private var activeFragment: Fragment? = null
    private lateinit var messageListener: ValueEventListener
    private var notificationID = 1

    private var messageMap = hashMapOf<String, Int>()
    private var firstIn = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.title = getString(R.string.storage)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navController =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
        bottomNavigationView = findViewById(R.id.bottom_bar)

        val myUid = FirebaseAuth.getInstance().currentUser?.uid!!
        val myRef = Constants.USER_DATABASE_REFERENCE
            .child("User").child(myUid)
            .child("Contacts")

        messageListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (contact in dataSnapshot.children) {
                    val messageList = mutableListOf<Message>()
                    pullMessage(myRef.child(contact.key!!).child("Message"), messageList, null)

                    // size not same, and other's send, update on this channel
                    if (messageList.isNotEmpty()) {
                        if (firstIn) {
                            messageMap[contact.key!!] = messageList.size
                        } else if (messageList.last().senderId != myUid &&
                            messageList.size != messageMap.get(contact.key!!)
                        ) {
                            messageMap[contact.key!!] = messageList.size
                            createNotification(baseContext, contact.key!!)
                        }
                    }
                }
                firstIn = false
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }

        myRef.addValueEventListener(messageListener)

        var userName = ""
        var profileImage = ""
        val databaseReference = USER_DATABASE_REFERENCE

        // Retrieve userName
        val userRef = databaseReference.child("User").child(myUid).child("userName")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    userName = dataSnapshot.value.toString()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the onCancelled event if needed
            }
        })

        // Retrieve profileImage
        databaseReference.child("User").child(myUid).child("profileImage")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    profileImage = dataSnapshot.value.toString()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle any errors
                }
            })


        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_storage -> {
                    supportActionBar?.title = getString(R.string.storage)
                    navController.navigate(R.id.taskFragment)
                    true
                }

                R.id.action_shopping_cart -> {
                    supportActionBar?.title = getString(R.string.shopping_list)
                    navController.navigate(R.id.shopListFragment)
                    true
                }

                R.id.action_contacts -> {
                    supportActionBar?.title = getString(R.string.contacts)
                    navController.navigate(R.id.contactsFragment)
                    true
                }

                R.id.action_profile -> {
                    supportActionBar?.title = getString(R.string.profile)

                    val bundle = Bundle().apply {
                        putString("userName", userName)
                        putString("profileImage", profileImage)
                    }

                    navController.navigate(R.id.profileFragment, bundle)

                    true
                }

                else -> false
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Update the title based on the destination fragment
            when (destination.id) {
                R.id.taskFragment -> {
                    supportActionBar?.title = getString(R.string.storage)
                }

                R.id.addFragment -> {
                    supportActionBar?.title = getString(R.string.add)
                }

                R.id.updateFragment -> {
                    supportActionBar?.title = getString(R.string.update)
                }

                R.id.shopListFragment -> {
                    supportActionBar?.title = getString(R.string.shopping_list)
                }

                R.id.addItemFragment -> {
                    supportActionBar?.title = getString(R.string.add)
                }

                R.id.contactsFragment -> {
                    supportActionBar?.title = getString(R.string.contacts)
                }

                R.id.addContactFragment -> {
                    supportActionBar?.title = getString(R.string.add_contact)
                }

                R.id.shareFragment -> {
                    supportActionBar?.title = getString(R.string.share)
                }

                R.id.requestFragment -> {
                    supportActionBar?.title = getString(R.string.request)
                }

                R.id.chatFragment -> {
                    supportActionBar?.title = getString(R.string.chat)
                }

                R.id.profileFragment -> {
                    supportActionBar?.title = getString(R.string.profile)
                }

                R.id.conversationFragment -> {
                    supportActionBar?.title = getString(R.string.profile)
                }

                else -> {
                    // Handle other fragments if needed
                }
            }
        }
        handleIntentAction(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntentAction(intent)
    }

    private fun handleIntentAction(intent: Intent?) {
        val fragmentId = intent?.getIntExtra("fragmentId", 0)
        if (fragmentId != 0) {
            // Navigate to the desired fragment based on fragmentId
                // Replace the following line with your actual navigation logic
            when (fragmentId) {
                R.id.taskFragment -> {
                    val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_bar)
                    bottomNavigationView.menu.findItem(R.id.action_storage).isChecked = true
                    navController.navigate(fragmentId)
                }
                R.id.conversationFragment -> {
                    val uid = intent.getStringExtra("uid")
                    val userName = intent.getStringExtra("userName")
                    val bundle = Bundle().apply {
                        putString("uid", uid)
                        putString("userName", userName)
                    }
                    navController.navigate(fragmentId, bundle)
                    bottomNavigationView.menu.findItem(R.id.action_profile).isChecked = true
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        activeFragment?.onCreateOptionsMenu(menu, menuInflater) ?: super.onCreateOptionsMenu(
            menu
        )
        return true
    }

    fun createNotification(context: Context, senderID: String) {
        val channelId = "my_channel_id"
        val channelName = "My Channel"
        val channelDescription = "My Channel Description"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = channelDescription
        }

        val userTask =
            USER_DATABASE_REFERENCE.child("User").child(senderID).child("userName").get()
        while (!userTask.isComplete) {
        }
        val userName = userTask.result.value.toString()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        notificationIntent.putExtra("fragmentId", R.id.conversationFragment)
        notificationIntent.putExtra("uid", senderID)
        notificationIntent.putExtra("userName", userName)

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationID,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle("New Message")
            .setContentText("You have received a new message from " + userName + ".")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationId = notificationID
        notificationID++
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}