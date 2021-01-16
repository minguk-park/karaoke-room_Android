package com.example.qrtest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button3.setOnClickListener{
            val integrator=IntentIntegrator(this)
            integrator.setBeepEnabled(true)
            integrator.captureActivity=MyBarcodeReaderActivity::class.java
            integrator.initiateScan()
        }
    }

    fun startBarcodeReader(view: View){
        IntentIntegrator(this).initiateScan()
    }

    fun startBarcodeReaderCustom(view:View){    // QR코드 스캔의 인식률을 높이는 커스텀 리더
        val integrator=IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("QR코드를 스캔하여 주세요")      // 하단 QR코드 텍스트 커스텀
        integrator.setCameraId(0)               // 후면카메라 vs 전면카메라(1)
        integrator.setBeepEnabled(true)         // 스캔 소리 설정
        integrator.setBarcodeImageEnabled(true) // 결과에 저장된 이미지를 비트맵으로 저장
        integrator.initiateScan()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result=IntentIntegrator.parseActivityResult(requestCode,resultCode,data)
        if(result!=null){
            if(result.contents!=null){
                Toast.makeText(this,"scanned :  ${result.contents} format: ${result.formatName}",Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this,"Cancelled",Toast.LENGTH_LONG).show()
            }
        }else {
            super.onActivityResult(requestCode, resultCode, data)
        }

    }
}

