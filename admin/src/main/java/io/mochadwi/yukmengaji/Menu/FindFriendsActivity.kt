package io.mochadwi.yukmengaji.Menu

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import io.mochadwi.yukmengaji.Class.FindFriends
import io.mochadwi.yukmengaji.PersonProfilActivity
import io.mochadwi.yukmengaji.R

class FindFriendsActivity : AppCompatActivity() {

    private var mToolbar: Toolbar? = null

    private var SearchButton: ImageButton? = null
    private var SearchInputText: EditText? = null

    private var SearchResultList: RecyclerView? = null

    private var allUsersDatabaseRef: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_friends)

        allUsersDatabaseRef = FirebaseDatabase.getInstance().reference.child("Users")

        mToolbar = findViewById<View>(R.id.find_frinds_bar_layout) as Toolbar
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Finds Friends"

        SearchButton = findViewById<View>(R.id.search_people_friends_button) as ImageButton
        SearchInputText = findViewById<View>(R.id.search_box_input) as EditText

        SearchResultList = findViewById<View>(R.id.search_result_list) as RecyclerView
        SearchResultList!!.setHasFixedSize(true)
        SearchResultList!!.layoutManager = LinearLayoutManager(this)

        SearchButton!!.setOnClickListener {
            val searchBoxInput = SearchInputText!!.text.toString()

            SearchPeopleAndFriends(searchBoxInput)
        }
    }

    private fun SearchPeopleAndFriends(searchBoxInput: String) {

        Toast.makeText(this, "Searching...", Toast.LENGTH_LONG).show()

        val searchPeopleandFriendsQuery = allUsersDatabaseRef!!.orderByChild("fullname")
            .startAt(searchBoxInput).endAt(searchBoxInput + "\uf8ff")

        val firebaseRecyclerAdapter = object :
            FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>(
                FindFriends::class.java!!,
                R.layout.all_users_display_layout,
                FindFriendsViewHolder::class.java,
                searchPeopleandFriendsQuery

            ) {
            override fun populateViewHolder(
                viewHolder: FindFriendsViewHolder,
                model: FindFriends,
                position: Int
            ) {

                viewHolder.setFullname(model.fullname)
                viewHolder.setStatus(model.status)
                viewHolder.setProfileimage(applicationContext, model.profileimage)

                viewHolder.mView.setOnClickListener {
                    val visit_user_id = getRef(position).key

                    val profileIntent = Intent(this@FindFriendsActivity,
                        PersonProfilActivity::class.java)
                    profileIntent.putExtra("visit_user_id", visit_user_id)
                    startActivity(profileIntent)
                }
            }
        }
        SearchResultList!!.adapter = firebaseRecyclerAdapter
    }

    class FindFriendsViewHolder(internal var mView: View) : RecyclerView.ViewHolder(mView) {

        fun setProfileimage(ctx: Context, profileimage: String) {

            val myImage = mView.findViewById<View>(R.id.all_users_profile_image) as CircleImageView
            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.default_profile)
                .into(myImage)
        }

        fun setFullname(fullname: String) {
            val myName = mView.findViewById<View>(R.id.all_users_profile_full_name) as TextView
            myName.text = fullname
        }

        fun setStatus(status: String) {
            val myStatus = mView.findViewById<View>(R.id.all_users_status) as TextView
            myStatus.text = status
        }
    }
}
