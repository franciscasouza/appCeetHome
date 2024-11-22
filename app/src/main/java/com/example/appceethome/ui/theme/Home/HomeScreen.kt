package com.example.appceethome.ui.theme.Home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.util.DebugLogger
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.appceethome.R

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val gifUrl = "https://i.ibb.co/WFhP3S4/XDZT.gif"
        //"https://i.ibb.co/FVJ53D6/Mr3W.gif" // URL do GIF

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        GlideImage(
            model = gifUrl, // Modelo do Glide (URL do GIF)
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.4f) // 50% da largura da tela
                .aspectRatio(1f),
            contentScale = ContentScale.Crop
        )
    }
}