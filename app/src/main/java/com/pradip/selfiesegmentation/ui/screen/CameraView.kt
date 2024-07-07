package com.pradip.selfiesegmentation.ui.screen

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pradip.selfiesegmentation.R

import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import com.pradip.selfiesegmentation.ui.theme.LightGreenColor
import com.pradip.selfiesegmentation.ui.theme.TealColor
import com.pradip.selfiesegmentation.ui.viewmodel.CameraMediaViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import com.pradip.selfiesegmentation.CAMERA_VIEW_ROUTE
import com.pradip.selfiesegmentation.PREVIEW_VIEW_ROUTE
import com.pradip.selfiesegmentation.extension.clickableSingle
import com.pradip.selfiesegmentation.extension.createFile
import com.pradip.selfiesegmentation.extension.decodeBitmapFromUri
import com.pradip.selfiesegmentation.ui.theme.SelfieSegmentationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraView(
    navController: NavController,
    cameraMediaViewModel: CameraMediaViewModel = hiltViewModel()

) {
    val context = LocalContext.current
    val multiplePermissionState = rememberMultiplePermissionsState(
        permissions = listOf(android.Manifest.permission.CAMERA)
    )

    LaunchedEffect(Unit) {
        if (!multiplePermissionState.permissions[0].status.isGranted) {
            multiplePermissionState.launchMultiplePermissionRequest()
        }
    }

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = {}
    )

    Column(modifier = Modifier.fillMaxSize()) {
        if (multiplePermissionState.permissions[0].status.isGranted) {
            CameraPreview(
                onClickCancel = { navController.navigateUp() },
                onClickOpenFile = {
                    fileLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                cameraMediaViewModel,
                navController
            )
        } else {
            CameraPermissionRequestPage(
                onClickCancel = { navController.navigateUp() },
                onClickOpenFile = {
                    fileLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onClickGrantPermission = {
                    val permissionState = multiplePermissionState.permissions[0]
                    if (permissionState.status.shouldShowRationale) {
                        permissionState.launchPermissionRequest()
                    } else {
                        context.openAppSetting()
                    }
                }
            )
        }
    }
}

@Composable
fun CameraPermissionRequestPage(
    onClickCancel: () -> Unit,
    onClickOpenFile: () -> Unit,
    onClickGrantPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(listOf(TealColor, LightGreenColor)))
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_cancel),
            contentDescription = null,
            modifier = Modifier
                .padding(start = 16.dp)
                .size(24.dp)
                .align(Alignment.Start)
                .clickable { onClickCancel() }
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.allow_app_to_access_camera),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.75f)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.camera_permission_rationale),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.75f),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(30.dp))

        Card(
            modifier = Modifier.fillMaxWidth(0.75f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically)
            ) {
                Row(
                    modifier = Modifier.clickable { onClickGrantPermission() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_camera),
                        contentDescription = null
                    )
                    Text(
                        text = stringResource(id = R.string.access_camera),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onClickOpenFile) {
            Text(text = stringResource(id = R.string.upload_from_gallery))
        }
    }
}

