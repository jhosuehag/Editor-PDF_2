package com.jhosue.pdfeditor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ViewerScreen(
    fileName: String = "Constancia_Matricula_2026.pdf",
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
        containerColor = Color(0xFF111113)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // TOP TOOLBAR
            TopToolbar(fileName, onBackClick)
            
            // PDF AREA
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A1E))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    PdfSheet()
                }
            }
            
            // BOTTOM TOOLBAR
            BottomToolbar()
        }
    }
}

@Composable
fun TopToolbar(fileName: String, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(Color(0xFF161619))
            .drawBehindBorder(bottom = 1.dp, color = Color(0xFF222228))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ToolbarButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            onClick = onBackClick
        )
        
        Text(
            text = fileName,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFC8C8D8),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = "1 / 1",
            fontSize = 10.sp,
            color = Color(0xFF555560),
            fontFamily = FontFamily.Monospace
        )
        
        ToolbarButton(
            icon = Icons.Default.Search,
            onClick = { }
        )
    }
}

@Composable
fun ToolbarButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E1E24))
            .border(1.dp, Color(0xFF2A2A32), RoundedCornerShape(8.dp))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFAAAAAA),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun PdfSheet() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(Color.White, RoundedCornerShape(4.dp))
            .border(1.dp, Color(0xFF2A2A32), RoundedCornerShape(4.dp))
            .defaultMinSize(minHeight = 600.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Placeholder for PDF content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Vista previa del PDF",
                color = Color.LightGray,
                fontSize = 14.sp
            )
        }
        
        // OVERLAYS
        Box(modifier = Modifier.matchParentSize()) {
            EditableField(x = 20, y = 80, w = 160, h = 12)
            EditableField(x = 20, y = 100, w = 130, h = 12, isActive = true)
            EditableField(x = 20, y = 120, w = 110, h = 12)
            EditableField(x = 160, y = 120, w = 50, h = 12)
        }
    }
}

@Composable
fun EditableField(x: Int, y: Int, w: Int, h: Int, isActive: Boolean = false) {
    Box(
        modifier = Modifier
            .absoluteOffset(x = x.dp, y = y.dp)
            .size(width = w.dp, height = h.dp)
            .background(
                Color(0xFF6366F1).copy(alpha = if (isActive) 0.15f else 0.08f),
                RoundedCornerShape(2.dp)
            )
            .border(
                width = if (isActive) 2.dp else 1.5.dp,
                color = Color(0xFF6366F1),
                shape = RoundedCornerShape(2.dp)
            )
    ) {
        if (isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 3.dp)
                    .width(1.5.dp)
                    .fillMaxHeight(0.6f)
                    .background(Color(0xFF6366F1))
            )
        }
    }
}

@Composable
fun BottomToolbar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(Color(0xFF161619))
            .drawBehindBorder(top = 1.dp, color = Color(0xFF222228))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        BottomToolbarAction(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            label = "Anterior",
            color = Color(0xFF555560)
        )
        
        Button(
            onClick = { },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Text(
                text = "Guardar PDF",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
        
        BottomToolbarAction(
            icon = Icons.AutoMirrored.Filled.ArrowForward,
            label = "Siguiente",
            color = Color(0xFF6366F1)
        )
    }
}

@Composable
fun BottomToolbarAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickableNoRipple { }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            fontSize = 8.sp,
            color = color
        )
    }
}

// Extension to draw only certain borders
fun Modifier.drawBehindBorder(
    top: androidx.compose.ui.unit.Dp = 0.dp,
    bottom: androidx.compose.ui.unit.Dp = 0.dp,
    color: Color
): Modifier = this.drawBehind {
    val strokeWidth = 1.dp.toPx()
    if (top > 0.dp) {
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(size.width, 0f),
            strokeWidth = strokeWidth
        )
    }
    if (bottom > 0.dp) {
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(0f, size.height),
            end = androidx.compose.ui.geometry.Offset(size.width, size.height),
            strokeWidth = strokeWidth
        )
    }
}

// Utility for no-ripple clickable (better for toolbars)
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = this.then(
    Modifier.clickable(
        interactionSource = null,
        indication = null,
        onClick = onClick
    )
)
