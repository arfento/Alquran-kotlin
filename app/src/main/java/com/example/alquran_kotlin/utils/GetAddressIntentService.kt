package com.example.alquran_kotlin.utils

import android.app.IntentService
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.ResultReceiver
import java.lang.Exception
import java.util.*

class GetAddressIntentService : IntentService(IDENTIFIER){

    private var addressResultReceiver : ResultReceiver? = null

    override fun onHandleIntent(intent: Intent?) {
    var msg = ""

        //get result receiver from intent
        addressResultReceiver = intent!!.getParcelableExtra("add_receiver")
        if (addressResultReceiver == null){
            return
        }
        val location = intent.getParcelableExtra<Location>("add_location")

//        send no location error to result receiver
        if (location == null){
            msg = "No Location, Can't go further without location"
            sendResultsReceiver(0, msg)
            return
        }
        val geocoder = Geocoder(this, Locale.getDefault())
        var addresses : List<Address>? = null
        try {
            addresses = geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                1
            )
        }catch (ex : Exception){
            ex.printStackTrace()
        }

        if (addresses == null || addresses.size == 0){
            msg = "No Address found for this location"
            sendResultsReceiver(1, msg)
        }else{
            val address = addresses[0]
            val addressDetails = StringBuffer()

            addressDetails.append(address.adminArea)
            addressDetails.append("\n")
            sendResultsReceiver(2, addressDetails.toString())
        }


    }


    private fun sendResultsReceiver(resultCode: Int, msg: String) {

        val bundle = Bundle()
        bundle.putString("address_result", msg)
        addressResultReceiver!!.send(resultCode, bundle)
    }


    companion object {
        private const val IDENTIFIER = "GetAddressIntentService"
    }
}