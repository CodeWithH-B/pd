package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.theme.PhotoCraftTheme
import com.example.ui.PhotoCraftApp
import com.example.viewmodel.PhotoCraftViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: PhotoCraftViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhotoCraftTheme {
                PhotoCraftApp(viewModel = viewModel)
            }
        }
    }
}
