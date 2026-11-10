package com.example.samachaarsathi.ui.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.samachaarsathi.ui.theme.ElectricCyan
import com.example.samachaarsathi.ui.theme.NeonCrimson
import com.example.samachaarsathi.ui.theme.NeonEmerald
import com.example.samachaarsathi.ui.theme.SamachaarMotion

enum class StepExecutionState {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
}

data class VerificationPipelineStep(
    val id: String,
    val title: String,
    val state: StepExecutionState,
    val detail: String? = null,
)

/**
 * Honest, state-driven cyber telemetry progress indicator for the verification pipeline.
 * Displays only actual application states without fake animations or simulated ticks,
 * with futuristic glowing node transitions.
 */
@Composable
fun StepProgressIndicator(
    steps: List<VerificationPipelineStep>,
    modifier: Modifier = Modifier,
) {
    FuturisticCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(SamachaarMotion.mediumTween()),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(ElectricCyan)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Verification Progress",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(modifier = Modifier.height(14.dp))

            steps.forEachIndexed { index, step ->
                StepRow(
                    index = index + 1,
                    step = step,
                    isLast = index == steps.lastIndex,
                )
            }
        }
    }
}

@Composable
private fun StepRow(
    index: Int,
    step: VerificationPipelineStep,
    isLast: Boolean,
) {
    val lineColor by animateColorAsState(
        targetValue = when (step.state) {
            StepExecutionState.COMPLETED -> NeonEmerald
            StepExecutionState.IN_PROGRESS -> ElectricCyan.copy(alpha = 0.6f)
            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        },
        animationSpec = SamachaarMotion.mediumTween(),
        label = "StepLineColor",
    )

    val titleColor by animateColorAsState(
        targetValue = when (step.state) {
            StepExecutionState.IN_PROGRESS -> MaterialTheme.colorScheme.primary
            StepExecutionState.COMPLETED -> MaterialTheme.colorScheme.onSurface
            StepExecutionState.FAILED -> MaterialTheme.colorScheme.error
            StepExecutionState.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        },
        animationSpec = SamachaarMotion.fastTween(),
        label = "StepTitleColor",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Step $index: ${step.title}, status: ${step.state}"
            },
        verticalAlignment = Alignment.Top,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            StepIcon(state = step.state, index = index)
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(26.dp)
                        .background(lineColor),
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 14.dp)) {
            Text(
                text = step.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (step.state == StepExecutionState.IN_PROGRESS) FontWeight.Bold else FontWeight.Medium,
                color = titleColor,
            )
            if (step.detail != null && step.state != StepExecutionState.PENDING) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = step.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (step.state == StepExecutionState.FAILED) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

@Composable
private fun StepIcon(
    state: StepExecutionState,
    index: Int,
) {
    Box(
        modifier = Modifier.size(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Crossfade(
            targetState = state,
            animationSpec = SamachaarMotion.fastTween(),
            label = "StepIconCrossfade",
        ) { targetState ->
            when (targetState) {
                StepExecutionState.PENDING -> {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "$index",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                StepExecutionState.IN_PROGRESS -> {
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan.copy(alpha = 0.15f))
                        )
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                StepExecutionState.COMPLETED -> {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(NeonEmerald)
                            .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.Black,
                            modifier = Modifier.size(13.dp),
                        )
                    }
                }
                StepExecutionState.FAILED -> {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(NeonCrimson),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Failed",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp),
                        )
                    }
                }
            }
        }
    }
}
