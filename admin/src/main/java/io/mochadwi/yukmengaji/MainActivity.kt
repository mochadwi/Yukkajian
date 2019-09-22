package io.mochadwi.yukmengaji

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import io.mochadwi.yukmengaji.Class.Posts
import io.mochadwi.yukmengaji.Menu.FindFriendsActivity
import io.mochadwi.yukmengaji.Menu.ProfileActivity
import io.mochadwi.yukmengaji.Menu.SettingsActivity

class MainActivity : AppCompatActivity() {

    private var navigationView: NavigationView? = null
    private var drawerLayout: DrawerLayout? = null
    private var actionBarDrawerToggle: ActionBarDrawerToggle? = null
    private var postList: RecyclerView? = null
    private var mToolbar: Toolbar? = null

    private var NavProfileImage: CircleImageView? = null
    private var NavProfileUserName: TextView? = null

    private var mAuth: FirebaseAuth? = null
    private var UsersRef: DatabaseReference? = null
    private var PostsRef: DatabaseReference? = null

    private var addPostBtn: FloatingActionButton? = null

    private lateinit var currentUserID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth!!.currentUser!!.uid
        UsersRef = FirebaseDatabase.getInstance().reference.child("Users")
        PostsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        UsersRef!!.keepSynced(true)
        PostsRef!!.keepSynced(true)

        mToolbar = findViewById<View>(R.id.main_page_toolbar) as Toolbar
        setSupportActionBar(mToolbar)
        supportActionBar!!.setTitle("Home")

        drawerLayout = findViewById<View>(R.id.drawable_layout) as DrawerLayout
        actionBarDrawerToggle = ActionBarDrawerToggle(this@MainActivity, drawerLayout,
            R.string.drawer_open, R.string.drawer_close)
        drawerLayout!!.addDrawerListener(actionBarDrawerToggle!!)
        actionBarDrawerToggle!!.syncState()
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        navigationView = findViewById(R.id.navigation_view)

        postList = findViewById<View>(R.id.all_users_post_list) as RecyclerView
        postList!!.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        postList!!.layoutManager = linearLayoutManager

        val navView = navigationView!!.inflateHeaderView(R.layout.navigation_header)
        NavProfileImage = navView.findViewById<View>(R.id.nav_profile_image) as CircleImageView
        NavProfileUserName = navView.findViewById<View>(R.id.nav_user_full_name) as TextView

        UsersRef!!.child(currentUserID).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("fullname")) {
                        val fullname = dataSnapshot.child("fullname").value!!.toString()
                        NavProfileUserName!!.text = fullname
                    }
                    if (dataSnapshot.hasChild("profileimage")) {
                        val image = dataSnapshot.child("profileimage").value!!.toString()
                        Picasso.with(this@MainActivity).load(image)
                            .placeholder(R.drawable.default_profile).into(NavProfileImage)
                    } else {
                        Toast.makeText(this@MainActivity, "Profile name do not exists...",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        navigationView!!.setNavigationItemSelectedListener { item ->
            UserMenuSelector(item)
            false
        }
        addPostBtn = findViewById(R.id.add_post_btn)
        addPostBtn!!.setOnClickListener {
            val newPostIntent = Intent(this@MainActivity, PostActivity::class.java)
            startActivity(newPostIntent)
        }

        DisplayAllUsersPosts()
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
                viewHolder.setProfileimage(applicationContext, model.profileimage)
                viewHolder.setPostimage(applicationContext, model.postimage)

                viewHolder.mView.setOnClickListener {
                    val clickPostIntent = Intent(this@MainActivity, ClickPostActivity::class.java)
                    clickPostIntent.putExtra("PostKey", PostKey)
                    startActivity(clickPostIntent)
                }
            }
        }
        postList!!.adapter = firebaseRecyclerAdapter
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

    private fun SendUserToPostActivity() {
        val addNewPostIntent = Intent(this@MainActivity, PostActivity::class.java)
        startActivity(addNewPostIntent)
    }

    override fun onStart() {
        super.onStart()

        val currentUser = mAuth!!.currentUser

        if (currentUser == null) {
            SendUserToLoginActivity()
        } else {
            CheckUserExistence()
        }
    }

    private fun CheckUserExistence() {
        val current_user_id = mAuth!!.currentUser!!.uid

        UsersRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.hasChild(current_user_id)) {
                    SendUserToSetupActivity()
                } else {
                    Toast.makeText(this@MainActivity, "no current user exist", Toast.LENGTH_LONG)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@MainActivity, databaseError.message, Toast.LENGTH_LONG)
            }
        })
    }

    private fun SendUserToSetupActivity() {
        val setupIntent = Intent(this@MainActivity, SetupActivity::class.java)
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(setupIntent)
        finish()
    }

    private fun SendUserToLoginActivity() {
        val loginIntent = Intent(this@MainActivity, LoginActivity::class.java)
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(loginIntent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarDrawerToggle!!.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(
            item)
    }

    private fun UserMenuSelector(item: MenuItem) {
        when (item.itemId) {
            R.id.nav_post -> SendUserToPostActivity()

            R.id.nav_profile -> {
                SendUserToProfileActivity()
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show()
            }

            R.id.nav_home -> Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show()

            R.id.nav_friends -> {
                SendUserToFriendsActivity()
                Toast.makeText(this, "Friends", Toast.LENGTH_SHORT).show()
            }

            R.id.nav_finds_friends -> {
                SendUserToFindFriendsActivity()
                Toast.makeText(this, "Find Friends", Toast.LENGTH_SHORT).show()
            }

            R.id.nav_messages -> Toast.makeText(this, "Messages", Toast.LENGTH_SHORT).show()

            R.id.nav_settings -> {
                SendUserToSettingsActivity()
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
            }

            R.id.nav_logout -> {
                mAuth!!.signOut()
                SendUserToLoginActivity()
            }
        }
    }

    private fun SendUserToFriendsActivity() {
        val friendsIntent = Intent(this@MainActivity, FriendsActivity::class.java)
        startActivity(friendsIntent)
    }

    private fun SendUserToSettingsActivity() {
        val settingIntent = Intent(this@MainActivity, SettingsActivity::class.java)
        startActivity(settingIntent)
    }

    private fun SendUserToProfileActivity() {
        val loginIntent = Intent(this@MainActivity, ProfileActivity::class.java)
        startActivity(loginIntent)
    }

    private fun SendUserToFindFriendsActivity() {
        val findFriendIntent = Intent(this@MainActivity, FindFriendsActivity::class.java)
        startActivity(findFriendIntent)
    }
}