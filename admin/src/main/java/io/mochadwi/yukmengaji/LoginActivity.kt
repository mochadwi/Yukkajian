package io.mochadwi.yukmengaji

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.mochadwi.yukmengaji.R

class LoginActivity : AppCompatActivity() {

    private var LoginButton: Button? = null

    private var googleSignInButton: ImageView? = null

    private var UserEmail: EditText? = null
    private var UserPassword: EditText? = null
    private var NeedNewAccountLink: TextView? = null
    private var ForgetPasswordLink: TextView? = null
    private var loadingBar: ProgressDialog? = null

    private var mAuth: FirebaseAuth? = null
    private var mGoogleSignInClient: GoogleApiClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        NeedNewAccountLink = findViewById<View>(R.id.register_account_link) as TextView
        UserEmail = findViewById<View>(R.id.login_email) as EditText
        UserPassword = findViewById<View>(R.id.login_password) as EditText

        googleSignInButton = findViewById<View>(R.id.google_signin_button) as ImageView

        LoginButton = findViewById<View>(R.id.login_button) as Button
        ForgetPasswordLink = findViewById<View>(R.id.forget_password_link) as TextView
        loadingBar = ProgressDialog(this)

        mAuth = FirebaseAuth.getInstance()

        NeedNewAccountLink!!.setOnClickListener { SendUserToLogin() }

        ForgetPasswordLink!!.setOnClickListener {
            startActivity(Intent(this@LoginActivity, ForgotPasswordActivity::class.java))
        }

        LoginButton!!.setOnClickListener { AllowingToUserLogin() }
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

        googleSignInButton!!.setOnClickListener { signIn() }
    }

    private fun signIn() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
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

    private fun AllowingToUserLogin() {

        val Email = UserEmail!!.text.toString()
        val Password = UserPassword!!.text.toString()

        if (TextUtils.isEmpty(Email)) {

            Toast.makeText(this, "Please Write Loading ", Toast.LENGTH_SHORT).show()

        } else if (TextUtils.isEmpty(Password)) {

            Toast.makeText(this, "Please Write your P", Toast.LENGTH_SHORT).show()
        } else {

            loadingBar!!.setTitle("Login")
            loadingBar!!.setMessage("Please Wait Login")
            loadingBar!!.setCanceledOnTouchOutside(true)
            loadingBar!!.show()

            mAuth!!.signInWithEmailAndPassword(Email, Password).addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    SendUserToMainActivity()
                    Toast.makeText(this@LoginActivity, "Logging Susscefully", Toast.LENGTH_SHORT)
                        .show()
                    loadingBar!!.dismiss()

                } else {

                    val message = task.exception!!.message
                    Toast.makeText(this@LoginActivity, "Error", Toast.LENGTH_SHORT).show()
                    loadingBar!!.dismiss()
                }
            }
        }

    }

    private fun SendUserToMainActivity() {

        val mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()

    }

    private fun SendUserToLogin() {

        val registerIntent = Intent(this@LoginActivity, RegisterActivity::class.java)
        startActivity(registerIntent)

    }

    private fun SendUserToLoginActivity() {

        val mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()

    }

    companion object {

        private val RC_SIGN_IN = 1
        private val TAG = "LoginActivity"
    }
}
