package com.example.samachaarsathi.ui.claim

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.samachaarsathi.data.image.OcrExtractionResult
import com.example.samachaarsathi.ui.component.FuturisticCard

/**
 * Image attachment, on-device OCR, and forwarded screenshot review card.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImageClaimPickerCard(
    selectedImageUri: Uri?,
    ocrResult: OcrExtractionResult?,
    isOcrLoading: Boolean,
    onImageSelected: (Uri) -> Unit,
    onImageRemoved: () -> Unit,
    onUseExtractedText: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri != null) {
            onImageSelected(uri)
        }
    }

    FuturisticCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Verify from Image / Screenshot",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (selectedImageUri != null) {
                    TextButton(onClick = onImageRemoved) {
                        Text("Remove", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Select a WhatsApp forward or news screenshot to extract text locally using on-device OCR.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (selectedImageUri == null) {
                OutlinedButton(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Select Image / Screenshot")
                }
            } else {
                if (isOcrLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Extracting text on-device...",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                } else if (ocrResult != null) {
                    var editableText by remember(ocrResult) {
                        mutableStateOf(ocrResult.cleanedClaimText)
                    }

                    // Forwarding Metadata Badges
                    ocrResult.forwardingMetadata?.let { meta ->
                        if (meta.isForwarded) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(bottom = 6.dp),
                            ) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text("Forwarded Message") },
                                )
                                meta.timestampDetected?.let { ts ->
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("Time: $ts") },
                                    )
                                }
                            }
                        }
                    }

                    if (!ocrResult.hasReadableText) {
                        Text(
                            text = "No readable text detected in this image. You can still type your claim manually above.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    } else {
                        OutlinedTextField(
                            value = editableText,
                            onValueChange = { editableText = it },
                            label = { Text("Extracted Claim (Review & Edit)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            maxLines = 5,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { onUseExtractedText(editableText) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Use as Claim Input")
                        }
                    }
                }
            }
        }
    }
}
