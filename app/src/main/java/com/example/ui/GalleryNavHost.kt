package com.example.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun GalleryNavHost(
    viewModel: GalleryViewModel,
    updateTrigger: Int,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "gallery"
    ) {
        composable("gallery") {
            GalleryScreen(
                viewModel = viewModel,
                updateTrigger = updateTrigger,
                onMediaClick = { mediaId ->
                    navController.navigate("detail/$mediaId")
                }
            )
        }
        composable(
            route = "detail/{mediaId}",
            arguments = listOf(navArgument("mediaId") { type = NavType.LongType })
        ) { backStackEntry ->
            val mediaId = backStackEntry.arguments?.getLong("mediaId")
            val mediaItem = mediaId?.let { viewModel.getMediaItemById(it) }
            DetailScreen(
                mediaItem = mediaItem,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
