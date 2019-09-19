package io.mochadwi.yukmengaji

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mochadwi.yukmengaji.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class PersonProfilActivity : AppCompatActivity() {

    private var userName: TextView? = null
    private var userProfilName: TextView? = null
    private var userStatus: TextView? = null
    private var userCountry: TextView? = null
    private var userGender: TextView? = null
    private var userRelation: TextView? = null
    private var userDOB: TextView? = null
    private var userProfileImage: CircleImageView? = null

    private var SendFriendReqButton: Button? = null
    private var DeclineFriendRequestbutton: Button? = null

    private val profileUserRef: DatabaseReference? = null
    private var FriendRequestRef: DatabaseReference? = null
    private var UsersRef: DatabaseReference? = null
    private var FriendRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null

    private var senderUserId: String? = null
    private var receiverUserId: String? = null
    private var CURRENT_STATE: String? = null
    private var saveCurrentDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_person_profil)

        mAuth = FirebaseAuth.getInstance()
        senderUserId = mAuth!!.currentUser!!.uid


        receiverUserId = intent.extras!!.get("visit_user_id")!!.toString()
        UsersRef = FirebaseDatabase.getInstance().reference.child("Users")
        FriendRequestRef = FirebaseDatabase.getInstance().reference.child("FriendRequests")

        FriendRef = FirebaseDatabase.getInstance().reference.child("Friends")


        IntializeFields()

        UsersRef!!.child(receiverUserId!!).addValueEventListener(object : ValueEventListener {
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

                    Picasso.with(this@PersonProfilActivity).load(myProfileImage)
                        .placeholder(R.drawable.default_profile).into(userProfileImage)

                    userName!!.text = "@$myUsername"
                    userProfilName!!.text = myProfileName
                    userStatus!!.text = myProfileStatus
                    userDOB!!.text = "DOB : $myDOB"
                    userCountry!!.text = "Country : $myCountry"
                    userGender!!.text = "Gender : $myGender"
                    userRelation!!.text = "Relationship : $myRelationship"

                    MaintananceofButton()

                }

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
        DeclineFriendRequestbutton!!.visibility = View.INVISIBLE
        DeclineFriendRequestbutton!!.isEnabled = false

        if (senderUserId != receiverUserId) {

            SendFriendReqButton!!.setOnClickListener {
                SendFriendReqButton!!.isEnabled = false
                if (CURRENT_STATE == "not_friends") {

                    SendFriendRequestToaPerson()

                }
                if (CURRENT_STATE == "request_sent") {

                    CancelFriendRequest()

                }
                if (CURRENT_STATE == "request_received") {

                    AcceptFriendRequest()
                }
                if (CURRENT_STATE == "friends") {

                    UnFriendAnExistingFriend()

                }
            }


        } else {

            DeclineFriendRequestbutton!!.visibility = View.INVISIBLE
            SendFriendReqButton!!.visibility = View.INVISIBLE

        }
    }

    private fun UnFriendAnExistingFriend() {

        FriendRef!!.child(senderUserId!!).child(receiverUserId!!)
            .removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    FriendRef!!.child(receiverUserId!!).child(senderUserId!!)
                        .removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                SendFriendReqButton!!.isEnabled = true
                                CURRENT_STATE = "request_send"
                                SendFriendReqButton!!.text = "Send Friend Request"

                                DeclineFriendRequestbutton!!.visibility = View.INVISIBLE
                                DeclineFriendRequestbutton!!.isEnabled = false

                            }
                        }
                }
            }

    }

    private fun AcceptFriendRequest() {

        val calForDate = Calendar.getInstance()
        val currentDate = SimpleDateFormat("dd-MM-yyyy")
        saveCurrentDate = currentDate.format(calForDate.time)

        FriendRef!!.child(senderUserId!!).child(receiverUserId!!).child("date")
            .setValue(saveCurrentDate)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    FriendRef!!.child(receiverUserId!!).child(senderUserId!!).child("date")
                        .setValue(saveCurrentDate)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                FriendRequestRef!!.child(senderUserId!!).child(receiverUserId!!)
                                    .removeValue()
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {

                                            FriendRequestRef!!.child(receiverUserId!!)
                                                .child(senderUserId!!)
                                                .removeValue()
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {

                                                        SendFriendReqButton!!.isEnabled = true
                                                        CURRENT_STATE = "friends"
                                                        SendFriendReqButton!!.text = "Unfriend This Person"

                                                        DeclineFriendRequestbutton!!.visibility = View.INVISIBLE
                                                        DeclineFriendRequestbutton!!.isEnabled = false

                                                    }
                                                }
                                        }
                                    }

                            }
                        }
                }
            }

    }

    private fun CancelFriendRequest() {

        FriendRequestRef!!.child(senderUserId!!).child(receiverUserId!!)
            .removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    FriendRequestRef!!.child(receiverUserId!!).child(senderUserId!!)
                        .removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                SendFriendReqButton!!.isEnabled = true
                                CURRENT_STATE = "not_friends"
                                SendFriendReqButton!!.text = "send Friend Request"

                                DeclineFriendRequestbutton!!.visibility = View.INVISIBLE
                                DeclineFriendRequestbutton!!.isEnabled = false

                            }
                        }
                }
            }

    }

    private fun MaintananceofButton() {

        FriendRequestRef!!.child(senderUserId!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    if (dataSnapshot.hasChild(receiverUserId!!)) {

                        val request_type = dataSnapshot.child(receiverUserId!!).child(
                            "request_type").value!!.toString()

                        if (request_type == "sent") {

                            CURRENT_STATE = "request_sent"
                            SendFriendReqButton!!.tag = "Cancel Friend Request"

                            DeclineFriendRequestbutton!!.visibility = View.INVISIBLE
                            DeclineFriendRequestbutton!!.isEnabled = false

                        } else if (request_type == "received") {

                            CURRENT_STATE = "request_received"
                            SendFriendReqButton!!.text = "Accept Friend Request"

                            DeclineFriendRequestbutton!!.visibility = View.VISIBLE
                            DeclineFriendRequestbutton!!.isEnabled = true

                            DeclineFriendRequestbutton!!.setOnClickListener { CancelFriendRequest() }

                        } else {

                            FriendRef!!.child(senderUserId!!)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                                        if (dataSnapshot.hasChild(receiverUserId!!)) {

                                            CURRENT_STATE = "friends"
                                            SendFriendReqButton!!.text = "Unfriend This Person"

                                            DeclineFriendRequestbutton!!.visibility = View.INVISIBLE
                                            DeclineFriendRequestbutton!!.isEnabled = false

                                        }
                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {

                                    }
                                })

                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
    }

    private fun SendFriendRequestToaPerson() {

        FriendRequestRef!!.child(senderUserId!!).child(receiverUserId!!)
            .child("request_type").setValue("sent")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    FriendRequestRef!!.child(receiverUserId!!).child(senderUserId!!)
                        .child("request_type").setValue("received")
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                SendFriendReqButton!!.isEnabled = true
                                CURRENT_STATE = "request_send"
                                SendFriendReqButton!!.text = "Cancel Friend Request"

                                DeclineFriendRequestbutton!!.visibility = View.INVISIBLE
                                DeclineFriendRequestbutton!!.isEnabled = false

                            }
                        }

                } else {

                }
            }
    }

    private fun IntializeFields() {

        userName = findViewById<View>(R.id.person_username) as TextView
        userProfilName = findViewById<View>(R.id.person_full_name) as TextView
        userStatus = findViewById<View>(R.id.person_profile_status) as TextView
        userCountry = findViewById<View>(R.id.person_country) as TextView
        userGender = findViewById<View>(R.id.person_gender) as TextView
        userRelation = findViewById<View>(R.id.person_relationship_status) as TextView
        userDOB = findViewById<View>(R.id.person_dob) as TextView

        userProfileImage = findViewById<View>(R.id.person_profile_pic) as CircleImageView

        //Button
        SendFriendReqButton = findViewById<View>(R.id.person_send_friend_request_btn) as Button
        DeclineFriendRequestbutton = findViewById<View>(
            R.id.person_decline_friend_request) as Button

        CURRENT_STATE = "not_friends"

    }
}
