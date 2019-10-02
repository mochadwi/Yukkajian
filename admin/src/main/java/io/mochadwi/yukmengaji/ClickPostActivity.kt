package io.mochadwi.yukmengaji

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class ClickPostActivity : AppCompatActivity() {

    private var mToolbar: Toolbar? = null

    private var PostImage: ImageView? = null
    private var PostDescription: TextView? = null
    private var DeletePostButton: Button? = null
    private var EditPostButton: Button? = null

    private var ClickPostRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null

    private var PostKey: String? = null
    private var currentUserId: String? = null
    private var databaseUserId: String? = null
    private var description: String? = null
    private var image: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_click_post)

        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth!!.currentUser!!.uid

        mToolbar = findViewById<View>(R.id.click_post_toolbar) as Toolbar

        PostKey = intent.extras!!.get("PostKey")!!.toString()
        ClickPostRef = FirebaseDatabase.getInstance().reference.child("Posts").child(PostKey!!)

        PostImage = findViewById<View>(R.id.click_post_image) as ImageView
        PostDescription = findViewById<View>(R.id.click_post_description) as TextView
        DeletePostButton = findViewById<View>(R.id.delete_post_button) as Button
        EditPostButton = findViewById<View>(R.id.edit_post_button) as Button

        DeletePostButton!!.visibility = View.INVISIBLE
        EditPostButton!!.visibility = View.INVISIBLE

        ClickPostRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.exists()) {

                    description = dataSnapshot.child("description").value!!.toString()
                    databaseUserId = dataSnapshot.child("uid").value!!.toString()

                    PostDescription!!.text = description
                    Picasso.with(this@ClickPostActivity).load(image).into(PostImage)

                    if (currentUserId == databaseUserId) {

                        DeletePostButton!!.visibility = View.VISIBLE
                        EditPostButton!!.visibility = View.VISIBLE
                    }
                    EditPostButton!!.setOnClickListener { EditCurrentPost(description) }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
        DeletePostButton!!.setOnClickListener { DeleteCurrentPost() }
    }

    private fun EditCurrentPost(description: String?) {

        val builder = AlertDialog.Builder(this@ClickPostActivity)
        builder.setTitle("Edit Post")

        val inputField = EditText(this@ClickPostActivity)
        inputField.setText(description)
        builder.setView(inputField)

        builder.setPositiveButton("Update") { dialog, which ->
            ClickPostRef!!.child("description").setValue(inputField.text.toString())
            Toast.makeText(this@ClickPostActivity, "Post update success", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }
        val dialog = builder.create()
        dialog.show()
        dialog.window!!.setBackgroundDrawableResource(android.R.color.white)
    }

    private fun DeleteCurrentPost() {

        ClickPostRef!!.removeValue()
        SendUserToMainActivity()
        Toast.makeText(this, "Post has been deleted.", Toast.LENGTH_SHORT).show()
    }

    private fun SendUserToMainActivity() {

        val mainIntent = Intent(this@ClickPostActivity, MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }
}
