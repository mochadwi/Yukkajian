package io.mochadwi.yukmengaji.Class

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import io.mochadwi.yukmengaji.R

class MessagesAdapter(private val userMessagesList: List<Messages>) :
    RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    private var mAuth: FirebaseAuth? = null

    private var userDatabaseRef: DatabaseReference? = null

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        var SenderMessageText: TextView
        var ReceiverMessageText: TextView
        var receiverProfileImage: CircleImageView

        init {

            SenderMessageText = itemView.findViewById<View>(R.id.sender_message_text) as TextView
            ReceiverMessageText = itemView.findViewById<View>(
                R.id.receiver_text_message) as TextView
            receiverProfileImage = itemView.findViewById<View>(
                R.id.message_profile_image) as CircleImageView


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {

        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.message_layout_of_users, parent, false)

        mAuth = FirebaseAuth.getInstance()
        return MessageViewHolder(v)

    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {

        val messageSenderID = mAuth!!.currentUser!!.uid
        val messages = userMessagesList[position]

        val fromUserID = messages.from
        val fromMessageType = messages.type

        userDatabaseRef = FirebaseDatabase.getInstance().reference.child("Users").child(fromUserID)
        userDatabaseRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.exists()) {

                    val image = dataSnapshot.child("profileimage").value!!.toString()

                    Picasso.with(holder.receiverProfileImage.context).load(image)
                        .placeholder(R.drawable.default_profile).into(holder.receiverProfileImage)

                }

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        if (fromMessageType == "text") {

            holder.ReceiverMessageText.visibility = View.INVISIBLE
            holder.receiverProfileImage.visibility = View.INVISIBLE

            if (fromUserID == messageSenderID) {

                holder.SenderMessageText.setBackgroundResource(R.drawable.action_add_send)
                holder.SenderMessageText.setTextColor(Color.WHITE)
                holder.SenderMessageText.gravity = Gravity.LEFT
                holder.SenderMessageText.text = messages.message

            } else {

                holder.SenderMessageText.visibility = View.INVISIBLE

                holder.ReceiverMessageText.visibility = View.VISIBLE
                holder.receiverProfileImage.visibility = View.VISIBLE

                holder.ReceiverMessageText.setBackgroundResource(R.drawable.action_add_send)
                holder.ReceiverMessageText.setTextColor(Color.WHITE)
                holder.ReceiverMessageText.gravity = Gravity.LEFT
                holder.ReceiverMessageText.text = messages.message

            }

        }

    }

    override fun getItemCount(): Int {
        return userMessagesList.size
    }
}
