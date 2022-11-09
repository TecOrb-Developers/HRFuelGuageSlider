package com.hrsliderfuel

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.hrfuelwidget.HRFuelSliderDialog
import com.hrsliderfuel.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        context = this
        binding.btnSlider.setOnClickListener {
            HRFuelSliderDialog.newInstance {
                binding.textView.text = "Fuel Level: $it"
            }.show(supportFragmentManager, "fuelDialog")
        }
    }
}