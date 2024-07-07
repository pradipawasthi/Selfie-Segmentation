package com.pradip.selfiesegmentation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pradip.selfiesegmentation.ui.screen.CameraView
import com.pradip.selfiesegmentation.ui.screen.PermissionView
import com.pradip.selfiesegmentation.ui.screen.PreviewView
import com.pradip.selfiesegmentation.ui.theme.SelfieSegmentationTheme


@Composable
fun RootScreen() {
    val navController = rememberNavController()
    val currentBackStackEntryAsState by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntryAsState?.destination
    val context = LocalContext.current

    val darkMode = when (currentDestination?.route) {
        HOME_SCREEN_ROUTE, FORMATTED_COMPLETE_CREATOR_VIDEO_ROUTE, CAMERA_ROUTE, null -> true
        else -> false
    }

    if (currentDestination?.route == HOME_SCREEN_ROUTE) {
        BackHandler {
            (context as? Activity)?.finish()
        }
    }

    SelfieSegmentationTheme(darkTheme = darkMode) {
                    AppNavHost(navController = navController)
                }
}


@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = CAMERA_VIEW_ROUTE) {
//        composable(PERMISSION_VIEW_ROUTE) { PermissionView(navController) }
        composable(CAMERA_VIEW_ROUTE) { CameraView(navController) }
        composable(PREVIEW_VIEW_ROUTE) { PreviewView(navController) }
    }
}


const val PERMISSION_VIEW_ROUTE = "permission_view"
const val CAMERA_VIEW_ROUTE = "camera_view"
const val PREVIEW_VIEW_ROUTE = "preview_view"
const val HOME_SCREEN_ROUTE = "home_screen"
const val FORMATTED_COMPLETE_CREATOR_VIDEO_ROUTE = "formatted_complete_creator_video"
const val CAMERA_ROUTE = "camera_route"

