package io.mochadwi.yukmengaji

import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.location.places.PlaceBuffer
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import io.mochadwi.yukmengaji.Adapter.PlaceArrayAdapter
import io.mochadwi.yukmengaji.Class.DateDialog
import java.text.SimpleDateFormat
import java.util.*

class PostActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener,
                     GoogleApiClient.ConnectionCallbacks {

    private var mToolbar: Toolbar? = null

    private var loadingBar: ProgressDialog? = null

    private var SelectPostImage: ImageButton? = null
    private var PostDescription: EditText? = null
    private var UpdatePostButton: Button? = null

    private var EditTextDate: EditText? = null
    private var PostSpinner: Spinner? = null
    private var mAutocompleteTextView: AutoCompleteTextView? = null

    private lateinit var mGoogleApiClient: GoogleApiClient
    private var mPlaceArrayAdapter: PlaceArrayAdapter? = null
    // Batas

    // Time Declaration
    private lateinit var chooseTime: EditText
    private lateinit var timePickerDialog: TimePickerDialog
    private lateinit var calendar: Calendar
    private var currentHour: Int = 0
    private var currentMinute: Int = 0
    private lateinit var amPm: String
    private var ImageUri: Uri? = null

    private lateinit var Description: String
    private lateinit var SpinnerDescription: String
    private lateinit var DatePickerPost: String
    private lateinit var TimePickerPost: String

    private var PostImagesReference: StorageReference? = null
    private var userRef: DatabaseReference? = null
    private var PostsRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null

    private lateinit var saveCurrentDate: String
    private lateinit var saveCurrentTime: String
    private lateinit var postRandomName: String
    private lateinit var downloadUrl: String
    private lateinit var current_user_id: String

    private var countPosts: Long = 0

    private val mAutocompleteClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
        val item = mPlaceArrayAdapter!!.getItem(position)
        val placeId = item!!.placeId.toString()
        Log.i(TAG, "Selected: " + item.description)
        val placeResult = Places.GeoDataApi
            .getPlaceById(mGoogleApiClient, placeId)
        placeResult.setResultCallback(mUpdatePlaceDetailsCallback)
        Log.i(TAG, "Fetching details for ID: " + item.placeId)
    }

    private val mUpdatePlaceDetailsCallback = ResultCallback<PlaceBuffer> { places ->
        if (!places.status.isSuccess) {
            Log.e(TAG, "Place query did not complete. Error: " + places.status.toString())
            return@ResultCallback
        }
        // Selecting the first object buffer.
        val place = places.get(0)
        val attributions = places.attributions

        // mNameView.setText(Html.fromHtml(place.getAddress() + ""));
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        mAuth = FirebaseAuth.getInstance()
        current_user_id = mAuth!!.currentUser!!.uid

        PostImagesReference = FirebaseStorage.getInstance().reference
        userRef = FirebaseDatabase.getInstance().reference.child("Users")
        PostsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        SelectPostImage = findViewById<View>(R.id.select_post_image) as ImageButton
        PostDescription = findViewById<View>(R.id.click_post_description) as EditText
        loadingBar = ProgressDialog(this)

        EditTextDate = findViewById<View>(R.id.update_post_date) as EditText
        PostSpinner = findViewById<View>(R.id.update_post_spinner) as Spinner
        val jenis = arrayOf("Umum", "Ikhwan", "Ahkwat")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, jenis)
        PostSpinner!!.adapter = adapter

        UpdatePostButton = findViewById<View>(R.id.update_post_button) as Button

        mToolbar = findViewById<View>(R.id.toolbar_post) as Toolbar
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle("Update Post")

        // Place Adapter
        mAutocompleteTextView = findViewById<View>(R.id.update_post_place) as AutoCompleteTextView
        mAutocompleteTextView!!.threshold = 3

        mGoogleApiClient = GoogleApiClient.Builder(this@PostActivity)
            .addApi(Places.GEO_DATA_API)
            .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
            .addConnectionCallbacks(this)
            .build()

        mAutocompleteTextView!!.onItemClickListener = mAutocompleteClickListener
        mPlaceArrayAdapter = PlaceArrayAdapter(this, android.R.layout.simple_list_item_1,
            BOUNDS_MOUNTAIN_VIEW, null)
        mAutocompleteTextView!!.setAdapter<PlaceArrayAdapter>(mPlaceArrayAdapter)

        // Time AutoPicker
        chooseTime = findViewById(R.id.update_post_time)
        chooseTime.setOnClickListener {
            calendar = Calendar.getInstance()
            currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            currentMinute = calendar.get(Calendar.MINUTE)

            timePickerDialog = TimePickerDialog(this@PostActivity,
                TimePickerDialog.OnTimeSetListener { timePicker, hourOfDay, minutes ->
                    if (hourOfDay >= 12) {
                        amPm = " PM"
                    } else {
                        amPm = " AM"
                    }
                    chooseTime.setText(String.format("%02d:%02d", hourOfDay, minutes) + amPm)
                }, currentHour, currentMinute, false)

            timePickerDialog.show()
        }

        SelectPostImage!!.setOnClickListener { OpenGallery() }

        UpdatePostButton!!.setOnClickListener { ValidatePostInfo() }
    }

    private fun ValidatePostInfo() {

        Description = PostDescription!!.text.toString()
        SpinnerDescription = PostSpinner!!.selectedItem.toString()
        DatePickerPost = EditTextDate!!.text.toString()
        TimePickerPost = chooseTime.text.toString()

        if (ImageUri == null) {

            Toast.makeText(this, "Please Select post Image", Toast.LENGTH_SHORT).show()
        }
        if (TextUtils.isEmpty(Description)) {

            Toast.makeText(this, "Please Write post", Toast.LENGTH_SHORT).show()
        }
        if (TextUtils.isEmpty(SpinnerDescription)) {

            Toast.makeText(this, "Please Write jenis", Toast.LENGTH_SHORT).show()
        }
        if (TextUtils.isEmpty(DatePickerPost)) {

            Toast.makeText(this, "Please Write date", Toast.LENGTH_SHORT).show()
        }
        if (TextUtils.isEmpty(TimePickerPost)) {

            Toast.makeText(this, "Please Write waktu", Toast.LENGTH_SHORT).show()
        } else {

            loadingBar!!.setTitle("Add New Post")
            loadingBar!!.setMessage("Please Wait Add New Post")
            loadingBar!!.show()
            loadingBar!!.setCanceledOnTouchOutside(true)
            StoringImageToFirebaseStorage()
        }
    }

    private fun StoringImageToFirebaseStorage() {

        val calForDate = Calendar.getInstance()
        val currentDate = SimpleDateFormat("dd-MM-yyyy")
        saveCurrentDate = currentDate.format(calForDate.time)

        val calForTime = Calendar.getInstance()
        val currentTime = SimpleDateFormat("HH:mm")
        saveCurrentTime = currentTime.format(calForDate.time)

        postRandomName = saveCurrentDate!! + saveCurrentTime!!

        val filepath = PostImagesReference!!.child("Posts Images")
            .child(ImageUri!!.lastPathSegment + postRandomName + ".jpg")
        filepath.putFile(ImageUri!!).addOnCompleteListener { task ->
            if (task.isSuccessful) {

                filepath.downloadUrl.addOnSuccessListener {
                    downloadUrl = it.toString()

                    Toast.makeText(this@PostActivity, "Image Upload Success", Toast.LENGTH_SHORT)
                        .show()

                    SavingInformationToDatabase()
                }
            } else {

                val message = task.exception!!.message
                Toast.makeText(this@PostActivity, "Error occured$message", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun SavingInformationToDatabase() {

        PostsRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.exists()) {

                    countPosts = dataSnapshot.childrenCount
                } else {

                    countPosts = 0
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        userRef!!.child(current_user_id!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.exists()) {

                    val userFullName = dataSnapshot.child("fullname").value!!.toString()
                    val userProfileImage = dataSnapshot.child("profileimage").value!!.toString()

                    val postMap = HashMap<String, String>()
                    postMap.put("uid", current_user_id)
                    postMap.put("date", saveCurrentDate)
                    postMap.put("time", saveCurrentTime)
                    postMap.put("description", Description)
                    postMap.put("jenis", SpinnerDescription)
                    postMap.put("datekajian", DatePickerPost)
                    postMap.put("timekajian", TimePickerPost)
                    postMap.put("postimage", downloadUrl)
                    postMap.put("profileimage", userProfileImage)
                    postMap.put("fullname", userFullName)
                    postMap.put("counter", "$countPosts")

                    PostsRef!!.child(current_user_id!! + postRandomName!!)
                        .updateChildren(postMap.toMap())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                SendUserToMainActivity()
                                Toast.makeText(this@PostActivity, "Posts Update Succesfully",
                                    Toast.LENGTH_SHORT).show()
                                loadingBar!!.dismiss()
                            } else {

                                val message = task.exception!!.message
                                Toast.makeText(this@PostActivity, "Error$message",
                                    Toast.LENGTH_SHORT).show()
                                loadingBar!!.dismiss()
                            }
                        }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun OpenGallery() {

        val galleryIntent = Intent()
        galleryIntent.action = Intent.ACTION_GET_CONTENT
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, Gallery_Pick)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null) {

            ImageUri = data.data
            SelectPostImage!!.setImageURI(ImageUri)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId
        if (id == android.R.id.home) {

            SendUserToMainActivity()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun SendUserToMainActivity() {

        val mainIntent = Intent(this@PostActivity, MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
    }

    // Date Picker

    override fun onStart() {
        super.onStart()

        val txtDate = findViewById<View>(R.id.update_post_date) as EditText
        txtDate.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {

                val dialog = DateDialog(v)
                val ft = fragmentManager.beginTransaction()
                dialog.show(ft, "DatePicker")
            }
        }
    }

    override fun onConnected(bundle: Bundle?) {

        mPlaceArrayAdapter!!.setGoogleApiClient(mGoogleApiClient)
        Log.i(TAG, "Google Places API connected.")
    }

    override fun onConnectionSuspended(i: Int) {
        mPlaceArrayAdapter!!.setGoogleApiClient(null)
        Log.e(TAG, "Google Places API connection suspended.")
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {

        Log.e(TAG,
            "Google Places API connection failed with error code: " + connectionResult.errorCode)

        Toast.makeText(this,
            "Google Places API connection failed with error code:" + connectionResult.errorCode,
            Toast.LENGTH_LONG).show()
    }

    companion object {

        // Maps Place Api
        private val TAG = "PostActivity"
        private val GOOGLE_API_CLIENT_ID = 0
        private val BOUNDS_MOUNTAIN_VIEW = LatLngBounds(LatLng(37.398160, -122.180831),
            LatLng(37.430610, -121.972090))

        internal val Gallery_Pick = 1
    }
}
