package com.pradip.selfiesegmentation.ui.screen

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.pradip.selfiesegmentation.CAMERA_VIEW_ROUTE

@Composable
fun PermissionView(navController: NavHostController) {
    // Implement the permission view here
    // Once permissions are granted, navigate to the CameraView
    Button(onClick = { navController.navigate(CAMERA_VIEW_ROUTE) }) {
        Text("Grant Permission")
    }
}