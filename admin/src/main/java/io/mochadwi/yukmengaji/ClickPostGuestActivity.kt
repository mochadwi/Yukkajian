package io.mochadwi.yukmengaji

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class ClickPostGuestActivity : AppCompatActivity() {

    private var mToolbar: Toolbar? = null

    private var PostImage: ImageView? = null
    private var PostDescription: TextView? = null
    private var NavigateMapsPostButton: Button? = null

    private var ClickPostRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null

    private var PostKey: String? = null
    private var currentUserId: String? = null
    private var databaseUserId: String? = null
    private var description: String? = null
    private var image: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_click_post_guest)

        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth!!.currentUser!!.uid

        mToolbar = findViewById<View>(R.id.click_post_toolbar) as Toolbar

        PostKey = intent.extras!!.get("PostKey")!!.toString()
        ClickPostRef = FirebaseDatabase.getInstance().reference.child("Posts").child(PostKey!!)

        PostImage = findViewById<View>(R.id.click_post_image) as ImageView
        PostDescription = findViewById<View>(R.id.click_post_description) as TextView
        NavigateMapsPostButton = findViewById<View>(R.id.maps_navigation_button) as Button

        ClickPostRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.exists()) {

                    description = dataSnapshot.child("description").value!!.toString()
                    databaseUserId = dataSnapshot.child("uid").value!!.toString()

                    PostDescription!!.text = description
                    Picasso.with(this@ClickPostGuestActivity).load(image).into(PostImage)

                    if (currentUserId == databaseUserId) {

                        NavigateMapsPostButton!!.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
        NavigateMapsPostButton!!.setOnClickListener { GoToMapsPost() }
    }

    private fun GoToMapsPost() {
        Toast.makeText(this, "Send to google maps.", Toast.LENGTH_SHORT).show()
    }
}
