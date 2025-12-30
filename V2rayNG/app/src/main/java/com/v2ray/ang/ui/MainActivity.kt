package com.v2ray.ang.ui

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.v2ray.ang.service.V2RayServiceManager
import com.v2ray.ang.util.AngConfigManager
import com.v2ray.ang.AppConfig
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    // 👇👇👇 บรรทัดที่คุณตามหาอยู่ตรงนี้ครับ (ผมใส่ลิงก์แจกฟรีให้แล้ว) 👇👇👇
    val CONFIG_URL = "https://raw.githubusercontent.com/aiboboxx/v2rayfree/main/v2"

    var serverList = mutableListOf<String>()
    lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#121212"))
        }

        val title = TextView(this).apply {
            text = "MST CLOUD VPN"
            textSize = 24f
            setTextColor(Color.CYAN)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 30)
        }

        statusText = TextView(this).apply {
            text = "กำลังดึงข้อมูล Server ฟรี..."
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 50)
        }
        
        val btnConnect = Button(this).apply {
            text = "รอโหลดสักครู่..."
            textSize = 18f
            setPadding(50, 40, 50, 40)
            setBackgroundColor(Color.DKGRAY)
            setTextColor(Color.WHITE)
            isEnabled = false 
        }

        layout.addView(title)
        layout.addView(statusText)
        layout.addView(btnConnect)
        setContentView(layout)

        // เริ่มโหลดรายการ Server ทันทีที่เปิดแอป
        thread {
            try {
                val rawData = URL(CONFIG_URL).readText()
                serverList = rawData.lines()
                    .filter { it.contains("vmess://") }
                    .filter { it.isNotBlank() }
                    .toMutableList()

                runOnUiThread {
                    if (serverList.isNotEmpty()) {
                        statusText.text = "✅ พร้อมใช้งาน! เจอ ${serverList.size} เซิร์ฟเวอร์"
                        btnConnect.text = "กดเพื่อเชื่อมต่อ"
                        btnConnect.isEnabled = true
                        btnConnect.setBackgroundColor(Color.RED)
                    } else {
                        statusText.text = "❌ ไม่พบข้อมูล Server (ลิงก์อาจเสีย)"
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    statusText.text = "⚠️ เชื่อมต่อเน็ตไม่ได้ กรุณาต่อเน็ตก่อนเข้าแอป"
                }
            }
        }

        btnConnect.setOnClickListener {
            if (V2RayServiceManager.v2rayPoint.isRunning) {
                V2RayServiceManager.stopV2Ray(this)
                btnConnect.text = "กดเพื่อเชื่อมต่อ"
                btnConnect.setBackgroundColor(Color.RED)
                statusText.text = "🔴 หยุดการทำงานแล้ว"
            } else {
                if (serverList.isNotEmpty()) {
                    try {
                        AngConfigManager.deleteServer(AppConfig.ANG_PACKAGE)
                        val randomConfig = serverList.random() // สุ่ม Server ใหม่ทุกครั้ง
                        val config = AngConfigManager.importShare(randomConfig)
                        
                        if (config != null) {
                            V2RayServiceManager.startV2Ray(this, config, null, null)
                            btnConnect.text = "🟢 CONNECTED"
                            btnConnect.setBackgroundColor(Color.GREEN)
                            statusText.text = "กำลังใช้: ${config.remarks}"
                        }
                    } catch (e: Exception) {
                        statusText.text = "Error: ลองกดใหม่อีกครั้ง"
                    }
                }
            }
        }
    }
}
