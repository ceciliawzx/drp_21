package com.android.kotlinmvvmtodolist.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.kotlinmvvmtodolist.R
import com.android.kotlinmvvmtodolist.databinding.ActivitySignUpBinding
import com.android.kotlinmvvmtodolist.util.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class SignUpActivity: AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.app_name)

        firebaseAuth = FirebaseAuth.getInstance()


        binding.goToSignIn.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
        binding.btnSignUp.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val userName = binding.userNameET.text.toString()
            val password = binding.passET.text.toString()
            val confirmPassword = binding.confirmPassEt.text.toString()

            if (email.isNotEmpty() && userName.isNotEmpty()
                && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    val registerUser = firebaseAuth.createUserWithEmailAndPassword(email, password)
                    registerUser.addOnCompleteListener {
                        if (it.isSuccessful) {
                            val userID = registerUser.result.user!!.uid
                            // Add to database
                            val database = Firebase.database("https://drp21-def08-default-rtdb.europe-west1.firebasedatabase.app")
                            val myRef = database.reference
                            val testUser = User(userID, userName, "", listOf("testUser1", "testUser2"))
                            myRef.child("User").child(userID).setValue(testUser)
                            val intent = Intent(this, SignInActivity::class.java)
                            startActivity(intent)
                        } else {
                            Log.d("Logging", "error: ${it.exception.toString()}")
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Password not matching!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show()
            }

        }
    }

}