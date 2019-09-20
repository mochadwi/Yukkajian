package io.mochadwi.yukmengaji

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import io.mochadwi.yukmengaji.Class.Comments
import java.text.SimpleDateFormat
import java.util.*

class CommentsActivity : AppCompatActivity() {

    private var CommentList: RecyclerView? = null
    private var PostCommentButton: ImageButton? = null
    private var CommentInputText: EditText? = null

    private var UsersRef: DatabaseReference? = null
    private var PostsRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null

    private var Post_Key: String? = null
    private lateinit var current_user_id: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        Post_Key = intent.extras!!.get("PostKey")!!.toString()

        mAuth = FirebaseAuth.getInstance()
        current_user_id = mAuth!!.currentUser!!.uid
        UsersRef = FirebaseDatabase.getInstance().reference.child("Users")
        PostsRef = FirebaseDatabase.getInstance().reference.child("Posts").child(Post_Key!!)
            .child("Comments")

        CommentList = findViewById<View>(R.id.comment_list) as RecyclerView
        CommentList!!.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        CommentList!!.layoutManager = linearLayoutManager

        CommentInputText = findViewById<View>(R.id.comment_input) as EditText
        PostCommentButton = findViewById<View>(R.id.post_comment_btn) as ImageButton

        PostCommentButton!!.setOnClickListener {
            UsersRef!!.child(current_user_id!!).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    if (dataSnapshot.exists()) {

                        val userName = dataSnapshot.child("username").value!!.toString()

                        ValidateComment(userName)

                        CommentInputText!!.setText("")

                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
        }

    }

    override fun onStart() {
        super.onStart()

        val firebaseRecyclerAdapter = object :
            FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(

                Comments::class.java!!,
                R.layout.all_comments_layout,
                CommentsViewHolder::class.java,
                PostsRef

            ) {
            override fun populateViewHolder(
                viewHolder: CommentsViewHolder, model: Comments, position: Int
            ) {

                viewHolder.setUsername(model.username)
                viewHolder.setComment(model.comment)
                viewHolder.setDate(model.date)
                viewHolder.setTime(model.time)

            }
        }

        CommentList!!.adapter = firebaseRecyclerAdapter

    }

    class CommentsViewHolder(internal var mView: View) : RecyclerView.ViewHolder(mView) {

        fun setUsername(username: String) {

            val myUsername = mView.findViewById<View>(R.id.comment_username) as TextView
            myUsername.text = "@$username "

        }

        fun setComment(comment: String) {

            val myComment = mView.findViewById<View>(R.id.comment_text) as TextView
            myComment.text = comment

        }

        fun setDate(date: String) {

            val myDate = mView.findViewById<View>(R.id.comment_date) as TextView
            myDate.text = "  Date: $date"

        }

        fun setTime(time: String) {

            val myTime = mView.findViewById<View>(R.id.comment_time) as TextView
            myTime.text = "  Time: $time"

        }
    }

    private fun ValidateComment(userName: String) {

        val commentText = CommentInputText!!.text.toString()
        if (TextUtils.isEmpty(commentText)) {

            Toast.makeText(this, "Please write text to comment...", Toast.LENGTH_SHORT).show()

        } else {

            val calForDate = Calendar.getInstance()
            val currentDate = SimpleDateFormat("dd-MM-yyyy")
            val saveCurrentDate = currentDate.format(calForDate.time)

            val calForTime = Calendar.getInstance()
            val currentTime = SimpleDateFormat("HH:mm")
            val saveCurrentTime = currentTime.format(calForTime.time)

            val RandomKey = current_user_id + saveCurrentDate + saveCurrentTime

            val commentsMap = HashMap<String, String>()
            commentsMap.put("uid", current_user_id)
            commentsMap.put("comment", commentText)
            commentsMap.put("date", saveCurrentDate)
            commentsMap.put("time", saveCurrentTime)
            commentsMap.put("username", userName)

            PostsRef!!.child(RandomKey).updateChildren(commentsMap.toMap())
        }
    }
}

