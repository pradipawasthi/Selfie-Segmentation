package com.pradip.selfiesegmentation.ui.screen

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.pradip.selfiesegmentation.PREVIEW_VIEW_ROUTE

@Composable
fun CameraView(navController: NavHostController) {
    // Implement the camera view here
    // Once the image is captured, navigate to the PreviewView
    Button(onClick = { navController.navigate(PREVIEW_VIEW_ROUTE) }) {
        Text("Capture Image")
    }
}