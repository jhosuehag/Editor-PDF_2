package com.jhosue.pdfeditor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jhosue.pdfeditor.ui.HomeScreen
import com.jhosue.pdfeditor.ui.ViewerScreen
import com.jhosue.pdfeditor.ui.theme.PDFEditorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge to edge fix for system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        
        enableEdgeToEdge()
        setContent {
            PDFEditorTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(onNavigateToViewer = { pathEncoded, fileNameEncoded ->
                navController.navigate("viewer/$pathEncoded/$fileNameEncoded")
            })
        }
        composable(
            route = "viewer/{pathEncoded}/{fileNameEncoded}",
            arguments = listOf(
                navArgument("pathEncoded") { type = NavType.StringType },
                navArgument("fileNameEncoded") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val pathEncoded = backStackEntry.arguments?.getString("pathEncoded") ?: ""
            val fileNameEncoded = backStackEntry.arguments?.getString("fileNameEncoded") ?: ""
            val fileName = try {
                java.net.URLDecoder.decode(fileNameEncoded, "UTF-8")
            } catch (e: Exception) {
                "documento.pdf"
            }
            ViewerScreen(
                fileName = fileName,
                uriString = pathEncoded,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}