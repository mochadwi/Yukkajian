package io.mochadwi.yukmengaji

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import io.mochadwi.yukmengaji.Class.Posts

class MainGuestActivity : AppCompatActivity() {
    companion object {
        val TAG = this::class.java.simpleName
    }

    private var postList: RecyclerView? = null
    private var mToolbar: Toolbar? = null

    private var mAuth: FirebaseAuth? = null
    private var UsersRef: DatabaseReference? = null
    private var PostsRef: DatabaseReference? = null

    private lateinit var currentUserID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_guest)

        cloudMessaging()

        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth!!.currentUser!!.uid
        UsersRef = FirebaseDatabase.getInstance().reference.child("Users")
        PostsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        UsersRef!!.keepSynced(true)
        PostsRef!!.keepSynced(true)

        mToolbar = findViewById<View>(R.id.main_page_toolbar) as Toolbar
        setSupportActionBar(mToolbar)
        supportActionBar!!.setTitle("Home")

        postList = findViewById<View>(R.id.all_users_post_list) as RecyclerView
        postList!!.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        postList!!.layoutManager = linearLayoutManager

        UsersRef!!.child(currentUserID).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("fullname")) {
                        val fullname = dataSnapshot.child("fullname").value!!.toString()
                    }
                    // TODO(mochamadiqbaldwicahyo): 2019-10-02 Implement profile image
//                    if (dataSnapshot.hasChild("profileimage")) {
//                        val image = dataSnapshot.child("profileimage").value!!.toString()
//                        Picasso.with(this@MainActivity).load(image)
//                            .placeholder(R.drawable.default_profile).into(NavProfileImage)
//                    } else {
//                        Toast.makeText(this@MainActivity, "Profile image do not exists...",
//                            Toast.LENGTH_SHORT).show()
//                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        DisplayAllUsersPosts()
    }

    private fun cloudMessaging() {
        FirebaseMessaging.getInstance().subscribeToTopic("all")
            .addOnCompleteListener { task ->
                var msg = "Subscribed to all info"
                if (!task.isSuccessful) {
                    msg = "Failed to subscribe"
                }
                Log.d(TAG, msg)
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            }

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token

                // Log and toast
                Log.d(TAG, "$token")
                Toast.makeText(baseContext, token, Toast.LENGTH_SHORT).show()
            })
    }
    private fun DisplayAllUsersPosts() {

        val SortPostInDescendingOrder = PostsRef!!.orderByChild("counter")

        val firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Posts, PostsViewHolder>(
            Posts::class.java!!,
            R.layout.all_posts_layout,
            PostsViewHolder::class.java,
            SortPostInDescendingOrder

        ) {
            override fun populateViewHolder(
                viewHolder: PostsViewHolder,
                model: Posts,
                position: Int
            ) {

                val PostKey = getRef(position).key ?: "PostKey"

                viewHolder.setFullname(model.fullname)
                viewHolder.setTime(model.time)
                viewHolder.setDate(model.date)
                viewHolder.setDescription(model.description)
//                viewHolder.setProfileimage(applicationContext, model.profileimage)
//                viewHolder.setPostimage(applicationContext, model.postimage)

                viewHolder.mView.setOnClickListener {
                    val clickPostIntent = Intent(this@MainGuestActivity,
                        ClickPostGuestActivity::class.java)
                    clickPostIntent.putExtra("PostKey", PostKey)
                    startActivity(clickPostIntent)
                }
            }
        }
        postList!!.adapter = firebaseRecyclerAdapter
        findViewById<TextView>(R.id.tv_users_post_empty).visibility = View.GONE
    }

    class PostsViewHolder(internal var mView: View) : RecyclerView.ViewHolder(mView) {

        internal var currentUserId: String

        init {
            currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        }

        fun setFullname(fullname: String) {
            val username = mView.findViewById<View>(R.id.post_user_name) as TextView
            username.text = fullname
        }

        fun setProfileimage(ctx: Context, profileimage: String) {
            val image = mView.findViewById<View>(R.id.post_profile_image) as CircleImageView
            Picasso.with(ctx).load(profileimage).into(image)
        }

        fun setTime(time: String) {
            val PostTime = mView.findViewById<View>(R.id.post_time) as TextView
            PostTime.text = "" + time
        }

        fun setDate(date: String) {
            val PostDate = mView.findViewById<View>(R.id.post_date) as TextView
            PostDate.text = "" + date
        }

        fun setDescription(description: String) {
            val PostDescription = mView.findViewById<View>(R.id.click_post_description) as TextView
            PostDescription.text = description
        }

        fun setPostimage(ctx1: Context, postimage: String) {
            val PostImage = mView.findViewById<View>(R.id.click_post_image) as ImageView
            Picasso.with(ctx1).load(postimage).into(PostImage)
        }
    }
}