// File: app/src/main/java/com/proyek/maganggsp/presentation/EmptyActivity.kt

package com.proyek.maganggsp.presentation

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Minimal empty activity for build success
 * Displays simple message
 */
class EmptyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create simple text view programmatically (no XML needed)
        val textView = TextView(this).apply {
            text = "GesPay Admin\n\nBuild Successful!\n\nCore layers working."
            textSize = 18f
            setPadding(50, 50, 50, 50)
        }

        setContentView(textView)
    }
}
