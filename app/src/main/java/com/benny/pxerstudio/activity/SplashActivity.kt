package com.benny.pxerstudio.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

import com.benny.pxerstudio.R
import com.benny.pxerstudio.util.Tool

class SplashActivity : AppCompatActivity() {

    private var handler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.benny.pxerstudio.R.layout.activity_splash)

//        (findViewById<View>(R.id.iv) as View).animate().alpha(1f).scaleY(1.1f).scaleX(1.1f).setDuration(2000L).interpolator = AccelerateDecelerateInterpolator()
        (findViewById<View>(R.id.tv) as View).animate().alpha(1f).scaleY(1.1f).scaleX(1.1f).setDuration(2000L).interpolator = AccelerateDecelerateInterpolator()

        handler = Handler()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            handler!!.postDelayed({
                startActivity(Intent(this@SplashActivity, SelectImageActivity::class.java))
                finish()
            }, 2000L)
        else
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 0x456)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 0x456) {
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    Tool.toast(this, "Sorry this application require storage permission for saving your project")
                    handler!!.postDelayed({ recreate() }, 1000)
                    return
                }
            }
            handler!!.postDelayed({
                startActivity(Intent(this@SplashActivity, SelectImageActivity::class.java))
                finish()
            }, 2000L)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
