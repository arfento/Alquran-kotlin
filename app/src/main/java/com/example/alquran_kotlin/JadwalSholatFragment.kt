package com.example.alquran_kotlin

import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CalendarView
import android.widget.Spinner
import androidx.core.content.ContextCompat
import com.azhar.quran.model.DaftarKota
import com.example.alquran_kotlin.utils.ClientAsyncTask
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vivekkaushik.datepicker.DatePickerTimeline
import kotlinx.android.synthetic.main.fragment_jadwal_sholat.*
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 * Use the [JadwalSholatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class JadwalSholatFragment : BottomSheetDialogFragment() {

    var mString : String? = null
    private var listDaftarKota : MutableList<DaftarKota>? = null
    private var mDaftarKotaAdapter : ArrayAdapter<DaftarKota>? = null
    var progDialog : ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mString = requireArguments().getString("detail")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v : View =  inflater.inflate(R.layout.fragment_jadwal_sholat, container, false)

        progDialog = ProgressDialog(activity)
        progDialog!!.setTitle("Mohon Tunggu")
        progDialog!!.setCancelable(false)
        progDialog!!.setMessage("Sedang menampilkan data...")

        val spKota : Spinner = v.findViewById(R.id.spinKota)
        listDaftarKota = ArrayList()
        mDaftarKotaAdapter = ArrayAdapter(requireActivity().applicationContext,
            android.R.layout.simple_spinner_item,
            listDaftarKota as ArrayList<DaftarKota>)
        mDaftarKotaAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spKota.adapter = mDaftarKotaAdapter
        spKota.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                loadJadwal(spinKota!!.id)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        val datePickerDialog : DatePickerTimeline = v.findViewById(R.id.dateTimeline)
        val date = Calendar.getInstance()
        val mYear : Int = date.get(Calendar.YEAR)
        val mMonth : Int = date.get(Calendar.MONTH)
        val mDay : Int = date.get(Calendar.DAY_OF_MONTH)

        datePickerDialog.setInitialDate(mYear, mMonth, mDay)
        datePickerDialog.setDisabledDateColor(ContextCompat.getColor(requireActivity(), R.color.colorAccent))
        datePickerDialog.setActiveDate(date)

        val dates = arrayOf(Calendar.getInstance().time)
        datePickerDialog.deactivateDates(dates)

        loadKota()


        return v
    }

    private fun loadKota() {
        try {
            progDialog!!.show()
            val url = "https://api.banghasan.com/sholat/format/json/kota"
            val task = ClientAsyncTask(this, object : ClientAsyncTask.OnPostExecuteListener{
                override fun onPostExecute(result: String) {
                    try {
                        progDialog!!.dismiss()
                        val jsonObject = JSONObject(result)
                        val jsonArray = jsonObject.getJSONArray("kota")
                        var daftarKota : DaftarKota?
                        for (i in 0 until jsonArray.length()){
                            val obj = jsonArray.getJSONObject(i)
                            daftarKota = DaftarKota()
                            daftarKota.id = obj.getInt("id")
                            daftarKota.nama = obj.getString("nama")
                            listDaftarKota!!.add(daftarKota)
                        }
                        mDaftarKotaAdapter!!.notifyDataSetChanged()
                    }catch (ex : JSONException)
                    {
                        ex.printStackTrace()
                    }
                }
            })
            task.execute(url)
        }catch (ex: Exception){
            ex.printStackTrace()
        }
    }

    private fun loadJadwal(id: Int) {
        try {
            progDialog!!.show()
            val idKota = id.toString()
            val current = SimpleDateFormat("yyyy-MM-dd")
            val tanggal = current.format(Date())
            val url = "https://api.banghasan.com/sholat/format/json/jadwal/kota/$idKota/tanggal/$tanggal"
            val task  = ClientAsyncTask(this, object : ClientAsyncTask.OnPostExecuteListener {
                override fun onPostExecute(result: String) {
                    try {
                        progDialog!!.dismiss()
                        val jsonObject = JSONObject(result)
                        val objJadwal = jsonObject.getJSONObject("jadwal")
                        val objData = jsonObject.getJSONObject("data")

                        tv_subuh.text = objData.getString("subuh")
                        tv_dzuhur.text = objData.getString("dzuhur")
                        tv_ashar.text = objData.getString("ashar")
                        tv_maghrib.text = objData.getString("maghrib")
                        tv_isya.text = objData.getString("isya")
                    }catch (e : JSONException){
                        e.printStackTrace()
                    }
                }

            } )

        }catch (e : Exception){
            e.printStackTrace()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireView().parent as View).setBackgroundColor(Color.TRANSPARENT)
    }

    public companion object{
        @JvmStatic
        fun newInstance(string : String?): JadwalSholatFragment {
            val args = Bundle()
            val f = JadwalSholatFragment()
//            val fragment = ()

            args.putString("detail", string)
            f.arguments = args
            return f
        }
    }


}