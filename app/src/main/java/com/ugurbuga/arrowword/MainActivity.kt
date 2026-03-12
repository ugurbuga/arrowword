package com.ugurbuga.arrowword

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ugurbuga.arrowword.ui.app.ArrowwordApp
import com.ugurbuga.arrowword.ui.theme.ArrowwordTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArrowwordTheme {
                ArrowwordApp()
            }
        }
    }
}