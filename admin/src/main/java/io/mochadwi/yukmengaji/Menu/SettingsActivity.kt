package io.mochadwi.yukmengaji.Menu

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mochadwi.yukmengaji.R
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import de.hdodenhof.circleimageview.CircleImageView
import io.mochadwi.yukmengaji.MainActivity
import java.util.*

class SettingsActivity : AppCompatActivity() {

    private var mToolbar: Toolbar? = null
    private var loadingBar: ProgressDialog? = null

    private var userName: EditText? = null
    private var userProfilName: EditText? = null
    private var userStatus: EditText? = null
    private var userCountry: EditText? = null
    private var userGender: EditText? = null
    private var userRelation: EditText? = null
    private var userDOB: EditText? = null
    private var UpdateAccountSettingsButton: Button? = null
    private var userProfileImage: CircleImageView? = null

    private var SettingsUserRef: DatabaseReference? = null
    private var PostUsersRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null

    private var currentuser: String? = null

    private var UserProfileImageRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        mAuth = FirebaseAuth.getInstance()
        currentuser = mAuth!!.currentUser!!.uid
        SettingsUserRef = FirebaseDatabase.getInstance().reference.child("Users")
            .child(currentuser!!)
        UserProfileImageRef = FirebaseStorage.getInstance().reference.child("Profile Images")

        PostUsersRef = FirebaseDatabase.getInstance().reference.child("Posts").child("fullname")

        mToolbar = findViewById<View>(R.id.settings_toolbar) as Toolbar
        setSupportActionBar(mToolbar)
        supportActionBar!!.title = "Account Settings"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        UpdateAccountSettingsButton = findViewById<View>(
            R.id.update_accounts_settings_buttons) as Button

        loadingBar = ProgressDialog(this)

        userName = findViewById<View>(R.id.settings_username) as EditText
        userProfilName = findViewById<View>(R.id.settings_profile_full_name) as EditText
        userStatus = findViewById<View>(R.id.settings_status) as EditText
        userCountry = findViewById<View>(R.id.settings_country) as EditText
        userGender = findViewById<View>(R.id.settings_gender) as EditText
        userRelation = findViewById<View>(R.id.settings_relationship) as EditText
        userDOB = findViewById<View>(R.id.settings_dob) as EditText

        userProfileImage = findViewById<View>(R.id.settings_profile_image) as CircleImageView

        PostUsersRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                //Error
                //String PostFullname = dataSnapshot.child("fullname").getValue().toString();

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        SettingsUserRef!!.addValueEventListener(object : ValueEventListener {
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

                    Picasso.with(this@SettingsActivity).load(myProfileImage)
                        .placeholder(R.drawable.default_profile).into(userProfileImage)

                    userName!!.setText(myUsername)
                    userProfilName!!.setText(myProfileName)
                    userStatus!!.setText(myProfileStatus)
                    userDOB!!.setText(myDOB)
                    userCountry!!.setText(myCountry)
                    userGender!!.setText(myGender)
                    userRelation!!.setText(myRelationship)

                }

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        UpdateAccountSettingsButton!!.setOnClickListener { ValidateAccountInfo() }

        userProfileImage!!.setOnClickListener {
            val galleryIntent = Intent()
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, Gallery_Pick)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Gallery_Pick && resultCode == Activity.RESULT_OK && data != null) {

            val imageUri = data.data
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(this)

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            val result = CropImage.getActivityResult(data)

            if (resultCode == Activity.RESULT_OK) {

                loadingBar!!.setTitle("Profile Image")
                loadingBar!!.setMessage("Please Wait ")
                loadingBar!!.setCanceledOnTouchOutside(true)
                loadingBar!!.show()

                val resultUri = result.uri

                val filepath = UserProfileImageRef!!.child(currentuser!! + ".jpg")

                filepath.putFile(resultUri).addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        Toast.makeText(this@SettingsActivity,
                            "Profile Image sucessfully to firebase storage", Toast.LENGTH_SHORT)
                            .show()

                        val downloadUrl = task.result.downloadUrl!!.toString()

                        SettingsUserRef!!.child("profileimage").setValue(downloadUrl)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {

                                    val selfIntent = Intent(this@SettingsActivity,
                                        SettingsActivity::class.java)
                                    startActivity(selfIntent)

                                    Toast.makeText(this@SettingsActivity,
                                        "profile image store to firebase database",
                                        Toast.LENGTH_SHORT).show()
                                    loadingBar!!.dismiss()
                                } else {

                                    val message = task.exception!!.message
                                    Toast.makeText(this@SettingsActivity, "Error dk tau",
                                        Toast.LENGTH_SHORT).show()
                                    loadingBar!!.dismiss()
                                }
                            }
                    }
                }

            } else {

                Toast.makeText(this, "Error Occured: Image can be cropped. Try Again",
                    Toast.LENGTH_SHORT).show()
                loadingBar!!.dismiss()
            }
        }
    }

    private fun ValidateAccountInfo() {

        val username = userName!!.text.toString()
        val profilename = userProfilName!!.text.toString()
        val status = userStatus!!.text.toString()
        val dob = userDOB!!.text.toString()
        val country = userCountry!!.text.toString()
        val gender = userGender!!.text.toString()
        val relation = userRelation!!.text.toString()

        if (TextUtils.isEmpty(username)) {

            Toast.makeText(this, "Please write you username", Toast.LENGTH_SHORT).show()

        }
        if (TextUtils.isEmpty(profilename)) {

            Toast.makeText(this, "Please write you Profil Name", Toast.LENGTH_SHORT).show()

        }
        if (TextUtils.isEmpty(status)) {

            Toast.makeText(this, "Please write you Status", Toast.LENGTH_SHORT).show()

        }
        if (TextUtils.isEmpty(dob)) {

            Toast.makeText(this, "Please write you Date of Birth", Toast.LENGTH_SHORT).show()

        }
        if (TextUtils.isEmpty(country)) {

            Toast.makeText(this, "Please write you Contry", Toast.LENGTH_SHORT).show()

        }
        if (TextUtils.isEmpty(gender)) {

            Toast.makeText(this, "Please write you Gender", Toast.LENGTH_SHORT).show()

        }
        if (TextUtils.isEmpty(relation)) {

            Toast.makeText(this, "Please write you Relationship", Toast.LENGTH_SHORT).show()

        } else {

            loadingBar!!.setTitle("Profile Image")
            loadingBar!!.setMessage("Please Wait ")
            loadingBar!!.setCanceledOnTouchOutside(true)
            loadingBar!!.show()

            UpdateAccountInfo(username, profilename, status, dob, country, gender, relation)

        }
    }

    private fun UpdateAccountInfo(
        username: String, profilename: String, status: String, dob: String, country: String,
        gender: String, relation: String
    ) {

        val userMap = HashMap()

        userMap.put("username", username)
        userMap.put("fullname", profilename)
        userMap.put("status", status)
        userMap.put("dob", dob)
        userMap.put("country", country)
        userMap.put("gender", gender)
        userMap.put("relationshipstatus", relation)

        SettingsUserRef!!.updateChildren(userMap)
            .addOnCompleteListener(object : OnCompleteListener {
                override fun onComplete(task: Task<*>) {

                    if (task.isSuccessful) {

                        SendUserToMainActivity()
                        Toast.makeText(this@SettingsActivity,
                            "Account Settings Updated Succesfully", Toast.LENGTH_SHORT).show()
                        //loadingBar.dismiss();

                    } else {

                        val message = task.exception!!.message
                        Toast.makeText(this@SettingsActivity,
                            "Error occured while updating account setting info..$message",
                            Toast.LENGTH_SHORT).show()
                        //loadingBar.dismiss();
                    }
                }
            })
    }

    private fun SendUserToMainActivity() {
        val setupIntent = Intent(this@SettingsActivity, MainActivity::class.java)
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(setupIntent)
        finish()
    }

    companion object {

        internal val Gallery_Pick = 1
    }
}
