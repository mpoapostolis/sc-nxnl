package com.skincoach.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.skincoach.app.ui.navigation.SkinCoachNavHost
import com.skincoach.app.ui.theme.SkinCoachTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SkinCoachTheme {
                SkinCoachNavHost()
            }
        }
    }
}
