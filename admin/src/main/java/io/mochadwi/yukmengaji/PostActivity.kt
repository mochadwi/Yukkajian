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
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import io.mochadwi.yukmengaji.Class.DateDialog
import kotlinx.android.synthetic.main.activity_post.*
import org.json.JSONException
import org.json.JSONObject
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

    private lateinit var mGoogleApiClient: GoogleApiClient
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
    private lateinit var Pemateri: String
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

    private lateinit var placesClient: PlacesClient

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
        val jenis = arrayOf("Kategori", "Umum", "Ikhwan", "Ahkwat")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, jenis)
        PostSpinner!!.adapter = adapter

        UpdatePostButton = findViewById<View>(R.id.update_post_button) as Button

        mToolbar = findViewById<View>(R.id.toolbar_post) as Toolbar
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle("Update Post")

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

        update_post_place.setOnClickListener {
            setupPlaces()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null) {

            ImageUri = data.data
            SelectPostImage!!.setImageURI(ImageUri)
        }

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE && data != null) {
            if (resultCode == RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data)
                Toast.makeText(this, "Place: ${place.id}, ${place.name}, ${place.latLng}",
                    Toast.LENGTH_SHORT)
                    .show()
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status = Autocomplete.getStatusFromIntent(data)
                Toast.makeText(this, "${status.statusMessage}", Toast.LENGTH_SHORT)
                    .show()
                Log.i(TAG, "${status.statusMessage}")
            }
        }
    }

    private fun setupPlaces() {
        // Initialize Places.
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        val placesClient = Places.createClient(this)

        // Set the fields to specify which types of place data to return.
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)

        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .setCountry("ID")
            .build(this)
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    private fun ValidatePostInfo() {

        Description = PostDescription!!.text.toString()
        Pemateri = "${click_post_ustadz.text}"
        SpinnerDescription = PostSpinner!!.selectedItem.toString()
        DatePickerPost = EditTextDate!!.text.toString()
        TimePickerPost = chooseTime.text.toString()

        // TODO(mochamadiqbaldwicahyo): 2019-10-02 post image
//        if (ImageUri == null) {
//
//            Toast.makeText(this, "Please Select post Image", Toast.LENGTH_SHORT).show()
//        }
        if (TextUtils.isEmpty(Description)) {

            Toast.makeText(this, "Please Write post", Toast.LENGTH_SHORT).show()
        }
        if (TextUtils.isEmpty(Pemateri)) {

            Toast.makeText(this, "Please Write post", Toast.LENGTH_SHORT).show()
        }
        if (TextUtils.isEmpty(SpinnerDescription)) {

            Toast.makeText(this, "Please Write kategori", Toast.LENGTH_SHORT).show()
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
            SavingInformationToDatabase()
        }
    }

    private fun StoringImageToFirebaseStorage() {

        val calForDate = Calendar.getInstance()
        val currentDate = SimpleDateFormat("dd-MM-yyyy")
        saveCurrentDate = currentDate.format(calForDate.time)

        val currentTime = SimpleDateFormat("HH:mm")
        saveCurrentTime = currentTime.format(calForDate.time)

        postRandomName = saveCurrentDate!! + saveCurrentTime!!

        // TODO(mochamadiqbaldwicahyo): 2019-10-02 post image impl
//        val filepath = PostImagesReference!!.child("Posts Images")
//            .child(ImageUri!!.lastPathSegment + postRandomName + ".jpg")
//        filepath.putFile(ImageUri!!).addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//
//                filepath.downloadUrl.addOnSuccessListener {
//                    downloadUrl = it.toString()
//
//                    Toast.makeText(this@PostActivity, "Image Upload Success", Toast.LENGTH_SHORT)
//                        .show()
//
//                    SavingInformationToDatabase()
//                }
//            } else {
//
//                val message = task.exception!!.message
//                Toast.makeText(this@PostActivity, "Error occured$message", Toast.LENGTH_SHORT)
//                    .show()
//            }
//        }
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

                    val userFullName = "${dataSnapshot.child("fullname").value}"
                    val userProfileImage = "${dataSnapshot.child("profileimage").value}"

                    val postMap = HashMap<String, String>()
                    postMap.put("uid", current_user_id)
//                    postMap.put("postid", "${current_user_id.random()}")
                    postMap.put("date", saveCurrentDate)
                    postMap.put("time", saveCurrentTime)
                    postMap.put("description", Description)
                    postMap.put("pemateri", Pemateri)
                    postMap.put("kategori", SpinnerDescription)
                    postMap.put("datekajian", DatePickerPost)
                    postMap.put("timekajian", TimePickerPost)
//                    postMap.put("postimage", downloadUrl)
//                    postMap.put("profileimage", userProfileImage)
                    postMap.put("fullname", userFullName)
                    postMap.put("latlong", userFullName)
                    postMap.put("counter", "$countPosts")

                    PostsRef!!.child(current_user_id!! + postRandomName!!)
                        .updateChildren(postMap.toMap())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                shareSocmed()
                                sendPushNotif()
                                Toast.makeText(this@PostActivity, "Posts Update Succesfully",
                                    Toast.LENGTH_SHORT).show()
                                SendUserToMainActivity()
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

    private fun shareSocmed() {
        val shareBody = "Here is the share kajian body"
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.setType("text/plain")
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(sharingIntent, "Berbagi ke platform berikut"))
    }

    private fun sendPushNotif() {
        val mRequestQue = Volley.newRequestQueue(this)

        val json = JSONObject()
        try {
            json.put("to", "/topics/" + "all")
            val notificationObj = JSONObject()
            notificationObj.put("title", "New Kajian")
            notificationObj.put("body", "New kajian from : ${current_user_id}")
            // replace notification with data when went send data
            json.put("notification", notificationObj)

            val URL = "https://fcm.googleapis.com/fcm/send"
            val request = object : JsonObjectRequest(
                Method.POST,
                URL,
                json,
                Response.Listener { response -> Log.d("MUR", "onResponse: $response") },
                Response.ErrorListener { error ->
                    Log.d("MUR", "onError: " + error.networkResponse)
                }) {
                override fun getHeaders(): Map<String, String> {
                    val header = HashMap<String, String>()
                    header["content-type"] = "application/json"
                    header["authorization"] = "key=AAAAD9qeNQM:APA91bHbsSyd149hjZViFEJ77zf5ay8z-R6VIRZ-6y36uSrXPd7m0bzrtnL7WXaAVPvBLsj_shWsSPiqn1-_jtTyUFXa6aqhpGWZrv6y5lW5d6cqBi7tz33br2uu-y1ZlwiYlp8VSbKP"
                    return header
                }
            }

            mRequestQue.add(request)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun OpenGallery() {

        val galleryIntent = Intent()
        galleryIntent.action = Intent.ACTION_GET_CONTENT
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, Gallery_Pick)
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

        Log.i(TAG, "Google Places API connected.")
    }

    override fun onConnectionSuspended(i: Int) {
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
        val AUTOCOMPLETE_REQUEST_CODE = 99
    }
}
