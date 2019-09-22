package io.mochadwi.yukmengaji.Menu

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import io.mochadwi.yukmengaji.R

class ProfileActivity : AppCompatActivity() {

    private var userName: TextView? = null
    private var userProfilName: TextView? = null
    private var userStatus: TextView? = null
    private var userCountry: TextView? = null
    private var userGender: TextView? = null
    private var userRelation: TextView? = null
    private var userDOB: TextView? = null
    private var userProfileImage: CircleImageView? = null

    private var profileUserRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null

    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth!!.currentUser!!.uid
        profileUserRef = FirebaseDatabase.getInstance().reference.child("Users")
            .child(currentUserId!!)

        userName = findViewById(R.id.my_username)
        userProfilName = findViewById(R.id.my_profile_full_name)
        userStatus = findViewById(R.id.my_profile_status)
        userCountry = findViewById(R.id.my_country)
        userGender = findViewById(R.id.my_gender)
        userRelation = findViewById(R.id.my_relationship)
        userDOB = findViewById(R.id.my_dob)

        userProfileImage = findViewById(R.id.my_profile_pic)

        profileUserRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.exists()) {

                    val myProfileImage = dataSnapshot.child("profileimage").value!!.toString()
                    val myUsername = dataSnapshot.child("username").value!!.toString()
                    val myProfileName = dataSnapshot.child("fullname").value!!.toString()
                    val myProfileStatus = dataSnapshot.child("status").value!!.toString()
                    val myDOB = dataSnapshot.child("dob").value!!.toString()
                    val myCountry = dataSnapshot.child("country").value!!.toString()
                    val myGender = dataSnapshot.child("gender").value!!.toString()
                    val myRelationship = dataSnapshot.child("relationshipstatus").value!!.toString()

                    Picasso.with(this@ProfileActivity).load(myProfileImage)
                        .placeholder(R.drawable.default_profile).into(userProfileImage)

                    userName!!.text = "@$myUsername"
                    userProfilName!!.text = myProfileName
                    userStatus!!.text = myProfileStatus
                    userDOB!!.text = "DOB : $myDOB"
                    userCountry!!.text = "Country : $myCountry"
                    userGender!!.text = "Gender : $myGender"
                    userRelation!!.text = "Relationship : $myRelationship"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }
}
