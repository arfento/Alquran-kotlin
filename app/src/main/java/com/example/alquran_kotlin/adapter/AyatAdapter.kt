package com.example.alquran_kotlin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.azhar.quran.model.ModelAyat
import com.example.alquran_kotlin.R

class AyatAdapter(private val mContext : Context, private val items: List<ModelAyat>) : RecyclerView.Adapter<AyatAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v : View = LayoutInflater.from(parent.context).inflate(R.layout.list_item_ayat, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data : ModelAyat = items[position]
        holder.tvNomorAyat.text = data.nomor
        holder.tvArabic.text = data.arab
        holder.tvTerjemahan.text = data.terjemahan
    }

    override fun getItemCount(): Int {
        items.size
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var tvNomorAyat : TextView = v.findViewById(R.id.tvNomorAyat)
        var tvArabic : TextView = v.findViewById(R.id.tvArabic)
        var tvTerjemahan : TextView = v.findViewById(R.id.tvTerjemahan)

    }

}
