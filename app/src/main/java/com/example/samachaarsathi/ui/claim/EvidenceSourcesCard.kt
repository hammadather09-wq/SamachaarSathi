package com.example.samachaarsathi.ui.claim

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.samachaarsathi.data.model.EvidenceCollection
import com.example.samachaarsathi.data.model.EvidenceSource
import com.example.samachaarsathi.data.model.SourceType

@Composable
fun EvidenceSourcesCard(
    evidenceStatus: EvidenceStatus,
    onRetrieveEvidenceClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (evidenceStatus) {
        is EvidenceStatus.NotRetrieved -> {
            NotRetrievedCard(
                onRetrieveClicked = onRetrieveEvidenceClicked,
                modifier = modifier,
            )
        }
        is EvidenceStatus.Loading -> {
            LoadingEvidenceCard(modifier = modifier)
        }
        is EvidenceStatus.Error -> {
            ErrorEvidenceCard(
                message = evidenceStatus.message,
                onRetryClicked = onRetrieveEvidenceClicked,
                modifier = modifier,
            )
        }
        is EvidenceStatus.Success -> {
            EvidenceSuccessCard(
                collection = evidenceStatus.evidence,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun NotRetrievedCard(
    onRetrieveClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Evidence Sources",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                AssistChip(
                    onClick = {},
                    label = { Text("Not Yet Retrieved") },
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Web search grounding is configured for each identified assertion. Tap below to retrieve relevant sources using Google Search.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onRetrieveClicked,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Search Web Evidence")
            }
        }
    }
}

@Composable
private fun LoadingEvidenceCard(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Searching Web Evidence...",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Querying Google Search Grounding for identified factual assertions...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ErrorEvidenceCard(
    message: String,
    onRetryClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Evidence Retrieval Error",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onRetryClicked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            ) {
                Text("Retry Evidence Retrieval")
            }
        }
    }
}

@Composable
private fun EvidenceSuccessCard(
    collection: EvidenceCollection,
    modifier: Modifier = Modifier,
) {
    val sources = collection.allSources

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Evidence Sources",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                AssistChip(
                    onClick = {},
                    label = { Text("${sources.size} Sources") },
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Attribution & Transparency Notice
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Text(
                    text = "Notice: Retrieved sources represent external web reporting discovered via Google Search Grounding. Finding a source does not confirm or verify whether the claim is true or false.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(10.dp),
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (sources.isEmpty()) {
                Text(
                    text = "No web sources were found for these assertions.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                sources.forEachIndexed { index, source ->
                    if (index > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    EvidenceSourceItem(source = source)
                }
            }
        }
    }
}

@Composable
private fun EvidenceSourceItem(
    source: EvidenceSource,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Source Title
            Text(
                text = source.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Chips: Domain & Source Type
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                source.domain?.let { domain ->
                    AssistChip(
                        onClick = {},
                        label = { Text(domain) },
                    )
                }

                val typeLabel = when (source.sourceType) {
                    SourceType.OFFICIAL_PORTAL -> "Official Portal"
                    SourceType.FACT_CHECK_ORGANIZATION -> "Fact-Check Org"
                    SourceType.NEWS_MEDIA -> "News Media"
                    SourceType.WEB_SEARCH -> "Web Source"
                    SourceType.OTHER -> "Other"
                }
                AssistChip(
                    onClick = {},
                    label = { Text(typeLabel) },
                )
            }

            // Snippet text if available
            if (!source.snippet.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "\"${source.snippet}\"",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Clickable Open Webpage Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                OutlinedButton(
                    onClick = {
                        try {
                            uriHandler.openUri(source.url)
                        } catch (_: Exception) {
                            Toast.makeText(context, "Could not open URL: ${source.url}", Toast.LENGTH_SHORT).show()
                        }
                    },
                ) {
                    Text(
                        text = "Open Webpage",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}
