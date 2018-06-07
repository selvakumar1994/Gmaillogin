package com.example.test.mysamplelogin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : AppCompatActivity() {

    //creating a GoogleSignInClient object
    internal lateinit var mGoogleSignInClient: GoogleSignInClient

    //And also a Firebase Auth object
    internal lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //first we intialized the FirebaseAuth object
        mAuth = FirebaseAuth.getInstance()

        //Then we need a GoogleSignInOptions object
        //And we need to build it as below
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        //Then we will get the GoogleSignInClient object from GoogleSignIn class
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        //Now we will attach a click listener to the sign_in_button
        //and inside onClick() method we are calling the signIn() method that will open
        //google sign in intent
        findViewById<View>(R.id.sign_in_button).setOnClickListener {
            signIn()
        }
    }

    override fun onStart() {
        super.onStart()

        //if the user is already signed in
        //we will close this activity
        //and take the user to profile activity
        if (mAuth.currentUser != null) {
            finish()
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        //if the requestCode is the Google Sign In code that we defined at starting
        if (requestCode == RC_SIGN_IN) {

            //Getting the GoogleSignIn Task
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                //Google Sign In was successful, authenticate with Firebase
                val account = task.getResult<ApiException>(ApiException::class.java)

                //authenticating with firebase
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)

        //getting the auth credential
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        println("check" + credential)
        //Now using firebase we are signing in the user here
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "signInWithCredential:success")
                        val user = mAuth.currentUser
                          println(user?.displayName)
                        Toast.makeText(this@MainActivity, "User Signed In", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, ProfileActivity::class.java))
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        Toast.makeText(this@MainActivity, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()

                    }

                    // ...
                }
    }


    //this method is called on click
    private fun signIn() {
        //getting the google signin intent
        val signInIntent = mGoogleSignInClient.signInIntent

        //starting the activity for result
        startActivityForResult(signInIntent, RC_SIGN_IN)
//        startActivity(Intent(this, ProfileActivity::class.java))
    }

    companion object {

        //a constant for detecting the login intent result
        private val RC_SIGN_IN = 234

        //Tag for the logs optional
        private val TAG = "simplifiedcoding"
    }
}