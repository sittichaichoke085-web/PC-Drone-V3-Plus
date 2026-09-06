package com.pcdrone.v3plus

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            setBackgroundColor(Color.WHITE)
        }

        val title = TextView(this).apply {
            text = "PC DRONE"
            textSize = 34f
            setTextColor(Color.rgb(7, 91, 36))
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "PC Drone V3 Plus"
            textSize = 20f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
        }

        val status = TextView(this).apply {
            text = "Phase 1 • Android Skeleton"
            textSize = 16f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(status)

        setContentView(root)
    }
}
