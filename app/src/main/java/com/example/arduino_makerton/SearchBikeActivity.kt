package com.example.arduino_makerton

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

class SearchBikeActivity : AppCompatActivity() {

    private lateinit var myFlag: Button
    private lateinit var greenFlag: Button
    private lateinit var blackFlag: Button
    private lateinit var redFlag: Button
    private lateinit var bikeCount: TextView
    private lateinit var bikeDummyCount: TextView
    private lateinit var parkingSpace: TextView
    private lateinit var myLock: ImageView

    private var parkingLotOneStatus = 0
    private var parkingLotTwoStatus = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_bike)

        myFlag = findViewById(R.id.myFlag)
        greenFlag = findViewById(R.id.greenFlag)
        blackFlag = findViewById(R.id.blackFlag)
        redFlag = findViewById(R.id.redFlag)
        bikeCount = findViewById(R.id.bikeCount)
        bikeDummyCount = findViewById(R.id.bikeDummyCount)
        parkingSpace = findViewById(R.id.parkingSpace)
        myLock = findViewById(R.id.myLock)

        bikeCount.visibility = GONE
        parkingSpace.visibility = GONE
        bikeDummyCount.visibility = GONE
        myLock.visibility = GONE

        parkingLotOneStatus = intent.getIntExtra("PARKING_LOT_ONE_STATUS", 0)
        parkingLotTwoStatus = intent.getIntExtra("PARKING_LOT_TWO_STATUS", 0)

        changeFlagColor()
        onClickFlag()
        onClickDummyFlag()
    }

    private fun changeFlagColor() {
        val colorResId = when {
            parkingLotOneStatus == 0 && parkingLotTwoStatus == 0 -> {
                bikeCount.text = "2대"
                myLock.visibility = GONE
                R.color.green
            }
            parkingLotOneStatus == 0 && parkingLotTwoStatus == 1 -> {
                bikeCount.text = "1대"
                myLock.visibility = GONE
                R.color.black
            }
            parkingLotOneStatus == 1 && parkingLotTwoStatus == 0 -> {
                bikeCount.text = "1대"
                myLock.visibility = VISIBLE
                R.color.black
            }
            else -> {
                bikeCount.text = "0대"
                myLock.visibility = VISIBLE
                R.color.red
            }
        }

        val drawable: Drawable = myFlag.background
        val wrappedDrawable = DrawableCompat.wrap(drawable)
        DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(this, colorResId))
        myFlag.background = wrappedDrawable
    }

    private fun onClickFlag(){
        myFlag.setOnClickListener {
            bikeDummyCount.visibility = GONE
            bikeCount.visibility = VISIBLE
            parkingSpace.visibility = VISIBLE
        }
    }

    private fun onClickDummyFlag(){
        greenFlag.setOnClickListener {
            bikeDummyCount.text = "6대"
            bikeDummyCount.visibility = VISIBLE
            bikeCount.visibility = GONE
            parkingSpace.visibility = VISIBLE
        }
        blackFlag.setOnClickListener {
            bikeDummyCount.text = "3대"
            bikeDummyCount.visibility = VISIBLE
            bikeCount.visibility = GONE
            parkingSpace.visibility = VISIBLE
        }
        redFlag.setOnClickListener {
            bikeDummyCount.text = "1대"
            bikeDummyCount.visibility = VISIBLE
            bikeCount.visibility = GONE
            parkingSpace.visibility = VISIBLE
        }
    }
}