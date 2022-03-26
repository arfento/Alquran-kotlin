package com.example.alquran_kotlin.utils

import android.location.Address
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class ParserPlace {

    fun parse (jsonObject: JSONObject): List<HashMap<String, String>>{
        var jPlaces : JSONArray? = null
        try {
            jPlaces = jsonObject.getJSONArray("results")
        }catch (ex : JSONException){
            ex.printStackTrace()
        }

        return getPlaces(jPlaces)
    }

    private fun getPlaces(jPlaces: JSONArray?): List<HashMap<String, String>> {

        val placesCount = jPlaces!!.length()
        val placeList : MutableList<HashMap<String, String>> = ArrayList()
        var place: HashMap<String, String>

        for (i in 0 until placesCount){
            try {
                place  = getPlace(jPlaces[i] as JSONObject)
                placeList.add(place)

            }catch (e : JSONException){
                e.printStackTrace()
            }
        }
        return placeList
    }

    private fun getPlace(jsonObject: JSONObject): java.util.HashMap<String, String> {

        val place = HashMap<String, String>()
        var placeName = "-NA-"
        var vicinity = "-NA-"
        var Address = "-NA-"
        var latitude = ""
        var longitude = ""


        try {
            if (!jsonObject.isNull("name")){
                placeName = jsonObject.getString("name")
            }
            if (!jsonObject.isNull("vicinity")){
                vicinity = jsonObject.getString("vicinity")
            }
            if (!jsonObject.isNull("formatted_address")) {
                Address = jsonObject.getString("formatted_address")
            }

            latitude = jsonObject.getJSONObject("geometry").getJSONObject("location").
                    getString("lat")

            longitude = jsonObject.getJSONObject("geometry").getJSONObject("location").
                    getString("lng")
            place["place_name"] = placeName
            place["vicinity"] = vicinity
            place["Address"] = Address
            place["lat"] = latitude
            place["lng"] = longitude
        }catch (e : JSONException){
            e.printStackTrace()
        }
        return place
    }


}