package com.example.qr_scanner

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.qr_scanner.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.checkerframework.checker.units.qual.m

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.scanBtn.setOnClickListener {
            scan()
        }
    }

    private fun scan() {
        val scan=IntentIntegrator(this)
        scan.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode==Activity.RESULT_OK){
            val result=IntentIntegrator.parseActivityResult(requestCode,resultCode,data)
            Log.d(TAG, result.contents)
            if(result!=null){
                if(result.contents!=null){
                    binding.text.visibility=View.VISIBLE
                    binding.text.text=result.contents
                    savedata(result.contents)
                    Toast.makeText(this,result.contents,Toast.LENGTH_SHORT)
                }else{
                    Toast.makeText(this,"cancelled",Toast.LENGTH_SHORT)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun savedata(contents: String) {
        val user=FirebaseAuth.getInstance().currentUser
        val firestore=FirebaseFirestore.getInstance()
        if (user != null) {
            GlobalScope.async{
                val snap=firestore.collection("data").document(user.uid).get().await()
                if(snap.data == null) {
                    val x = data(ArrayList())
                    x.qrdata.add(contents)
                    firestore.collection("data").document(user.uid).set(x)
                }else{
                     val x= snap.data!!.get("qrdata")
                    val y = data(ArrayList())
                    y.qrdata.addAll(x as ArrayList<String>)
                    y.qrdata.add(contents)
                    firestore.collection("data").document(user.uid).update("qrdata",y.qrdata)
                }
            }


        }
    }


}