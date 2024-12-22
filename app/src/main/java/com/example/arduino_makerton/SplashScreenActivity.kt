package com.example.arduino_makerton


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {

    // Handler와 Runnable을 사용하여 지연 작업을 수행합니다.
    private val splashScreenDelay: Long = 2000 // 2초 지연

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // 2초 후에 MainActivity로 이동
        Handler().postDelayed({
            // MainActivity를 시작합니다.
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            // 현재 SplashScreenActivity를 종료합니다.
            finish()
        }, splashScreenDelay)
    }
}