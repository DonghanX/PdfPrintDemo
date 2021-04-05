package com.redhood.pdfprintdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.redhood.pdfprintdemo.databinding.ActivityMainBinding
import com.redhood.pdfprintdemo.utils.PdfPrintUtils

class MainActivity : AppCompatActivity() {

    private fun getTAG() = "MainActivity"

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)


        setContentView(binding.root)

        initClickListener()
    }

    private fun initClickListener() {
        binding.printButton.setOnClickListener {
            Log.e(getTAG(), "onPdfPrintClick: ")
            onPdfPrintClick()
        }
    }

    private fun onPdfPrintClick() {
        PdfPrintUtils.generatePdfManually(binding.printContainer)
    }


}