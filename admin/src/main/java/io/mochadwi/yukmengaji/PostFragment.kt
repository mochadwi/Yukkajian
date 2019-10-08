package io.mochadwi.yukmengaji

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import io.mochadwi.yukmengaji.Class.Posts

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [PostFragment.OnListFragmentInteractionListener] interface.
 */
class PostFragment : Fragment() {

    // TODO: Customize parameters
    private var columnCount = 1
    private var categoryName: String? = null
    private var UsersRef: DatabaseReference? = null
    private var PostsRef: DatabaseReference? = null

    private var listener: OnListFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            fetchData(it.getString(ARG_USER_ID) ?: "unknown_id")
            columnCount = it.getInt(ARG_COLUMN_COUNT)
            categoryName = it.getString(ARG_CATEEGORY_NAME) ?: "Umum"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_posts_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)

                val query = PostsRef!!.orderByChild("kategori")
                    .equalTo(categoryName)

                val firebaseRecyclerAdapter = object :
                    FirebaseRecyclerAdapter<Posts, PostsViewHolder>(
                        Posts::class.java!!,
                        R.layout.all_posts_layout,
                        PostsViewHolder::class.java,
                        query
                    ) {
                    override fun populateViewHolder(
                        viewHolder: PostsViewHolder,
                        model: Posts,
                        position: Int
                    ) {
                        val PostKey = getRef(position).key ?: "PostKey"

                        viewHolder.setCategory(model.kategori)
                        viewHolder.setUstadz(model.pemateri)
                        viewHolder.setFullname(model.fullname)
                        viewHolder.setTime(model.time)
                        viewHolder.setDate(model.date)
                        viewHolder.setDescription(model.description)

                        viewHolder.mView.setOnClickListener {
                            listener?.onListFragmentInteraction(PostKey)
                        }
                    }
                }

                adapter = firebaseRecyclerAdapter
            }
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(
                "$context must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun fetchData(userId: String) {
        PostsRef = FirebaseDatabase.getInstance().reference.child("Posts")
        PostsRef!!.keepSynced(true)

        UsersRef = FirebaseDatabase.getInstance().reference.child("Users")
        UsersRef!!.keepSynced(true)

        UsersRef!!.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("fullname")) {
                        val fullname = dataSnapshot.child("fullname").value!!.toString()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson
     * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnListFragmentInteractionListener {

        // TODO: Update argument type and name
        fun onListFragmentInteraction(pos: String)
    }

    class PostsViewHolder(internal var mView: View) : RecyclerView.ViewHolder(mView) {

        internal var currentUserId: String

        init {
            currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        }

        fun setCategory(category: String) {
            (mView.findViewById<View>(R.id.post_category) as TextView).apply {
                text = category
            }
        }

        fun setUstadz(ustadz: String) {
            (mView.findViewById<View>(R.id.post_ustadz) as TextView).apply {
                text = ustadz
            }
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

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"
        const val ARG_USER_ID = "user-id"
        const val ARG_CATEEGORY_NAME = "category-name"
        const val ARG_COUNTER = "counter"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            PostFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }

        fun newInstance(userId: String, categoryName: String) =
            PostFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId)
                    putString(ARG_CATEEGORY_NAME, categoryName)
                }
            }
    }
}
