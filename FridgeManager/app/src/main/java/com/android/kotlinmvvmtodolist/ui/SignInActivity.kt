package com.android.kotlinmvvmtodolist.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.kotlinmvvmtodolist.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.view.MenuItem
import com.android.kotlinmvvmtodolist.R

class SignInActivity: AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignInBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        setContentView(binding.root)

        // Check if the user is already logged in
        if (firebaseAuth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.goToSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.btnSignIn.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val password = binding.passET.text.toString()


            if (email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val editor = sharedPreferences.edit()
                            editor.putBoolean("isLoggedIn", true)
                            editor.apply()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
//                            finish()
                        } else {
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }

            } else {
                Toast.makeText(this, "Password not matching!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logOut() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", false)
        editor.apply()
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.action_log_out -> {
//                logOut()
//                val intent = Intent(this, SignInActivity::class.java)
//                startActivity(intent)
//                finish()
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

}