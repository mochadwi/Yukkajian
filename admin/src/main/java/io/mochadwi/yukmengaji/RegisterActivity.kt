package io.mochadwi.yukmengaji

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.mochadwi.yukmengaji.R

class RegisterActivity : AppCompatActivity() {

    private var UserEmail: EditText? = null
    private var UserPassword: EditText? = null
    private var UserConfirmPassword: EditText? = null
    private var CreateAccountButton: Button? = null
    private var LoginAccountButton: Button? = null

    private var loadingBar: ProgressDialog? = null

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val toolbar = findViewById<View>(R.id.register_toolbar) as Toolbar
        setSupportActionBar(toolbar)

        mAuth = FirebaseAuth.getInstance()
        UserEmail = findViewById<View>(R.id.register_email) as EditText
        UserPassword = findViewById<View>(R.id.register_password) as EditText
        UserConfirmPassword = findViewById<View>(R.id.register_confirm_password) as EditText

        LoginAccountButton = findViewById<View>(R.id.register_button_login) as Button


        LoginAccountButton!!.setOnClickListener { SendToLogin() }


        CreateAccountButton = findViewById<View>(R.id.register_create_account) as Button
        loadingBar = ProgressDialog(this)
        CreateAccountButton!!.setOnClickListener { CreateNewAccount() }
    }

    private fun SendToLogin() {

        val registerToLogin = Intent(this@RegisterActivity, LoginActivity::class.java)
        startActivity(registerToLogin)

    }

    override fun onStart() {
        super.onStart()
        val currentUser = mAuth!!.currentUser
        if (currentUser != null) {

            SendUserToSetupActivity()

        }
    }

    private fun CreateNewAccount() {

        val Email = UserEmail!!.text.toString()
        val Password = UserPassword!!.text.toString()
        val ConfirmPassword = UserConfirmPassword!!.text.toString()


        if (TextUtils.isEmpty(Email)) {

            Toast.makeText(this, "PLease write your email", Toast.LENGTH_SHORT).show()

        } else if (TextUtils.isEmpty(Password)) {

            Toast.makeText(this, "Please write your Password", Toast.LENGTH_SHORT).show()

        } else if (TextUtils.isEmpty(ConfirmPassword)) {

            Toast.makeText(this, "Please write confirm password", Toast.LENGTH_SHORT).show()
        } else if (Password != ConfirmPassword) {

            Toast.makeText(this, "You password do not same", Toast.LENGTH_SHORT).show()
        } else {

            loadingBar!!.setTitle("Creating New Account")
            loadingBar!!.setMessage("Please Wait Loading Acoount")
            loadingBar!!.show()
            loadingBar!!.setCanceledOnTouchOutside(true)

            mAuth!!.createUserWithEmailAndPassword(Email, Password).addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    SendUserToSetupActivity()

                    Toast.makeText(this@RegisterActivity, "success regiter", Toast.LENGTH_SHORT)
                        .show()
                    loadingBar!!.dismiss()

                } else {

                    val message = task.exception!!.message
                    Toast.makeText(this@RegisterActivity, "Error", Toast.LENGTH_SHORT).show()
                    loadingBar!!.dismiss()

                }
            }
        }
    }

    private fun SendUserToSetupActivity() {

        val setupIntent = Intent(this@RegisterActivity, SetupActivity::class.java)
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(setupIntent)
        finish()

    }
}
