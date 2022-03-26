package com.example.alquran_kotlin

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.text.format.DateFormat
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.azhar.quran.model.ModelSurah
import com.example.alquran_kotlin.JadwalSholatFragment.Companion.newInstance
import com.example.alquran_kotlin.adapter.SurahAdapter
import com.example.alquran_kotlin.networking.Api
import com.example.alquran_kotlin.utils.GetAddressIntentService
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_surah.*
import org.json.JSONArray
import org.json.JSONException
import java.util.*
import java.util.jar.Manifest
import kotlin.collections.ArrayList

class SurahActivity : AppCompatActivity(), SurahAdapter.onSelectData {


    var surahAdapter : SurahAdapter? = null
    var progressDialog : ProgressDialog? = null
    var modelSurah : MutableList<ModelSurah> = ArrayList()
    var hariIni : String? = null
    var tanggal : String? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var addressResultReceiver : LocationAddressResultReceiver? = null
    private var currentLocation : Location? = null
    private var locationCallback : LocationCallback? = null

    private inner class LocationAddressResultReceiver internal constructor(handler : Handler?) : ResultReceiver(handler){
        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            super.onReceiveResult(resultCode, resultData)
            if (resultCode == 0){
                address
            }
            if (resultCode == 1){
                Toast.makeText(this@SurahActivity, "Address not found", Toast.LENGTH_SHORT).show()
            }
            val currentAdd = resultData!!.getString("address_result")
            showResults(currentAdd)
        }

        private fun showResults(currentAdd: String?) {
            txtLocation!!.text = currentAdd
        }
    }

    private val address : Unit
    get() {
        if (!Geocoder.isPresent()){
            Toast.makeText(this, "Cant find current address", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(this, GetAddressIntentService::class.java)
        intent.putExtra("add_receiver", addressResultReceiver)
        intent.putExtra("add_location", currentLocation)
        startService(intent)
    }


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_surah)

        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Mohon Tunggu")
        progressDialog!!.setCancelable(false)
        progressDialog!!.setMessage("Sedang Menampilkan Data")

        addressResultReceiver = LocationAddressResultReceiver(Handler())

        val dateNow = Calendar.getInstance().time
        hariIni = DateFormat.format("EEEE", dateNow) as String
        tanggal = DateFormat.format("d MMMM yyyy", dateNow) as String
        tvToday.setText("$hariIni")
        tvDate.setText(tanggal)

        val sendDetail = newInstance("detail")
        llTime.setOnClickListener(View.OnClickListener {
            sendDetail.show(supportFragmentManager, sendDetail.tag)
            //startActivity(new Intent(MainActivity.this, FragmentMenu.class));
        })

        llMosque.setOnClickListener {
            startActivity(Intent(this@SurahActivity, MasjidActivity::class.java)) }

        rvSurah.layoutManager = LinearLayoutManager(this)
        rvSurah.setHasFixedSize(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                currentLocation = locationResult.locations[0]
                address

            }
        }
        startlocationUpdate()

        listSurah()

    }

    private fun listSurah() {
        progressDialog!!.show()
        AndroidNetworking.get(Api.URL_LIST_SURAH)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(response: JSONArray) {
                    for (i in 0 until response.length()) {
                        try {
                            progressDialog!!.dismiss()
                            val dataApi = ModelSurah()
                            val jsonObject = response.getJSONObject(i)
                            dataApi.nomor = jsonObject.getString("nomor")
                            dataApi.nama = jsonObject.getString("nama")
                            dataApi.type = jsonObject.getString("type")
                            dataApi.ayat = jsonObject.getString("ayat")
                            dataApi.asma = jsonObject.getString("asma")
                            dataApi.arti = jsonObject.getString("arti")
                            dataApi.audio = jsonObject.getString("audio")
                            dataApi.keterangan = jsonObject.getString("keterangan")
                            modelSurah.add(dataApi)
                            showListSurah()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Toast.makeText(this@SurahActivity, "Gagal menampilkan data!",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                override fun onError(anError: ANError) {
                    progressDialog!!.dismiss()
                    Toast.makeText(this@SurahActivity, "Tidak ada jaringan internet!",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showListSurah() {
        surahAdapter = SurahAdapter(this@SurahActivity, modelSurah, this)
        rvSurah!!.adapter = surahAdapter
    }

    override fun onSelected(modelSurah: ModelSurah) {
        val intent = Intent(this@SurahActivity, MainActivity::class.java)
        intent.putExtra("detailSurah", modelSurah)
        startActivity(intent)
    }


    override fun onResume() {
        super.onResume()
        startlocationUpdate()
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient!!.removeLocationUpdates(locationCallback)
    }

    private fun startlocationUpdate() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }else{
            val locationRequest = LocationRequest()
            locationRequest.interval = 1000
            locationRequest.fastestInterval = 1000
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            fusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback, null)



        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startlocationUpdate()
            } else {
                Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 2
    }


}