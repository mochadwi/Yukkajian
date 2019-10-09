package io.mochadwi.yukmengaji

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import io.mochadwi.yukmengaji.Adapter.MyViewPagerFragmentAdapter
import io.mochadwi.yukmengaji.Class.Category
import kotlinx.android.synthetic.main.activity_main_guest.*

class MainGuestActivity : AppCompatActivity(), PostFragment.OnListFragmentInteractionListener {
    companion object {
        val TAG = this::class.java.simpleName
    }

    private var loadingBar: ProgressDialog? = null
    private var mToolbar: Toolbar? = null

    private var mAuth: FirebaseAuth? = null
    private var CategoryRef: DatabaseReference? = null
    private var categories: MutableList<Category> = mutableListOf()

    private lateinit var currentUserID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_guest)

        loadingBar = ProgressDialog(this).apply {
            setTitle("Memuat Kajian")
            setMessage("Mohon Tunggu...")
            setCanceledOnTouchOutside(true)
            show()
        }
        cloudMessaging()

        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth!!.currentUser!!.uid
        CategoryRef = FirebaseDatabase.getInstance().reference.child("Categories")
        CategoryRef!!.keepSynced(true)

        mToolbar = findViewById<View>(R.id.main_page_toolbar) as Toolbar
        setSupportActionBar(mToolbar)
        supportActionBar!!.title = "Home"

        CategoryRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                // do nothing
            }

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach { data ->
                    categories.add(Category(data.key?.toInt() ?: 0, data.value.toString()))
                }

                displayCategories()
            }
        })
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

    private fun displayCategories() {
        viewpager2.apply {
            adapter = MyViewPagerFragmentAdapter(this@MainGuestActivity,
                currentUserID, categories.toList())
        }

        TabLayoutMediator(tabs, viewpager2) { tab: TabLayout.Tab, position: Int ->
            tab.text = "Kategori ${categories[position].name}"

            if (position == categories.size - 1) {
                loadingBar?.dismiss()
            }
        }.attach()
    }

    override fun onListFragmentInteraction(pos: String) {
        val clickPostIntent = Intent(this@MainGuestActivity,
            ClickPostGuestActivity::class.java)
        clickPostIntent.putExtra("PostKey", pos)
        startActivity(clickPostIntent)
    }
}