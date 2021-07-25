package com.example.androidandfirestore.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.androidandfirestore.R
import com.example.androidandfirestore.databinding.ActivityMainBinding
import com.example.androidandfirestore.model.User
import com.example.androidandfirestore.network.Callback
import com.example.androidandfirestore.network.FirestoreService
import com.example.androidandfirestore.network.USER_COLLECTION_NAME
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.lang.Exception

const val USER_KEY= "username_key"

class SigningActivity : AppCompatActivity() {

    private val TAG = "SigningActivity"
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var binding: ActivityMainBinding
    lateinit var db : FirestoreService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        db = FirestoreService(FirebaseFirestore.getInstance())
        
    }
    

    fun onStartClicked(view :View){
        view.isEnabled = false
        auth.signInAnonymously().addOnCompleteListener(this){ task ->
            if(task.isSuccessful){ 
                var username = binding.editSU.text.toString()
                db.findUserById(username,object : Callback<User>{
                    override fun onSuccess(result: User?) {
                        if (result == null){
                            val user = User()
                            user.username = username
                            saveUserAndStartMainActivity(user, view)
                        }else{
                            startHomeActivity(username)
                        }
                    }

                    override fun onFailed(exception: Exception) {
                        showErrorMessage(view)
                        view.isEnabled = true
                    }
                })
            }else{
                showErrorMessage(view)
                view.isEnabled = true
        }
        }
        
    }

    private fun saveUserAndStartMainActivity(user: User, view: View) {
        db.setDocument(user, USER_COLLECTION_NAME,user.username,object :Callback<Void>{
            override fun onSuccess(result: Void?) {
                startHomeActivity(user.username)
            }

            override fun onFailed(exception: Exception) {
                showErrorMessage(view)
                view.isEnabled = true
            }
        })
    }

    private fun showErrorMessage(view: View){
        Snackbar.make(view,"Error while connecting to the server!!!",Snackbar.LENGTH_SHORT).setAction("info",null)
            .show()
    }

    private fun startHomeActivity(username : String) {

        val intent = Intent(this@SigningActivity,HomeActivity::class.java)
        intent.putExtra(USER_KEY,username)
        startActivity(intent)
        finish()

    }

}