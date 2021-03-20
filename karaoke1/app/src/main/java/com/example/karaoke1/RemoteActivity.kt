package com.example.karaoke1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.karaoke1.BluetoothUtils.Companion.onClickWrite
import com.example.karaoke1.MyApplication.Companion.mGatt
import kotlinx.android.synthetic.main.activity_remote.*

class RemoteActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote)


        btnTempoUp.setOnClickListener{
            Toast.makeText(this,"Tempo Up / 1",Toast.LENGTH_SHORT).show()
            //onClickWrite(mGatt,"1")
        }
        btnTempoDown.setOnClickListener{
            Toast.makeText(this,"Tempo Down / 2",Toast.LENGTH_SHORT).show()
            //onClickWrite(mGatt,"2")
        }
        btnVolumeUp.setOnClickListener{
            Toast.makeText(this,"Volume Up / 3",Toast.LENGTH_SHORT).show()
            //onClickWrite(mGatt,"3")
        }
        btnVolumeDown.setOnClickListener{
            Toast.makeText(this,"Volume Down / 4",Toast.LENGTH_SHORT).show()
            //builder.setView(dialogView)
            //onClickWrite(mGatt,"4")
        }
        btnPitchUp.setOnClickListener{
            Toast.makeText(this,"Pitch Up / 5",Toast.LENGTH_SHORT).show()
            //onClickWrite(mGatt,"5")
        }
        btnPitchDown.setOnClickListener{
            Toast.makeText(this,"Pitch Down / 6",Toast.LENGTH_SHORT).show()
            //onClickWrite(mGatt,"6")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.search_menu, menu)
        val mSearch : MenuItem= menu!!.findItem(R.id.search);

        //Log.d("TAG","${mSearch}")
        val sv:SearchView= mSearch.actionView as SearchView
        sv.isSubmitButtonEnabled

        sv.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                val builder=AlertDialog.Builder(this@RemoteActivity)
                val dialogView=layoutInflater.inflate(R.layout.search_result_dialog,null)
                builder.setView(dialogView).show()
                /*var result=ArrayList<String>()
                onClickWrite(mGatt,"search/${query}.toString()")
                */
                Toast.makeText(MyApplication.applicationContext(),"yes",Toast.LENGTH_SHORT).show()
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return true
            }

        })

        return true

    }
}