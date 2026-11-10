package com.example.samachaarsathi.ui.library

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.samachaarsathi.data.model.VerdictCategory
import com.example.samachaarsathi.ui.claim.SessionVerificationItem
import com.example.samachaarsathi.ui.component.EmptyStateCard
import com.example.samachaarsathi.ui.component.FuturisticCard
import com.example.samachaarsathi.ui.component.VerdictBadge
import com.example.samachaarsathi.ui.share.ShareHelper
import com.example.samachaarsathi.ui.theme.CyberCardBorder
import com.example.samachaarsathi.ui.theme.CyberDarkSurface
import com.example.samachaarsathi.ui.theme.ElectricCyan
import com.example.samachaarsathi.ui.theme.NeonCrimson
import com.example.samachaarsathi.ui.theme.SamachaarMotion
import com.example.samachaarsathi.ui.theme.VerdictColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Filter categories for Library items.
 */
enum class LibraryVerdictFilter(val label: String, val category: VerdictCategory?) {
    ALL("All", null),
    TRUE("Verified True", VerdictCategory.SUPPORTED),
    FALSE("Verified False", VerdictCategory.CONTRADICTED),
    MISLEADING("Misleading", VerdictCategory.MISLEADING_OR_MISSING_CONTEXT),
    UNVERIFIED("Unverified", VerdictCategory.UNVERIFIED),
}