@Composable
fun CameraPreview(
    onClickCancel: () -> Unit,
    onClickOpenFile: () -> Unit,
    segmentationViewModel: CameraMediaViewModel,
    navController : NavController
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture =
        remember { ProcessCameraProvider.getInstance(context) }
    var defaultCameraFacing by remember { mutableStateOf(CameraSelector.DEFAULT_FRONT_CAMERA) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
//    var imageCapture = remember {
//        ImageCapture.Builder()
////            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
//            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
//            .setJpegQuality(50) // Adjust JPEG quality to reduce file size
//            .build()
//    }

    val cameraProvider = cameraProviderFuture.get()
    val preview = remember { Preview.Builder().build() }
    val cameraExecutor = ContextCompat.getMainExecutor(context)

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                        .height(150.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val coroutineScope = rememberCoroutineScope()

                    Image(
                        modifier = Modifier
                            .size(42.dp)
                            .clickable { onClickOpenFile() },
                        painter = painterResource(id = R.drawable.ic_gallery),
                        contentDescription = "",
                    )

                    Image(
                        modifier = Modifier
                            .size(72.dp)
                            .clickableSingle {
                                coroutineScope.launch {
//                                    val photoFile = createFile(context)
                                    val photoFile = File(context.externalCacheDir, "captured_image.jpg")

                                    val outputOptions =
                                        ImageCapture.OutputFileOptions
                                            .Builder(photoFile)
                                            .build()
                                    imageCapture?.takePicture(
                                        outputOptions,
                                        cameraExecutor,
                                        object : ImageCapture.OnImageSavedCallback {
                                            override fun onImageSaved(
                                                output: ImageCapture.OutputFileResults
                                            ) {
                                                val savedUri = output.savedUri ?: return

                                                coroutineScope.launch(Dispatchers.IO) {
                                                    val originalBitmap = decodeBitmapFromUri(savedUri)

                                                    if (originalBitmap != null) {
                                                           val segmentedBitmap =      segmentationViewModel.segmentImage(
                                                                    originalBitmap
                                                                )


//                                                    val segmentedBitmap = originalBitmap?.let {
//                                                        segmentationViewModel.segmentImage(it)
//                                                    }

                                                        segmentedBitmap?.let {
                                                            withContext(Dispatchers.Main) {
//                                                            SegmentedImage(it, segmentationViewModel)
                                                                Log.d("temp:::", it.toString())
                                                            }
                                                        }
                                                    }
                                                }

//                                                navController.navigate(PREVIEW_VIEW_ROUTE) {
//                                                    popUpTo(CAMERA_VIEW_ROUTE) { inclusive = true }
//                                                }
//                                                coroutineScope.launch(Dispatchers.IO) {
//                                                    val originalBitmap =
//                                                        decodeBitmapFromUri(savedUri)
//                                                    val rotatedCroppedBitmap = originalBitmap?.let {
//                                                        rotateAndCropToCircle(
//                                                            it,
//                                                            photoFile.absolutePath,
//                                                            0.8f,
//                                                            0.1f
//                                                        )
//                                                    }
//
//                                                    rotatedCroppedBitmap?.let {
//                                                        val compressedImageFile =
//                                                            compressBitmapToFile(
//                                                                it
//                                                            )
//                                                        withContext(Dispatchers.Main) {
//                                                            val updatedUri =
//                                                                Uri.fromFile(compressedImageFile)
//                                                            segmentationViewModel.saveImageUri(
//                                                                updatedUri,
//                                                                cameraOpenType
//                                                            )
//                                                        }
//                                                    }
//                                                }
                                            }

                                            override fun onError(exception: ImageCaptureException) {
                                                Toast.makeText(
                                                    context,
                                                    "Failed to capture image",
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                            }
                                        },
                                    )
                                }
                            },
                        painter = painterResource(id = R.drawable.ic_capture),
                        contentDescription = "",
                    )

                    Image(
                        modifier = Modifier
                            .size(42.dp)
                            .clickable {
                                defaultCameraFacing =
                                    if (defaultCameraFacing ==
                                        CameraSelector.DEFAULT_FRONT_CAMERA
                                    ) {
                                        CameraSelector.DEFAULT_BACK_CAMERA
                                    } else {
                                        CameraSelector.DEFAULT_FRONT_CAMERA
                                    }
                                bindCameraUseCases(
                                    cameraProvider = cameraProvider,
                                    lifecycleOwner = lifecycleOwner,
                                    defaultCameraFacing = defaultCameraFacing,
                                    preview = preview,
                                    imageCapture = {
                                            imageCapture = it
                                    },
                                )
                            },
                        painter = painterResource(id = R.drawable.ic_rotate_camera),
                        contentDescription = "",
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            val density = LocalDensity.current
            var cameraHeight by remember { mutableStateOf<Dp?>(null) }
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    previewView.scaleType = PreviewView.ScaleType.FIT_START
                    cameraProviderFuture.addListener({
                        preview.also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        bindCameraUseCases(
                            cameraProvider = cameraProvider,
                            lifecycleOwner = lifecycleOwner,
                            defaultCameraFacing = defaultCameraFacing,
                            preview = preview,
                            imageCapture = {
                                    imageCapture = it

                            },
                        )
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned {
                        cameraHeight = with(density) { it.size.height.toDp() }
                    },
            )

            // Circular Cutout Overlay
            if (cameraHeight != null) {
                CircularCutoutOverlay(
                    Modifier.fillMaxWidth().height(cameraHeight!!)
                )
            }

            // Close Button
            Box(
                modifier = Modifier.padding(16.dp),
            ) {
                Image(
                    modifier = Modifier
                        .size(42.dp)
                        .clickable { onClickCancel() },
                    painter = painterResource(id = R.drawable.ic_close_circle),
                    contentDescription = "",
                )
            }

            Text(
                text = stringResource(id = R.string.place_your_face_inside_the_circle),
                color = Color.Black,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 30.dp),
                textAlign = TextAlign.Center
            )
        }

    }
}


private fun bindCameraUseCases(
    cameraProvider: ProcessCameraProvider,
    lifecycleOwner: LifecycleOwner,
    defaultCameraFacing: CameraSelector,
    preview: Preview,
    imageCapture: (ImageCapture?) -> Unit,
) {
    try {
        cameraProvider.unbindAll()
        val imageCaptureInstance = ImageCapture.Builder()
//            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
//            .setJpegQuality(50) // Adjust JPEG quality to reduce file size
            .build()//ImageCapture.Builder().build()
        imageCapture(imageCaptureInstance)
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            defaultCameraFacing,
            preview,
            imageCaptureInstance
        )
    } catch (e: Exception) {
        Log.e("camera", "camera preview exception :${e.message}")
    }
}

@Composable
fun CircularCutoutOverlay(
    modifier: Modifier = Modifier,
    circleDiameterFraction: Float = 0.8f, // fraction of screen width
    topMarginFraction: Float = 0.1f, // fraction of screen height
    borderWidth: Dp = 1.dp,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val circleDiameter = size.width * circleDiameterFraction
        val topMargin = size.height * topMarginFraction
        val centerX = size.width / 2
        val centerY = topMargin + circleDiameter / 2

        // Draw the semi-transparent overlay
        drawRect(
            color = Color.Black.copy(alpha = 0.6f),
            size = size,
        )

        // Clear the area inside the circle
        drawCircle(
            color = Color.Transparent,
            radius = circleDiameter / 2,
            center = Offset(centerX, centerY),
            blendMode = BlendMode.Clear,
        )

        // Draw the border of the circle
        drawCircle(
            color = Color.White,
            radius = circleDiameter / 2,
            center = Offset(centerX, centerY),
            style = Stroke(width = borderWidth.toPx()),
        )
    }
}

fun Context.openAppSetting() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", packageName, null)
    intent.data = uri
    startActivity(intent)
}
