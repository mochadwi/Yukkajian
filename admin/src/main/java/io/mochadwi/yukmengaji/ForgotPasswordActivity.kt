package io.mochadwi.yukmengaji

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

class ForgotPasswordActivity : AppCompatActivity() {

    private var mToolbar: Toolbar? = null

    private var ResetPasswordSendEmailButton: Button? = null
    private var ResetEmailInput: EditText? = null

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        mAuth = FirebaseAuth.getInstance()

        ResetPasswordSendEmailButton = findViewById<View>(
            R.id.reset_password_email_button) as Button
        ResetEmailInput = findViewById<View>(R.id.reset_password_email) as EditText

        mToolbar = findViewById<View>(R.id.forget_password_toolbar) as Toolbar
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setTitle("Reset Password")


        ResetPasswordSendEmailButton!!.setOnClickListener {
            val userEmail = ResetEmailInput!!.text.toString()

            if (TextUtils.isEmpty(userEmail)) {

                Toast.makeText(this@ForgotPasswordActivity, "Please write your email account",
                    Toast.LENGTH_SHORT).show()

            } else {

                mAuth!!.sendPasswordResetEmail(userEmail).addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        Toast.makeText(this@ForgotPasswordActivity, "Please check your email",
                            Toast.LENGTH_SHORT).show()
                        startActivity(
                            Intent(this@ForgotPasswordActivity, LoginActivity::class.java))

                    } else {

                        val message = task.exception!!.message
                        Toast.makeText(this@ForgotPasswordActivity, "Error$message",
                            Toast.LENGTH_SHORT).show()

                    }
                }
            }
        }
    }
}
