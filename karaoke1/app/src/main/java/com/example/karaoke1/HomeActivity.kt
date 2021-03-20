package com.example.karaoke1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        btnRemote.setOnClickListener {
            val intent= Intent(this,RemoteActivity::class.java)
            startActivity(intent)
        }
    }
}