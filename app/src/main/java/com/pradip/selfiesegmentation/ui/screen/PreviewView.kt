package com.pradip.selfiesegmentation.ui.screen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.pradip.selfiesegmentation.CAMERA_VIEW_ROUTE
import com.pradip.selfiesegmentation.PREVIEW_VIEW_ROUTE
import com.pradip.selfiesegmentation.extension.cropToCircle
import com.pradip.selfiesegmentation.extension.rotateBitmapIfNeeded
import com.pradip.selfiesegmentation.ui.viewmodel.CameraMediaViewModel
import java.io.File

@Composable
fun PreviewView(navController: NavHostController,
                cameraMediaViewModel: CameraMediaViewModel = hiltViewModel()
) {

    BackHandler {
        navController.navigate(CAMERA_VIEW_ROUTE) {
            popUpTo(PREVIEW_VIEW_ROUTE) { inclusive = true }
        }
    }

    val context = LocalContext.current
    val photoFile = File(context.externalCacheDir, "captured_image.jpg")

    if (photoFile.exists()) {
        val originalBitmap = BitmapFactory.decodeFile(photoFile.path)
        val rotatedCroppedBitmap = originalBitmap?.let {
            rotateAndCropToCircle(
                it,
                photoFile.absolutePath,
                0.8f,
                0.1f
            )
        }

//        val croppedBitmap = cropBitmap(bitmap, 100, 100, 200, 200) // Change these values as needed

        rotatedCroppedBitmap?.asImageBitmap()?.let {
            Image(
                bitmap = it,
                contentDescription = "Captured Image",
                modifier = Modifier.fillMaxSize()
            )
            cameraMediaViewModel.segmentImage(it.asAndroidBitmap())
        }
    } else {
        Text("No image captured")
    }
}

//fun cropBitmap(source: Bitmap, left: Int, top: Int, width: Int, height: Int): Bitmap {
//    return Bitmap.createBitmap(source, left, top, width, height)
//}

fun rotateAndCropToCircle(bitmap: Bitmap, filePath: String, scale: Float, offset: Float): Bitmap? {
    val rotatedBitmap = rotateBitmapIfNeeded(bitmap, filePath)
    return rotatedBitmap.let {
        cropToCircle(it, scale, offset)
    }
}
