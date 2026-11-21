package com.aymen.tiktokvoice

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 96, 48, 48)
        }

        val info = TextView(this).apply {
            text = "1) اسمح بالميكروفون\n2) فعّل الخدمة من تسهيلات الاستخدام\n3) افتح تيك توك وقل: التالي / السابق / وقف"
            textSize = 18f
        }

        val micBtn = Button(this).apply {
            text = "السماح بالميكروفون"
            setOnClickListener {
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            }
        }

        val accBtn = Button(this).apply {
            text = "فتح إعدادات تسهيلات الاستخدام"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        layout.addView(info)
        layout.addView(micBtn)
        layout.addView(accBtn)
        setContentView(layout)
    }
}
