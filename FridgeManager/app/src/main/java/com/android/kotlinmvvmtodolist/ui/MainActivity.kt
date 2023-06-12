package com.android.kotlinmvvmtodolist.ui

import android.os.Bundle
import android.view.Menu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.android.kotlinmvvmtodolist.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

//class TestUser(
//    val userName : String,
//    val friends : List<String>) {
//}

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomNavigationView: BottomNavigationView
    private var activeFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.title = getString(R.string.storage)

        // Firebase Database
//        val database = Firebase.database("https://drp21-def08-default-rtdb.europe-west1.firebasedatabase.app")
//        val myRef = database.reference
//
//        val testUser = TestUser("leoli", listOf("bob", "tim", "tom"))
//        myRef.child("TestUser").child("Leo").setValue(testUser)


        navController = findNavController(R.id.nav_host_fragment)
        bottomNavigationView = findViewById(R.id.bottom_bar)

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
                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        activeFragment?.onCreateOptionsMenu(menu, menuInflater) ?: super.onCreateOptionsMenu(menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}