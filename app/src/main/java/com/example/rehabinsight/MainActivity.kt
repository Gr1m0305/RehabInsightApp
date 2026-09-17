package com.example.rehabinsight

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.rehabinsight.ui.navigation.RehabNavGraph
import com.example.rehabinsight.ui.theme.RehabInsightTheme
import com.example.rehabinsight.data.network.testClientLogin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        enableEdgeToEdge()

        testClientLogin()

        setContent {
            RehabInsightTheme {
                RehabNavGraph()
            }
        }
    }
}
