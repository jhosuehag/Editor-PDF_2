package com.jhosue.pdfeditor.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Model for editable PDF fields
data class EditablePdfField(
    val id: Int,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val originalText: String,
    val currentText: MutableState<String>
)

enum class ExportOption { DEVICE, SHARE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerScreen(
    fileName: String = "Constancia_Matricula_2026.pdf",
    uriString: String = "",
    onBackClick: () -> Unit = {}
) {
    // State for navigation and save simulation
    var currentPage by remember { mutableIntStateOf(1) }
    var totalPages by remember { mutableIntStateOf(1) }
    var isSaving by remember { mutableStateOf(false) }
    var pdfBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Export Modal State
    var showExportModal by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var exportFileName by remember { mutableStateOf(fileName.substringBeforeLast(".") + "_editado") }
    var selectedExportOption by remember { mutableStateOf(ExportOption.DEVICE) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // State for fields (Hardcoded data for Phase 1.3/1.4)
    val fieldData = remember {
        listOf(
            EditablePdfField(1, 25, 80, 180, 22, "MINISTERIO DE EDUCACIÓN", mutableStateOf("MINISTERIO DE EDUCACIÓN")),
            EditablePdfField(2, 25, 108, 140, 20, "CONSTANCIA DE MATRÍCULA", mutableStateOf("CONSTANCIA DE MATRÍCULA")),
            EditablePdfField(3, 25, 134, 120, 18, "Estudiante: Jhosue Acosta", mutableStateOf("Estudiante: Jhosue Acosta")),
            EditablePdfField(4, 175, 134, 60, 18, "Cód: 2026", mutableStateOf("Cód: 2026"))
        )
    }

    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val appContext = LocalContext.current

    LaunchedEffect(uriString) {
        withContext(Dispatchers.IO) {
            try {
                val filePath = java.net.URLDecoder.decode(uriString, "UTF-8")
                val file = java.io.File(filePath)

                if (!file.exists()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(appContext, "Error: archivo temporal no encontrado", Toast.LENGTH_LONG).show()
                    }
                    return@withContext
                }

                val parcelFileDescriptor = android.os.ParcelFileDescriptor.open(
                    file,
                    android.os.ParcelFileDescriptor.MODE_READ_ONLY
                )

                val pdfRenderer = android.graphics.pdf.PdfRenderer(parcelFileDescriptor)
                totalPages = pdfRenderer.pageCount

                val page = pdfRenderer.openPage(0)
                val width = (page.width * 2).coerceAtMost(2048)
                val height = (page.height * 2).coerceAtMost(4096)
                val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(android.graphics.Color.WHITE)
                page.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                pdfRenderer.close()
                parcelFileDescriptor.close()

                withContext(Dispatchers.Main) {
                    pdfBitmap = bitmap
                    isLoading = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(appContext, "Error: ${e.javaClass.simpleName}: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
        containerColor = Color(0xFF111113),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // TOP TOOLBAR
            TopToolbar(fileName, currentPage, totalPages, onBackClick)
            
            // PDF AREA
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A1E))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { 
                        focusManager.clearFocus()
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    PdfSheet(fields = fieldData, isLoading = isLoading, bitmap = pdfBitmap)
                }
            }
            
            // BOTTOM TOOLBAR
            BottomToolbar(
                currentPage = currentPage,
                totalPages = totalPages,
                isSaving = isSaving,
                onPreviousPage = { if (currentPage > 1) currentPage-- },
                onNextPage = { if (currentPage < totalPages) currentPage++ },
                onSaveClick = {
                    showExportModal = true
                }
            )
        }

        // EXPORT MODAL
        if (showExportModal) {
            ModalBottomSheet(
                onDismissRequest = { showExportModal = false },
                sheetState = sheetState,
                containerColor = Color(0xFF17171B),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .size(32.dp, 4.dp)
                            .background(Color(0xFF323238), RoundedCornerShape(2.dp))
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Guardar PDF editado",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // File Name Input
                    OutlinedTextField(
                        value = exportFileName,
                        onValueChange = { exportFileName = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                        label = { Text("Nombre del archivo", color = Color(0xFF8B8B94)) },
                        trailingIcon = { Text(".pdf", color = Color(0xFF8B8B94), modifier = Modifier.padding(end = 12.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6366F1),
                            unfocusedBorderColor = Color(0xFF2A2A32),
                            cursorColor = Color(0xFF6366F1)
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Text(
                        text = "Se guardará en la carpeta Descargas del dispositivo",
                        fontSize = 11.sp,
                        color = Color(0xFF666670),
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 8.dp, bottom = 24.dp)
                    )

                    // Export Options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ExportOptionCard(
                            title = "Guardar en el dispositivo",
                            icon = Icons.Default.Download,
                            isSelected = selectedExportOption == ExportOption.DEVICE,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedExportOption = ExportOption.DEVICE }
                        )
                        ExportOptionCard(
                            title = "Compartir PDF",
                            icon = Icons.Default.Share,
                            isSelected = selectedExportOption == ExportOption.SHARE,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedExportOption = ExportOption.SHARE }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Buttons
                    Button(
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                            }.invokeOnCompletion {
                                showExportModal = false
                                scope.launch {
                                    isSaving = true
                                    delay(2000) // Simulation delay (2s as per 1.5 spec)
                                    isSaving = false
                                    snackbarHostState.showSnackbar("PDF guardado correctamente")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirmar", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }

                    TextButton(
                        onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                showExportModal = false
                            }
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Cancelar", color = Color(0xFF8B8B94), fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ExportOptionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFF1E1E24) else Color(0xFF17171B))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) Color(0xFF6366F1) else Color(0xFF2A2A32),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF6366F1) else Color(0xFF8B8B94),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                color = if (isSelected) Color.White else Color(0xFF8B8B94),
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color(0xFF6366F1),
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.TopEnd)
            )
        }
    }
}

@Composable
fun TopToolbar(fileName: String, currentPage: Int, totalPages: Int, onBackClick: () -> Unit) {
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
            text = "$currentPage / $totalPages",
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
fun PdfSheet(fields: List<EditablePdfField>, isLoading: Boolean, bitmap: android.graphics.Bitmap?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(Color.White, RoundedCornerShape(4.dp))
            .border(1.dp, Color(0xFF2A2A32), RoundedCornerShape(4.dp))
            .defaultMinSize(minHeight = 700.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(700.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = Color(0xFF6366F1)
                )
            } else if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Página del PDF",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth
                )
            }
        }
        
        // OVERLAYS - EDITABLE FIELDS
        Box(modifier = Modifier.matchParentSize()) {
            fields.forEach { field ->
                EditableFieldOverlay(field)
            }
        }
    }
}

