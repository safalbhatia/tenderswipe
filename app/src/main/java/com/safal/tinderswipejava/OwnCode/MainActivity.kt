package com.safal.tinderswipejava.OwnCode

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.safal.tinderswipejava.R
import com.safal.tinderswipejava.tinder.CardStackView
import com.safal.tinderswipejava.tinder.CardStackView.CardEventListener
import com.safal.tinderswipejava.tinder.SwipeDirection
import java.util.*

class MainActivity : AppCompatActivity() {
    private var progressBar: ProgressBar? = null
    private var cardStackView: CardStackView? = null
    private var adapter: TouristSpotCardAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setup()
        reload()
    }

    private fun createTouristSpots(): List<TouristSpot> {
        val spots: MutableList<TouristSpot> = ArrayList()
        spots.add(TouristSpot("Yasaka Shrine", "Kyoto", "https://source.unsplash.com/Xq1ntWruZQI/600x800"))
        spots.add(TouristSpot("Fushimi Inari Shrine", "Kyoto", "https://source.unsplash.com/NYyCqdBOKwc/600x800"))
        spots.add(TouristSpot("Bamboo Forest", "Kyoto", "https://source.unsplash.com/buF62ewDLcQ/600x800"))
        spots.add(TouristSpot("Brooklyn Bridge", "New York", "https://source.unsplash.com/THozNzxEP3g/600x800"))
        spots.add(TouristSpot("Empire State Building", "New York", "https://source.unsplash.com/USrZRcRS2Lw/600x800"))
        spots.add(TouristSpot("The statue of Liberty", "New York", "https://source.unsplash.com/PeFk7fzxTdk/600x800"))
        spots.add(TouristSpot("Louvre Museum", "Paris", "https://source.unsplash.com/LrMWHKqilUw/600x800"))
        spots.add(TouristSpot("Eiffel Tower", "Paris", "https://source.unsplash.com/HN-5Z6AmxrM/600x800"))
        spots.add(TouristSpot("Big Ben", "London", "https://source.unsplash.com/CdVAUADdqEc/600x800"))
        spots.add(TouristSpot("Great Wall of China", "China", "https://source.unsplash.com/AWh9C-QjhE4/600x800"))
        return spots
    }

    private fun createTouristSpotCardAdapter(): TouristSpotCardAdapter {
        val adapter = TouristSpotCardAdapter(applicationContext)
        adapter.addAll(createTouristSpots())
        return adapter
    }

    private fun setup() {
        progressBar = findViewById(R.id.activity_main_progress_bar)
        cardStackView = findViewById(R.id.activity_main_card_stack_view)
        cardStackView!!.setCardEventListener(object : CardEventListener {
            override fun onCardDragging(percentX: Float, percentY: Float) {
                Log.d("CardStackView", "onCardDragging")
            }

            override fun onCardSwiped(direction: SwipeDirection) {
                Log.d("CardStackView", "onCardSwiped: $direction")
                Log.d("CardStackView", "topIndex: " + cardStackView!!.getTopIndex())
                if (cardStackView!!.getTopIndex() == adapter!!.count - 5) {
                    Log.d("CardStackView", "Paginate: " + cardStackView!!.getTopIndex())
                    paginate()
                }
            }

            override fun onCardReversed() {
                Log.d("CardStackView", "onCardReversed")
            }

            override fun onCardMovedToOrigin() {
                Log.d("CardStackView", "onCardMovedToOrigin")
            }

            override fun onCardClicked(index: Int) {
                Log.d("CardStackView", "onCardClicked: $index")
            }
        })
    }

    private fun reload() {
        cardStackView!!.visibility = View.GONE
        progressBar!!.visibility = View.VISIBLE
        Handler().postDelayed({
            adapter = createTouristSpotCardAdapter()
            cardStackView!!.setAdapter(adapter)
            cardStackView!!.visibility = View.VISIBLE
            progressBar!!.visibility = View.GONE
        }, 1000)
    }

    private fun paginate() {
        cardStackView!!.setPaginationReserved()
        adapter!!.addAll(createTouristSpots())
        adapter!!.notifyDataSetChanged()
    }
}