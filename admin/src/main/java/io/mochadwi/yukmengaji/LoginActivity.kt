package io.mochadwi.yukmengaji

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private var loadingBar: ProgressDialog? = null

    private var mAuth: FirebaseAuth? = null
    private var mGoogleSignInClient: GoogleApiClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loadingBar = ProgressDialog(this)

        mAuth = FirebaseAuth.getInstance()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this) {
                Toast.makeText(this@LoginActivity, "Connection to Google Sign in Failed",
                    Toast.LENGTH_SHORT).show()
            }
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()

        google_signin_button.setOnClickListener { signIn() }
    }

    private fun signIn() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {

            loadingBar!!.setTitle("Google Sign in")
            loadingBar!!.setMessage(
                "Please wait, while we are allowing you to login using your Google Account...")
            loadingBar!!.setCanceledOnTouchOutside(true)
            loadingBar!!.show()

            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)

            if (result.isSuccess) {

                val account = result.signInAccount
                firebaseAuthWithGoogle(account!!)
                Toast.makeText(this, "Please wait, while we are getting yout auth result...",
                    Toast.LENGTH_SHORT).show()
            } else {

                Toast.makeText(this, "Can't get auth Result", Toast.LENGTH_SHORT).show()
                loadingBar!!.dismiss()
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    SendUserToMainActivity()
                    loadingBar!!.dismiss()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    val messsage = task.result.toString()
                    SendUserToLoginActivity()
                    Toast.makeText(this@LoginActivity, "Not Authentication, try again$messsage",
                        Toast.LENGTH_SHORT).show()
                    loadingBar!!.dismiss()
                }

                // ...
            }
    }

    override fun onStart() {
        super.onStart()

        val currentUser = mAuth!!.currentUser
        if (currentUser != null) {

            SendUserToMainActivity()
        }
    }

    private fun SendUserToMainActivity() {

        val mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }

    private fun SendUserToLoginActivity() {

        val mainIntent = Intent(this@LoginActivity, LoginActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }

    companion object {

        private val RC_SIGN_IN = 1
        private val TAG = "LoginActivity"
    }
}
