// File: app/src/main/java/com/proyek/maganggsp/presentation/login/SplashActivity.kt
// (Fix typo: SplashActicity → SplashActivity)

package com.proyek.maganggsp.presentation.login

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Minimal splash activity - no XML layout needed
 */
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create simple text view programmatically (NO XML NEEDED!)
        val textView = TextView(this).apply {
            text = "GesPay Admin\n\n✅ BUILD SUCCESSFUL!\n\nCore Architecture Ready"
            textSize = 20f
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            setPadding(50, 200, 50, 50)
        }

        setContentView(textView)
    }
}
