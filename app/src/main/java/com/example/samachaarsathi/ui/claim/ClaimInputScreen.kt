package com.example.samachaarsathi.ui.claim

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.samachaarsathi.R
import com.example.samachaarsathi.data.image.OcrExtractionResult
import com.example.samachaarsathi.ui.component.AiTruthHaloCenterpiece
import com.example.samachaarsathi.ui.component.CyberGradientButton
import com.example.samachaarsathi.ui.component.SecondaryButton
import com.example.samachaarsathi.ui.component.TruthHaloState
import com.example.samachaarsathi.ui.theme.ElectricCyan
import com.example.samachaarsathi.ui.theme.NeonCobalt
import com.example.samachaarsathi.ui.theme.SamachaarMotion
import kotlinx.coroutines.delay

@Composable
fun ClaimInputRoute(
    modifier: Modifier = Modifier,
    viewModel: ClaimInputViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    ClaimInputScreen(
        uiState = uiState,
        onClaimTextChanged = viewModel::onClaimTextChanged,
        onVerifyClaimClicked = viewModel::onVerifyClaimClicked,
        onRetrieveEvidenceClicked = viewModel::onRetrieveEvidenceClicked,
        onImageSelected = { uri -> viewModel.onImageSelected(context, uri) },
        onImageRemoved = viewModel::onImageRemoved,
        onUseExtractedText = viewModel::onUseExtractedText,
        modifier = modifier,
    )
}

/**
 * Signature Cyber Check Home screen adhering to the Cyber Intelligence design system.
 * Features the centered luminous Truth Halo orb, bold dual-color mission statement,
 * unified Cyber Claim Input Console, and distinctive gradient verify button.
 */
