package io.mochadwi.yukmengaji

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import io.mochadwi.yukmengaji.Class.Friends
import io.mochadwi.yukmengaji.Menu.ProfileActivity

class FriendsActivity : AppCompatActivity() {


    private var myFriendList: RecyclerView? = null
    private var FriendsRef: DatabaseReference? = null
    private var UsersRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var online_user_id: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        mAuth = FirebaseAuth.getInstance()
        online_user_id = mAuth!!.currentUser!!.uid
        FriendsRef = FirebaseDatabase.getInstance().reference.child("Friends")
            .child(online_user_id!!)
        UsersRef = FirebaseDatabase.getInstance().reference.child("Users")

        myFriendList = findViewById<View>(R.id.friend_list) as RecyclerView
        myFriendList!!.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        myFriendList!!.layoutManager = linearLayoutManager

        DisplayAllFriends()
    }

    private fun DisplayAllFriends() {

        val firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(

            Friends::class.java!!,
            R.layout.all_users_display_layout,
            FriendsViewHolder::class.java,
            FriendsRef

        ) {
            override fun populateViewHolder(
                viewHolder: FriendsViewHolder, model: Friends, position: Int
            ) {

                viewHolder.setDate(model.date)

                val userIDs = getRef(position).key ?: "userIDs"

                UsersRef!!.child(userIDs).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        if (dataSnapshot.exists()) {

                            val userName = dataSnapshot.child("fullname").value!!.toString()
                            val profileImage = dataSnapshot.child("profileimage").value!!.toString()

                            viewHolder.setFullname(userName)
                            viewHolder.setProfileimage(applicationContext, profileImage)

                            viewHolder.mView.setOnClickListener {
                                val options = arrayOf<CharSequence>(

                                    "$userName's Profile", "Send Message")
                                val builder = AlertDialog.Builder(this@FriendsActivity)
                                builder.setTitle("Select Option")

                                builder.setItems(options) { dialog, which ->
                                    if (which == 0) {

                                        val profileIntent = Intent(this@FriendsActivity,
                                            ProfileActivity::class.java)
                                        profileIntent.putExtra("visit_user_id", userIDs)
                                        startActivity(profileIntent)

                                    }
                                    if (which == 1) {

                                        val chatIntent = Intent(this@FriendsActivity,
                                            ChatActivity::class.java)
                                        chatIntent.putExtra("visit_user_id", userIDs)
                                        chatIntent.putExtra("userName", userName)
                                        startActivity(chatIntent)

                                    }
                                }
                                builder.show()
                            }

                        }

                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })

            }
        }
        myFriendList!!.adapter = firebaseRecyclerAdapter
    }

    class FriendsViewHolder(internal var mView: View) : RecyclerView.ViewHolder(mView) {

        fun setProfileimage(ctx: Context, profileimage: String) {

            val myImage = mView.findViewById<View>(R.id.all_users_profile_image) as CircleImageView
            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.default_profile)
                .into(myImage)

        }

        fun setFullname(fullname: String) {
            val myName = mView.findViewById<View>(R.id.all_users_profile_full_name) as TextView
            myName.text = fullname
        }

        fun setDate(date: String) {
            val friendDate = mView.findViewById<View>(R.id.all_users_status) as TextView
            friendDate.text = "Friends since : $date"
        }
    }
}
