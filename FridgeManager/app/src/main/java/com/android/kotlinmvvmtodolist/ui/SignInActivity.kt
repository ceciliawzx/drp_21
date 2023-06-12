package com.android.kotlinmvvmtodolist.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.kotlinmvvmtodolist.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth

class SignInActivity: AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignInBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()

        setContentView(binding.root)


        binding.goToSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.btnSignIn.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val password = binding.passET.text.toString()


            if (email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()

                    }
                }

            } else {
                Toast.makeText(this, "Password not matching!", Toast.LENGTH_SHORT).show()
            }
        }

    }


}