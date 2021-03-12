package com.safal.tinderswipejava.OwnCode

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.safal.tinderswipejava.R

class TouristSpotCardAdapter(context: Context?) : ArrayAdapter<TouristSpot?>(context!!, 0) {
    override fun getView(position: Int, contentView: View?, parent: ViewGroup): View {
        var contentView = contentView
        val holder: ViewHolder
        if (contentView == null) {
            val inflater = LayoutInflater.from(context)
            contentView = inflater.inflate(R.layout.item_tourist_spot_card, parent, false)
            holder = ViewHolder(contentView)
            contentView.tag = holder
        } else {
            holder = contentView.tag as ViewHolder
        }
        val spot = getItem(position)
        holder.name.text = spot!!.name
        holder.city.text = spot.city
        Glide.with(context).load(spot.url).into(holder.image)
        return contentView!!
    }

    private class ViewHolder(view: View?) {
        var name: TextView
        var city: TextView
        var image: ImageView

        init {
            name = view!!.findViewById<View>(R.id.item_tourist_spot_card_name) as TextView
            city = view.findViewById<View>(R.id.item_tourist_spot_card_city) as TextView
            image = view.findViewById<View>(R.id.item_tourist_spot_card_image) as ImageView
        }
    }
}