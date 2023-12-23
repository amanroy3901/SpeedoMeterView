package com.example.speedoMeter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.speedoMeter.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var  binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        binding.button.setOnClickListener {
            val speed = binding.editTextNumber.text.toString().toInt()
            binding.speedometerView.setSpeed(speed)
        }


//        val intent = Intent(this,BackgroundServices::class.java)
//        startService(intent)
    }
}