package com.example.samachaarsathi.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.samachaarsathi.ui.theme.CyberDarkSurface
import com.example.samachaarsathi.ui.theme.ElectricCyan
import com.example.samachaarsathi.ui.theme.FuturisticGradients

enum class AppDestination(
    val title: String,
    val icon: ImageVector,
) {
    CHECK("Check", Icons.Default.Search),
    LIBRARY("Library", Icons.AutoMirrored.Filled.List),
}

/**
 * Reusable two-tab floating navigation dock for SamachaarSathi.
 * Features a floating glassmorphic pill container with subtle neon laser border
 * and tactile feedback, matching the futuristic visual reference.
 */
@Composable
fun SamachaarBottomNavigationBar(
    currentDestination: AppDestination,
    onNavigateToDestination: (AppDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = if (isDark) {
                CyberDarkSurface.copy(alpha = 0.94f)
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            },
            tonalElevation = 8.dp,
            shadowElevation = 12.dp,
            border = BorderStroke(
                1.dp,
                if (isDark) {
                    FuturisticGradients.cyberCardBorder
                } else {
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        )
                    )
                },
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            NavigationBar(
                containerColor = Color.Transparent,
                tonalElevation = 0.dp,
                modifier = Modifier.height(64.dp),
            ) {
                AppDestination.entries.forEach { destination ->
                    val selected = destination == currentDestination
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNavigateToDestination(destination)
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.title,
                            )
                        },
                        label = {
                            Text(
                                text = destination.title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (isDark) ElectricCyan else MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = if (isDark) ElectricCyan else MaterialTheme.colorScheme.primary,
                            indicatorColor = if (isDark) ElectricCyan.copy(alpha = 0.18f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        ),
                    )
                }
            }
        }
    }
}
