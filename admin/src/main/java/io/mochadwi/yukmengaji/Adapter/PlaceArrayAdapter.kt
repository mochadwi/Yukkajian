package io.mochadwi.yukmengaji.Adapter

import android.content.Context
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.compat.AutocompleteFilter
import com.google.android.libraries.places.compat.Places
import java.util.*

class PlaceArrayAdapter(
    context: Context,
    resource: Int,
    private val mBounds: LatLngBounds,
    private val mPlaceFilter: AutocompleteFilter?
) : ArrayAdapter<PlaceArrayAdapter.PlaceAutocomplete>(context, resource), Filterable {

    private var mGoogleApiClient: GoogleApiClient? = null
    private var mResultList: ArrayList<PlaceAutocomplete>? = null

    fun setGoogleApiClient(googleApiClient: GoogleApiClient?) {
        if (googleApiClient == null || !googleApiClient.isConnected) {
            mGoogleApiClient = null
        } else {
            mGoogleApiClient = googleApiClient
        }
    }

    override fun getCount(): Int {
        return mResultList?.size ?: 0
    }

    override fun getItem(position: Int): PlaceAutocomplete? {
        return mResultList?.get(position) ?: PlaceAutocomplete("none", "none")
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {

                val results = FilterResults()
                if (constraint != null) {
                    mResultList = getPredictions(constraint)

                    if (mResultList != null) {
                        results.values = mResultList
                        results.count = mResultList!!.size
                    }
                }

                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null && results.count > 0) {
                    // The API returned at least one result, update the data.
                    notifyDataSetChanged()
                } else {
                    // The API did not return any results, invalidate the data set.
                    notifyDataSetInvalidated()
                }
            }
        }
    }

    private fun getPredictions(constraint: CharSequence): ArrayList<PlaceAutocomplete>? {

        mGoogleApiClient?.let {
            Log.i(TAG, "Executing autocomplete query for: $constraint")

            val results = Places.getGeoDataClient(context)
                .getAutocompletePredictions(constraint.toString(),
                    mBounds, mPlaceFilter)

            // Wait for predictions, set the timeout
            if (results.isSuccessful) {
                val autocompletePredictions = results.result

                Log.i(TAG, "Query completed. Received " + autocompletePredictions?.count +
                    " predictions.")
                val iterator = autocompletePredictions?.iterator()
                val resultList = ArrayList<PlaceAutocomplete>(autocompletePredictions?.count ?: 0)
                while (iterator?.hasNext() == true) {
                    iterator.next()?.let { prediction ->
                        resultList.add(
                            PlaceAutocomplete(prediction.placeId ?: "",
                                prediction.getFullText(null) ?: ""))
                    }
                }
                // Buffer release
                autocompletePredictions?.release()
                return resultList
            } else {

                Toast.makeText(context, "Error: ${results.exception?.message}",
                    Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error getting place predictions: ${results.exception?.message}")
                return null
            }
        }

        Log.e(TAG, "Google API client is not connected.")
        return null
    }

    inner class PlaceAutocomplete internal constructor(
        var placeId: String,
        var description: CharSequence
    ) {

        override fun toString(): String {
            return description.toString()
        }
    }

    companion object {

        private val TAG = "PlaceArrayAdapter"
    }
}
