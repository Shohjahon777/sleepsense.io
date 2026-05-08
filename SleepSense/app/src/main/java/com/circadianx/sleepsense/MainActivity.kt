package com.circadianx.sleepsense

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.circadianx.sleepsense.navigation.SleepSenseNavGraph
import com.circadianx.sleepsense.service.SleepTrackingService
import com.circadianx.sleepsense.ui.theme.SleepSenseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Phase 1: keep passive sleep/wake tracking running.
        startForegroundService(Intent(this, SleepTrackingService::class.java))

        setContent {
            SleepSenseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SleepSenseTheme.colors.bgDeep
                ) {
                    SleepSenseNavGraph()
                }
            }
        }
    }
}
