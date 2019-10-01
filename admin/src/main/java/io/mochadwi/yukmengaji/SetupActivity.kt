package io.mochadwi.yukmengaji

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class SetupActivity : AppCompatActivity() {

    private var FullName: EditText? = null
    private var SaveInformationButton: Button? = null
    private var ProfileImage: CircleImageView? = null

    private var mAuth: FirebaseAuth? = null
    private var UserRef: DatabaseReference? = null

    private var loadingBar: ProgressDialog? = null

    lateinit var currentUserID: String

    private val mImageUri: Uri? = null

    private var UserProfileImageRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth!!.currentUser!!.uid
        UserRef = FirebaseDatabase.getInstance().reference.child("Users").child(currentUserID)
        UserProfileImageRef = FirebaseStorage.getInstance().reference.child("Profile Images")

        ProfileImage = findViewById<View>(R.id.setup_profile_image) as CircleImageView
        FullName = findViewById<View>(R.id.setup_full_name) as EditText

        SaveInformationButton = findViewById<View>(R.id.setup_information_button) as Button


        loadingBar = ProgressDialog(this)

        SaveInformationButton!!.setOnClickListener { SaveAccountSetupInformation() }

        ProfileImage!!.setOnClickListener {
            val galleryIntent = Intent()
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, Gallery_Pick)
        }

/*        UserRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.exists()) {

                    if (dataSnapshot.hasChild("profileimage")) {

                        val image = dataSnapshot.child("profileimage").value!!.toString()
                        Picasso.with(this@SetupActivity).load(image)
                            .placeholder(R.drawable.default_profile).into(ProfileImage)
                    } else {

                        Toast.makeText(this@SetupActivity, "Please Select Profile Image First",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })*/
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

            if (resultCode == RESULT_OK) {

                loadingBar!!.setTitle("Profile Image")
                loadingBar!!.setMessage("Please Wait ")
                loadingBar!!.show()
                loadingBar!!.setCanceledOnTouchOutside(false)

                val resultUri = result.uri

                val filepath = UserProfileImageRef!!.child("$currentUserID.jpg")

                filepath.putFile(resultUri).addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        Toast.makeText(this@SetupActivity,
                            "Profile Image sucessfully to firebase storage", Toast.LENGTH_SHORT)
                            .show()

                        filepath.downloadUrl.addOnSuccessListener { downloadUrl ->
                            UserRef!!.child("profileimage").setValue(downloadUrl)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {

                                        val selfIntent = Intent(this@SetupActivity,
                                            SetupActivity::class.java)
                                        startActivity(selfIntent)

                                        Toast.makeText(this@SetupActivity,
                                            "profile image store to firebase database",
                                            Toast.LENGTH_SHORT).show()
                                        loadingBar!!.dismiss()
                                    } else {

                                        val message = task.exception!!.message
                                        Toast.makeText(this@SetupActivity, "Error dk tau",
                                            Toast.LENGTH_SHORT).show()
                                        loadingBar!!.dismiss()
                                    }
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

    private fun SaveAccountSetupInformation() {

        val fullname = FullName!!.text.toString()
        val username = "dkm_${fullname.toLowerCase().replace("\\s".toRegex(), "")}"
        val country = "Indonesia"

        val user_id = mAuth!!.currentUser!!.uid

        if (TextUtils.isEmpty(fullname)) {

            Toast.makeText(this, "Please Write your full name", Toast.LENGTH_SHORT).show()
        } else {

            loadingBar!!.setTitle("Saving Information")
            loadingBar!!.setMessage("Please Wait Loading create new account")
            loadingBar!!.show()
            loadingBar!!.setCanceledOnTouchOutside(false)

            val userMap = HashMap<String, String>()
            userMap.put("username", username)
            userMap.put("fullname", fullname)
            userMap.put("country", country)

            UserRef!!.updateChildren(userMap.toMap()).addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    SendUsersToMainActivity()
                    Toast.makeText(this@SetupActivity, "your account created succesfulyl",
                        Toast.LENGTH_SHORT).show()
                    loadingBar!!.dismiss()
                } else {

                    val message = task.exception!!.message
                    Toast.makeText(this@SetupActivity, "error$message", Toast.LENGTH_SHORT)
                        .show()
                    loadingBar!!.dismiss()
                }
            }
        }
    }

    private fun SendUsersToMainActivity() {

        val setupIntent = Intent(this@SetupActivity, MainActivity::class.java)
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(setupIntent)
        finish()
    }

    companion object {

        internal val Gallery_Pick = 1
    }
}
