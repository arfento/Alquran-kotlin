package com.example.alquran_kotlin

import android.app.ProgressDialog
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.androidnetworking.model.Progress
import com.azhar.quran.model.ModelAyat
import com.azhar.quran.model.ModelSurah
import com.example.alquran_kotlin.adapter.AyatAdapter
import com.example.alquran_kotlin.networking.Api
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    var nomor : String? = null;
    var nama : String? = null;
    var arti : String? = null;
    var type : String? = null;
    var ayat : String? = null;
    var keterangan : String? = null;
    var audio : String? = null;
    var modelSurah : ModelSurah? = null;
    var ayatAdapter : AyatAdapter? = null;
    var progressDialog : ProgressDialog? = null;
    var modelAyat : MutableList<ModelAyat> = ArrayList();
    var mHanlder : Handler? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //setToolbar
        toolbar_detail.setTitle(null)
        setSupportActionBar(toolbar_detail)
        assert(supportActionBar != null)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mHanlder = Handler()

        //get data dari listSurah
        modelSurah = intent.getSerializableExtra("detailSurah") as ModelSurah
        if (modelSurah != null){
            nomor = modelSurah!!.nomor
            nama = modelSurah!!.nama
            arti = modelSurah!!.arti
            type = modelSurah!!.type
            ayat = modelSurah!!.ayat
            keterangan = modelSurah!!.keterangan
            audio = modelSurah!!.audio

            fabStop.visibility = View.GONE
            fabPlay.visibility = View.VISIBLE

            tvHeader.setText(nama)
            tvTitle.setText(nama)
            tvSubtitle.setText(arti)
            tvInfo.setText("$type - $ayat Ayat")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                tvKet.text = Html.fromHtml(keterangan, Html.FROM_HTML_MODE_COMPACT)
            else{
                tvKet.text = Html.fromHtml(keterangan)
            }


            //get & play audio
            val mediaPlayer = MediaPlayer()
            fabPlay.setOnClickListener(View.OnClickListener {
                try {
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
                    mediaPlayer.setDataSource(audio)
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                }catch (e : IOException){
                    e.printStackTrace()
                }
                fabPlay.setVisibility(View.GONE)
                fabStop.setVisibility(View.VISIBLE)

            })

            fabStop.setOnClickListener {
                mediaPlayer.stop()
                mediaPlayer.reset()
                fabPlay.visibility = View.VISIBLE
                fabStop.visibility = View.GONE
            }

        }

        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Mohon Tunggu")
        progressDialog!!.setCancelable(false)
        progressDialog!!.setMessage("Sedang menampilkan data...")


        rvAyat.layoutManager = LinearLayoutManager(this)
        rvAyat.setHasFixedSize(true)

        listAyat()

    }

    private fun listAyat(){
        progressDialog!!.show()
        AndroidNetworking.get(Api.URL_LIST_AYAT)
            .addPathParameter("nomor", nomor)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(response: JSONArray) {
                    for (i in 0 until response.length()){
                        try {
                            progressDialog!!.dismiss()
                            val dataApi = ModelAyat()
                            val jsonObject : JSONObject = response.getJSONObject(i)
                            dataApi.nomor = jsonObject.getString("nomor")
                            dataApi.arab = jsonObject.getString("ar")
                            dataApi.indo = jsonObject.getString("id")
                            dataApi.terjemahan = jsonObject.getString("tr")
                            modelAyat.add(dataApi)
                            showListAyat()
                        }catch (e: JSONException)
                        {
                            e.printStackTrace()
                            Toast.makeText(this@MainActivity, "Gagal Mendapatkan Data", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onError(anError: ANError) {
                    anError.printStackTrace()
                    Toast.makeText(this@MainActivity, "Tidak Ada Jaringan Internet", Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun showListAyat() {
        ayatAdapter = AyatAdapter(this@MainActivity, modelAyat)
        rvAyat!!.adapter = ayatAdapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
        {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)

    }

}