/**
 * Dedicated Library destination presenting active session verification history,
 * bookmarked / saved checks, multi-category filtering, keyword search,
 * quick sharing, and transparent session-storage disclosures.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LibraryScreen(
    sessionHistory: List<SessionVerificationItem>,
    onSelectHistoryItem: (SessionVerificationItem) -> Unit,
    onToggleSave: (String) -> Unit,
    onNavigateToCheck: () -> Unit,
    modifier: Modifier = Modifier,
    onRemoveSaved: (String) -> Unit = onToggleSave,
    initialTab: Int = 0,
    initialSearchQuery: String = "",
    initialFilter: LibraryVerdictFilter = LibraryVerdictFilter.ALL,
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    var searchQuery by remember { mutableStateOf(initialSearchQuery) }
    var activeFilter by remember { mutableStateOf(initialFilter) }
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    val savedItems = remember(sessionHistory) {
        sessionHistory.filter { it.isSaved }
    }

    val haptic = LocalHapticFeedback.current
    val isReducedMotion = SamachaarMotion.isReducedMotionEnabled()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        // 1. Header & Title with cyber accents
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Library",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Verification history & saved checks",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDark) ElectricCyan.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Tabs: History vs Saved with cyber pill container
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isDark) CyberDarkSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            border = BorderStroke(1.dp, if (isDark) CyberCardBorder.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = if (isDark) ElectricCyan else MaterialTheme.colorScheme.primary,
                indicator = {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(selectedTab),
                        color = if (isDark) ElectricCyan else MaterialTheme.colorScheme.primary,
                        height = 3.dp,
                    )
                },
                divider = {},
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedTab = 0
                    },
                    text = {
                        Text(
                            text = "History (${sessionHistory.size})",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 0) {
                                if (isDark) ElectricCyan else MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedTab = 1
                    },
                    text = {
                        Text(
                            text = "Saved (${savedItems.size})",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 1) {
                                if (isDark) ElectricCyan else MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Cyber Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Search checks by claim or finding...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = ElectricCyan,
                )
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        searchQuery = ""
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = ElectricCyan,
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElectricCyan,
                unfocusedBorderColor = if (isDark) CyberCardBorder else MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = if (isDark) CyberDarkSurface.copy(alpha = 0.65f) else MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = if (isDark) CyberDarkSurface.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface,
                cursorColor = ElectricCyan,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            ),
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Category Filter Chips with Cyber Neon styling
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LibraryVerdictFilter.entries.forEach { filter ->
                val selected = activeFilter == filter
                FilterChip(
                    selected = selected,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        activeFilter = filter
                    },
                    label = {
                        Text(
                            text = filter.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(
                        1.dp,
                        if (selected) ElectricCyan else if (isDark) CyberCardBorder.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outlineVariant,
                    ),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (isDark) ElectricCyan.copy(alpha = 0.16f) else MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = if (isDark) ElectricCyan else MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = if (isDark) CyberDarkSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 5. Content List or Empty State with spatial tab transition
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                if (isReducedMotion) {
                    fadeIn(animationSpec = SamachaarMotion.fastTween()) togetherWith
                        fadeOut(animationSpec = SamachaarMotion.fastTween())
                } else if (targetState > initialState) {
                    SamachaarMotion.slideInFromRight() togetherWith SamachaarMotion.slideOutToLeft()
                } else {
                    SamachaarMotion.slideInFromLeft() togetherWith SamachaarMotion.slideOutToRight()
                }
            },
            modifier = Modifier.weight(1f),
            label = "LibraryTabTransition",
        ) { currentTab ->
            val itemsForTab = if (currentTab == 0) sessionHistory else savedItems
            val filtered = remember(itemsForTab, searchQuery, activeFilter) {
                itemsForTab.filter { item ->
                    val matchesSearch = if (searchQuery.isBlank()) {
                        true
                    } else {
                        item.claimText.contains(searchQuery, ignoreCase = true) ||
                            (item.verdict?.headline?.contains(searchQuery, ignoreCase = true) == true)
                    }
                    val matchesFilter = when (activeFilter) {
                        LibraryVerdictFilter.ALL -> true
                        else -> item.verdict?.category == activeFilter.category
                    }
                    matchesSearch && matchesFilter
                }
            }

            if (filtered.isEmpty()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    if (searchQuery.isNotBlank() || activeFilter != LibraryVerdictFilter.ALL) {
                        EmptyStateCard(
                            heading = "No Matching Checks",
                            description = "No verifications match your current search and filter criteria.",
                            actionLabel = "Clear Filters",
                            onActionClicked = {
                                searchQuery = ""
                                activeFilter = LibraryVerdictFilter.ALL
                            },
                        )
                    } else if (currentTab == 0) {
                        EmptyStateCard(
                            heading = "No Checks Yet",
                            description = "Claims verified during this session appear here for quick review, filtering, and re-checking.",
                            actionLabel = "Verify a Claim",
                            onActionClicked = onNavigateToCheck,
                        )
                    } else {
                        EmptyStateCard(
                            heading = "No Saved Checks",
                            description = "Tap the heart icon on any check in History or the Assessment screen to bookmark important verifications.",
                            actionLabel = "Check a Claim",
                            onActionClicked = onNavigateToCheck,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(filtered, key = { it.id }) { item ->
                        LibraryItemCard(
                            item = item,
                            isSavedTab = currentTab == 1,
                            onClick = { onSelectHistoryItem(item) },
                            onToggleSave = {
                                if (currentTab == 1) {
                                    onRemoveSaved(item.id)
                                } else {
                                    onToggleSave(item.id)
                                }
                            },
                            onShare = {
                                ShareHelper.shareText(
                                    context = context,
                                    claim = item.claimText,
                                    verdict = item.verdict,
                                    timestampMs = item.timestampMs,
                                )
                            },
                        )
                    }
                }
            }
        }

        // 6. Transparent Session-Storage Notice with Cyber Chip
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = if (isDark) CyberDarkSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            border = BorderStroke(1.dp, if (isDark) ElectricCyan.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Local Device Storage: Verification history and bookmarked claims are stored permanently on this device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LibraryItemCard(
    item: SessionVerificationItem,
    isSavedTab: Boolean,
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme(),
) {
    val dateText = remember(item.timestampMs) {
        SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.ENGLISH).format(Date(item.timestampMs))
    }
    val haptic = LocalHapticFeedback.current

    val bookmarkScale by animateFloatAsState(
        targetValue = if (item.isSaved) 1.18f else 1.0f,
        animationSpec = SamachaarMotion.bouncySpring(),
        label = "BookmarkScale",
    )

    val bookmarkTint by animateColorAsState(
        targetValue = if (item.isSaved) NeonCrimson else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = SamachaarMotion.fastTween(),
        label = "BookmarkTint",
    )

    FuturisticCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = "Check: ${item.claimText}"
            },
        shape = RoundedCornerShape(18.dp),
        isDark = isDark,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Verdict Badge (or status) + Timestamp + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (item.verdict != null) {
                        VerdictBadge(category = item.verdict.category)
                    } else {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Text(
                                text = "Pending Verdict",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                        }
                    }

                    if (isSavedTab) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ElectricCyan.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.4f)),
                        ) {
                            Text(
                                text = "Saved Check",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                        }
                    }
                }

                // Action buttons: Share & Save/Bookmark with full 48dp touch targets and micro-interactions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onShare()
                        },
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share check",
                            tint = ElectricCyan.copy(alpha = 0.85f),
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleSave()
                        },
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = if (item.isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (item.isSaved) "Remove from saved" else "Save check",
                            tint = bookmarkTint,
                            modifier = Modifier
                                .size(20.dp)
                                .scale(bookmarkScale),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Claim Text
            Text(
                text = item.claimText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 22.sp,
            )

            // Finding headline if present
            item.verdict?.headline?.let { headline ->
                Spacer(modifier = Modifier.height(6.dp))
                val headlineColor = if (item.verdict != null) {
                    VerdictColors.forCategory(item.verdict.category, isDark).main
                } else {
                    ElectricCyan
                }
                Text(
                    text = headline,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = headlineColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 19.sp,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Timestamp & Review Affordance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    text = "Tap to review report →",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = ElectricCyan,
                )
            }
        }
    }
}
