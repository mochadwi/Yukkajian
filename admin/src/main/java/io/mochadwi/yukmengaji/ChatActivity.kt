package io.mochadwi.yukmengaji

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mochadwi.yukmengaji.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import io.mochadwi.yukmengaji.Class.Messages
import io.mochadwi.yukmengaji.Class.MessagesAdapter
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {

    private var Chattoolbar: Toolbar? = null
    private var SendMessageButton: ImageButton? = null
    private var SendImagefileButton: ImageButton? = null
    private var userMessageInput: EditText? = null
    private var userMessageList: RecyclerView? = null

    private val messagesList = ArrayList<Messages>()
    private var linearLayoutManager: LinearLayoutManager? = null
    private var messagesAdapter: MessagesAdapter? = null

    private var messageReceiverID: String? = null
    private var messageReceiverName: String? = null
    private var messageSenderID: String? = null
    private var saveCurrentDate: String? = null
    private var saveCurrentTime: String? = null

    private var receiverName: TextView? = null
    private var receiverProfileImage: CircleImageView? = null

    private var RootRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        mAuth = FirebaseAuth.getInstance()
        messageSenderID = mAuth!!.currentUser!!.uid

        RootRef = FirebaseDatabase.getInstance().reference

        messageReceiverID = intent.extras!!.get("visit_user_id")!!.toString()
        messageReceiverName = intent.extras!!.get("userName")!!.toString()

        initializeFields()

        DisplayReceiverInfo()

        SendMessageButton!!.setOnClickListener { SendMessage() }

        FecthMessages()

    }

    private fun FecthMessages() {

        RootRef!!.child("Messages").child(messageSenderID!!).child(messageReceiverID!!)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String) {

                    if (dataSnapshot.exists()) {

                        val messages = dataSnapshot.getValue<Messages>(Messages::class.java)
                        messagesList.add(messages)
                        messagesAdapter!!.notifyDataSetChanged()

                    }
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, s: String) {

                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {

                }

                override fun onChildMoved(dataSnapshot: DataSnapshot, s: String) {

                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })

    }

    private fun SendMessage() {

        val messageText = userMessageInput!!.text.toString()

        if (TextUtils.isEmpty(messageText)) {

            Toast.makeText(this, "Please a type message", Toast.LENGTH_SHORT).show()

        } else {

            val message_sender_ref = "Messages/$messageSenderID/$messageReceiverID"
            val message_receiver_ref = "Messages/$messageReceiverID/$messageSenderID"

            val user_message_key = RootRef!!.child("Messages").child(messageSenderID!!)
                .child(messageReceiverID!!).push()

            val message_push_id = user_message_key.key

            val calForDate = Calendar.getInstance()
            val currentDate = SimpleDateFormat("dd-MM-yyyy")
            saveCurrentDate = currentDate.format(calForDate.time)

            val calForTime = Calendar.getInstance()
            val currentTime = SimpleDateFormat("HH:mm")
            saveCurrentTime = currentTime.format(calForDate.time)

            val messageTextBody = HashMap()
            messageTextBody.put("message", messageText)
            messageTextBody.put("time", saveCurrentTime)
            messageTextBody.put("date", saveCurrentDate)
            messageTextBody.put("type", "text")
            messageTextBody.put("from", messageSenderID)

            val messageBodyDetails = HashMap()
            messageBodyDetails.put("$message_sender_ref/$message_push_id", messageTextBody)
            messageBodyDetails.put("$message_receiver_ref/$message_push_id", messageTextBody)

            RootRef!!.updateChildren(messageBodyDetails)
                .addOnCompleteListener(object : OnCompleteListener {
                    override fun onComplete(task: Task<*>) {

                        if (task.isSuccessful) {

                            Toast.makeText(this@ChatActivity, "Send Message succesfully",
                                Toast.LENGTH_SHORT).show()
                            userMessageInput!!.setText("")

                        } else {

                            val message = task.exception!!.message
                            Toast.makeText(this@ChatActivity, "Error$message", Toast.LENGTH_SHORT)
                                .show()
                            userMessageInput!!.setText("")
                        }

                    }
                })

        }
    }

    private fun DisplayReceiverInfo() {

        receiverName!!.text = messageReceiverName

        RootRef!!.child("Users").child(messageReceiverID!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    if (dataSnapshot.exists()) {

                        val profileImage = dataSnapshot.child("profileimage").value!!.toString()
                        Picasso.with(this@ChatActivity).load(profileImage)
                            .placeholder(R.drawable.default_profile).into(receiverProfileImage)


                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })

    }

    private fun initializeFields() {

        Chattoolbar = findViewById<View>(R.id.chat_bar_layout) as Toolbar
        setSupportActionBar(Chattoolbar)

        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowCustomEnabled(true)
        val layoutInflater = this.getSystemService(
            Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar, null)
        actionBar.customView = action_bar_view

        receiverName = findViewById<View>(R.id.custom_profile_name) as TextView
        receiverProfileImage = findViewById<View>(R.id.custom_profile_image) as CircleImageView

        SendMessageButton = findViewById<View>(R.id.send_message_button) as ImageButton
        SendImagefileButton = findViewById<View>(R.id.send_image_file_button) as ImageButton
        userMessageInput = findViewById<View>(R.id.input_message) as EditText


        messagesAdapter = MessagesAdapter(messagesList)
        userMessageList = findViewById<View>(R.id.messages_list_users) as RecyclerView
        linearLayoutManager = LinearLayoutManager(this)
        userMessageList!!.setHasFixedSize(true)
        userMessageList!!.layoutManager = linearLayoutManager
        userMessageList!!.adapter = messagesAdapter

    }
}