@Composable
fun EditableFieldOverlay(field: EditablePdfField) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .absoluteOffset(x = field.x.dp, y = field.y.dp)
            .size(width = field.width.dp, height = field.height.dp)
            .background(
                color = if (isFocused) Color(0xFF6366F1).copy(alpha = 0.15f) 
                        else Color(0xFF6366F1).copy(alpha = 0.05f),
                shape = RoundedCornerShape(2.dp)
            )
            .border(
                width = if (isFocused) 1.5.dp else 0.8.dp,
                color = if (isFocused) Color(0xFF6366F1) else Color(0xFF6366F1).copy(alpha = 0.3f),
                shape = RoundedCornerShape(2.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusRequester.requestFocus()
                keyboardController?.show()
            },
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = field.currentText.value,
            onValueChange = { field.currentText.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .focusRequester(focusRequester)
                .onFocusChanged { isFocused = it.isFocused },
            textStyle = TextStyle(
                fontSize = (field.height * 0.6).sp, // Scaled font size
                color = if (isFocused) Color(0xFF4338CA) else Color.Black,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif
            ),
            cursorBrush = SolidColor(Color(0xFF6366F1)),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            ),
            singleLine = true
        )
    }
}

@Composable
fun BottomToolbar(
    currentPage: Int,
    totalPages: Int,
    isSaving: Boolean,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onSaveClick: () -> Unit
) {
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
            color = Color(0xFF555560),
            isEnabled = currentPage > 1,
            onClick = onPreviousPage
        )
        
        Button(
            onClick = { if (!isSaving) onSaveClick() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.height(36.dp),
            enabled = !isSaving
        ) {
            if (isSaving) {
                SizedBoxHorizontal(8.dp)
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                SizedBoxHorizontal(8.dp)
                Text("Guardando...", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            } else {
                Text(
                    text = "Guardar PDF",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
        
        BottomToolbarAction(
            icon = Icons.AutoMirrored.Filled.ArrowForward,
            label = "Siguiente",
            color = Color(0xFF6366F1),
            isEnabled = currentPage < totalPages,
            onClick = onNextPage
        )
    }
}

@Composable
fun SizedBoxHorizontal(width: androidx.compose.ui.unit.Dp) {
    Spacer(modifier = Modifier.width(width))
}

@Composable
fun BottomToolbarAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .alpha(if (isEnabled) 1f else 0.3f)
            .clickableNoRipple(onClick = { if (isEnabled) onClick() })
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isEnabled) color else Color.Gray,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            fontSize = 8.sp,
            color = if (isEnabled) color else Color.Gray
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