@Composable
fun ClaimInputScreen(
    uiState: ClaimInputUiState,
    onClaimTextChanged: (String) -> Unit,
    onVerifyClaimClicked: () -> Unit,
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") onRetrieveEvidenceClicked: () -> Unit = {},
    onImageSelected: (Uri) -> Unit = {},
    onImageRemoved: () -> Unit = {},
    onUseExtractedText: (String) -> Unit = {},
    onViewResultsClicked: () -> Unit = {},
    initialInputFocused: Boolean = false,
) {
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val clipboardManager = LocalClipboardManager.current
    val hapticFeedback = LocalHapticFeedback.current
    val focusRequester = remember { FocusRequester() }
    var isInputFocused by remember { mutableStateOf(initialInputFocused) }
    var actionFeedback by remember { mutableStateOf<String?>(null) }
    val isDark = isSystemInDarkTheme()

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri != null) {
            onImageSelected(uri)
            actionFeedback = "Image attached · Extracting text..."
        }
    }

    LaunchedEffect(actionFeedback) {
        if (actionFeedback != null) {
            delay(2200)
            actionFeedback = null
        }
    }

    LaunchedEffect(initialInputFocused) {
        if (initialInputFocused) {
            focusRequester.requestFocus()
        }
    }

    // Auto-scroll focused console into view when virtual keyboard emerges
    LaunchedEffect(isInputFocused) {
        if (isInputFocused) {
            delay(150)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    // Dynamic Truth Halo state honestly reflecting current interaction
    val haloState = when {
        uiState.isLoading -> TruthHaloState.VERIFYING
        isInputFocused || uiState.claimText.isNotBlank() || uiState.selectedImageUri != null -> TruthHaloState.INPUT_ACTIVE
        else -> TruthHaloState.IDLE
    }

    val telemetryText = when (haloState) {
        TruthHaloState.IDLE -> "TRUTH ENGINE • STANDBY"
        TruthHaloState.INPUT_ACTIVE -> "CONSOLE • INPUT ACTIVE"
        TruthHaloState.VERIFYING -> "ANALYZING EVIDENCE"
    }

    val indicatorColor = when (haloState) {
        TruthHaloState.IDLE -> ElectricCyan
        TruthHaloState.INPUT_ACTIVE -> ElectricCyan
        TruthHaloState.VERIFYING -> NeonCobalt
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // 1. Status Telemetry Pill
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isDark) Color(0xFF0C1929) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            border = BorderStroke(
                1.dp,
                if (isDark) indicatorColor.copy(alpha = 0.45f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            ),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(indicatorColor),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = telemetryText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = indicatorColor,
                    letterSpacing = 1.2.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Prominent Truth Halo Centerpiece
        AiTruthHaloCenterpiece(
            state = haloState,
            size = 118.dp,
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Hero Typography Section
        Text(
            text = "Question everything.",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Verify with evidence.",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = ElectricCyan,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Understand claims. Examine sources. Discover context.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 4. Unified Cyber Claim Input Console
        CyberClaimInputConsole(
            claimText = uiState.claimText,
            onClaimTextChanged = onClaimTextChanged,
            isFocused = isInputFocused,
            onFocusChanged = { isInputFocused = it },
            focusRequester = focusRequester,
            isLoading = uiState.isLoading,
            selectedImageUri = uiState.selectedImageUri,
            ocrResult = uiState.ocrResult,
            isOcrLoading = uiState.isOcrLoading,
            actionFeedback = actionFeedback,
            onPickImage = { imageLauncher.launch("image/*") },
            onRemoveImage = onImageRemoved,
            onUseExtractedText = onUseExtractedText,
            onPasteClicked = {
                val text = clipboardManager.getText()?.text
                if (!text.isNullOrBlank()) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClaimTextChanged(text)
                    actionFeedback = "Pasted from clipboard"
                } else {
                    actionFeedback = "Clipboard is empty"
                }
            },
            onLinkClicked = {
                val text = clipboardManager.getText()?.text?.trim()
                if (!text.isNullOrBlank() && (text.startsWith("http://", ignoreCase = true) || text.startsWith("https://", ignoreCase = true) || text.startsWith("www.", ignoreCase = true))) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClaimTextChanged(text)
                    actionFeedback = "Link pasted from clipboard"
                } else {
                    if (uiState.claimText.isBlank()) {
                        onClaimTextChanged("https://")
                    }
                    actionFeedback = "Enter article URL to verify"
                    focusRequester.requestFocus()
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            },
            onClearClicked = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClaimTextChanged("")
                actionFeedback = "Input cleared"
            },
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Distinctive Cyber Gradient Verify Button
        CyberGradientButton(
            text = "VERIFY CLAIM",
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                keyboardController?.hide()
                onVerifyClaimClicked()
            },
            enabled = uiState.canSubmit,
            isLoading = uiState.isLoading,
            loadingText = "Analyzing...",
            isError = uiState.analysisStatus is AnalysisStatus.Error,
            errorMessage = (uiState.analysisStatus as? AnalysisStatus.Error)?.message,
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_scan),
                    contentDescription = null,
                    tint = if (uiState.canSubmit) Color.White else Color(0xFF5A7090),
                    modifier = Modifier.size(20.dp),
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = if (uiState.canSubmit) Color.White else Color(0xFF5A7090),
                    modifier = Modifier.size(18.dp),
                )
            },
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 6. Elegant Trust Statement
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_shield_check),
                contentDescription = null,
                tint = ElectricCyan.copy(alpha = 0.85f),
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Evidence-led verification · Transparent results",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.80f),
            )
        }

        // 7. Persistent Assessment Status Banner
        if (uiState.isLoading || (uiState.verdictStatus is VerdictStatus.Success)) {
            Spacer(modifier = Modifier.height(14.dp))
            AssessmentStatusCard(
                isLoading = uiState.isLoading,
                onViewResultsClicked = onViewResultsClicked,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Unified cyber claim console featuring smooth cyan focus illumination,
 * subtle surface elevation glow, attachment preview, and accessible 48dp action controls.
 */
@Composable
private fun CyberClaimInputConsole(
    claimText: String,
    onClaimTextChanged: (String) -> Unit,
    isFocused: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    focusRequester: FocusRequester,
    isLoading: Boolean,
    selectedImageUri: Uri?,
    ocrResult: OcrExtractionResult?,
    isOcrLoading: Boolean,
    actionFeedback: String?,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit,
    onUseExtractedText: (String) -> Unit,
    onPasteClicked: () -> Unit,
    onLinkClicked: () -> Unit,
    onClearClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = isSystemInDarkTheme()

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) ElectricCyan else Color(0xFF1E2D48),
        animationSpec = SamachaarMotion.fastTween(),
        label = "ConsoleBorder",
    )

    val shadowElevation by animateDpAsState(
        targetValue = if (isFocused) 10.dp else 0.dp,
        animationSpec = SamachaarMotion.fastTween(),
        label = "ConsoleGlow",
    )

    val containerColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF0F1B2F) else Color(0xFF0D1627),
        animationSpec = SamachaarMotion.fastTween(),
        label = "ConsoleContainer",
    )

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isDark) containerColor else MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (isFocused) 1.5.dp else 1.dp, borderColor),
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = shadowElevation,
                shape = RoundedCornerShape(18.dp),
                spotColor = ElectricCyan.copy(alpha = 0.45f),
                ambientColor = NeonCobalt.copy(alpha = 0.20f),
            ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Console label + Feedback Pill + Clear action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "CLAIM INPUT CONSOLE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isFocused) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        letterSpacing = 1.4.sp,
                    )

                    AnimatedVisibility(
                        visible = actionFeedback != null,
                        enter = fadeIn(SamachaarMotion.fastTween()) + expandHorizontally(),
                        exit = fadeOut(SamachaarMotion.fastTween()) + shrinkHorizontally(),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF082038),
                            border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.padding(start = 8.dp),
                        ) {
                            Text(
                                text = actionFeedback ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricCyan,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = claimText.isNotBlank() && !isLoading,
                    enter = fadeIn(SamachaarMotion.fastTween()) + expandHorizontally(),
                    exit = fadeOut(SamachaarMotion.fastTween()) + shrinkHorizontally(),
                ) {
                    TextButton(
                        onClick = onClearClicked,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "Clear",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            // Image Attachment / OCR Extraction Preview (animated)
            AnimatedVisibility(
                visible = selectedImageUri != null || isOcrLoading || ocrResult != null,
                enter = fadeIn(SamachaarMotion.fastTween()) + expandVertically(),
                exit = fadeOut(SamachaarMotion.fastTween()) + shrinkVertically(),
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    ConsoleImageAttachmentCard(
                        selectedImageUri = selectedImageUri,
                        ocrResult = ocrResult,
                        isOcrLoading = isOcrLoading,
                        onReplace = onPickImage,
                        onRemove = onRemoveImage,
                        onUseExtractedText = onUseExtractedText,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Input Text Area with custom cyber cursor and selection colors
            OutlinedTextField(
                value = claimText,
                onValueChange = onClaimTextChanged,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 96.dp, max = 220.dp)
                    .focusRequester(focusRequester)
                    .onFocusChanged { onFocusChanged(it.isFocused) },
                placeholder = {
                    Text(
                        text = "Paste a message, article link or claim to investigate...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    cursorColor = ElectricCyan,
                    selectionColors = TextSelectionColors(
                        handleColor = ElectricCyan,
                        backgroundColor = ElectricCyan.copy(alpha = 0.35f),
                    ),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.50f),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
                minLines = 3,
                maxLines = 8,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Default,
                ),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Action Tray with accessible 48dp touch targets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Paste action icon (48x48dp touch target)
                    IconButton(
                        onClick = onPasteClicked,
                        enabled = !isLoading,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_paste),
                            contentDescription = "Paste from clipboard",
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    // Link action icon (48x48dp touch target)
                    IconButton(
                        onClick = onLinkClicked,
                        enabled = !isLoading,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_link),
                            contentDescription = "Insert article link",
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    // Image / OCR action icon (48x48dp touch target)
                    IconButton(
                        onClick = onPickImage,
                        enabled = !isLoading,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_image),
                            contentDescription = "Attach image or screenshot for OCR",
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                // Right status telemetry label
                Text(
                    text = if (claimText.isNotBlank()) "Input ready" else "Input supported",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                )
            }
        }
    }
}

/**
 * Sleek inline attachment card for image claims and on-device OCR results.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConsoleImageAttachmentCard(
    selectedImageUri: Uri?,
    ocrResult: OcrExtractionResult?,
    isOcrLoading: Boolean,
    onReplace: () -> Unit,
    onRemove: () -> Unit,
    onUseExtractedText: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF111C30),
        border = BorderStroke(1.dp, Color(0xFF223854)),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_image),
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOcrLoading) "Analyzing Image Text..." else (selectedImageUri?.lastPathSegment ?: "Image Attached"),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan,
                        maxLines = 1,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = onReplace,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text("Replace", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                    }
                    TextButton(
                        onClick = onRemove,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text("Remove", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            if (isOcrLoading) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = ElectricCyan,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "On-device OCR extracting claims...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else if (ocrResult != null) {
                Spacer(modifier = Modifier.height(6.dp))
                ocrResult.forwardingMetadata?.let { meta ->
                    if (meta.isForwarded) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(bottom = 6.dp),
                        ) {
                            AssistChip(
                                onClick = {},
                                label = { Text("Forwarded Message", style = MaterialTheme.typography.labelSmall) },
                            )
                        }
                    }
                }

                if (ocrResult.hasReadableText) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF091220),
                        border = BorderStroke(1.dp, Color(0xFF1B2C42)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = ocrResult.cleanedClaimText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 4,
                            modifier = Modifier.padding(8.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    SecondaryButton(
                        text = "Use Extracted Text",
                        onClick = { onUseExtractedText(ocrResult.cleanedClaimText) },
                        modifier = Modifier.height(38.dp),
                    )
                } else {
                    Text(
                        text = "No readable text detected in this image.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

/**
 * Status card displayed if an assessment is running in the background or completed.
 */
@Composable
private fun AssessmentStatusCard(
    isLoading: Boolean,
    onViewResultsClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F1A2E),
        border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onViewResultsClicked),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isLoading) "Verification In Progress" else "Assessment Report Ready",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isLoading) "Tap to monitor real-time pipeline telemetry" else "Tap to view synthesized verdict and grounded evidence",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "View Assessment",
                tint = ElectricCyan,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
