package com.example.samachaarsathi.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.samachaarsathi.ui.claim.ClaimInputScreen
import com.example.samachaarsathi.ui.claim.ClaimInputViewModel
import com.example.samachaarsathi.ui.claim.VerificationResultScreen
import com.example.samachaarsathi.ui.component.AppDestination
import com.example.samachaarsathi.ui.component.SamachaarBottomNavigationBar
import com.example.samachaarsathi.ui.library.LibraryScreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import com.example.samachaarsathi.ui.theme.SamachaarMotion

/**
 * Sub-destinations within the primary Check experience.
 */
enum class CheckSubDestination {
    INPUT,
    RESULTS,
}

/**
 * Main application scaffold with two-tab bottom navigation (Check & Library),
 * dedicated verification assessment results, and seamless state preservation
 * across destination switches.
 */
@Composable
fun MainAppScreen(
    viewModel: ClaimInputViewModel = viewModel(),
    initialDestination: AppDestination = AppDestination.CHECK,
    initialSubDestination: CheckSubDestination = CheckSubDestination.INPUT,
    initialLibraryTab: Int = 0,
    initialScrollToSources: Boolean = false,
    initialScrollToShare: Boolean = false,
    initialShowSourceDetails: Boolean = false,
    initialInputFocused: Boolean = false,
    initialSearchQuery: String = "",
    initialLibraryFilter: com.example.samachaarsathi.ui.library.LibraryVerdictFilter = com.example.samachaarsathi.ui.library.LibraryVerdictFilter.ALL,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentDestination by rememberSaveable { mutableStateOf(initialDestination) }
    var checkSubDestination by rememberSaveable { mutableStateOf(initialSubDestination) }
    val context = LocalContext.current
    val isReducedMotion = SamachaarMotion.isReducedMotionEnabled()

    // Android back navigation:
    // 1. If currently in RESULTS on the Check tab, Back returns to INPUT
    BackHandler(enabled = currentDestination == AppDestination.CHECK && checkSubDestination == CheckSubDestination.RESULTS) {
        checkSubDestination = CheckSubDestination.INPUT
    }

    // 2. If on the Library tab, Back returns to the Check tab
    BackHandler(enabled = currentDestination != AppDestination.CHECK) {
        currentDestination = AppDestination.CHECK
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            SamachaarBottomNavigationBar(
                currentDestination = currentDestination,
                onNavigateToDestination = { dest ->
                    currentDestination = dest
                },
            )
        },
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentDestination,
            transitionSpec = {
                if (isReducedMotion) {
                    fadeIn(animationSpec = SamachaarMotion.fastTween()) togetherWith
                        fadeOut(animationSpec = SamachaarMotion.fastTween())
                } else if (targetState.ordinal > initialState.ordinal) {
                    SamachaarMotion.slideInFromRight() togetherWith SamachaarMotion.slideOutToLeft()
                } else {
                    SamachaarMotion.slideInFromLeft() togetherWith SamachaarMotion.slideOutToRight()
                }
            },
            label = "MainDestinationTransition",
        ) { destination ->
            when (destination) {
                AppDestination.CHECK -> {
                    AnimatedContent(
                        targetState = checkSubDestination,
                        transitionSpec = {
                            if (isReducedMotion) {
                                fadeIn(animationSpec = SamachaarMotion.fastTween()) togetherWith
                                    fadeOut(animationSpec = SamachaarMotion.fastTween())
                            } else if (targetState == CheckSubDestination.RESULTS) {
                                SamachaarMotion.slideInFromRight() togetherWith SamachaarMotion.slideOutToLeft()
                            } else {
                                SamachaarMotion.slideInFromLeft() togetherWith SamachaarMotion.slideOutToRight()
                            }
                        },
                        label = "CheckSubDestinationTransition",
                    ) { subDestination ->
                        when (subDestination) {
                            CheckSubDestination.INPUT -> {
                                ClaimInputScreen(
                                    uiState = uiState,
                                    onClaimTextChanged = viewModel::onClaimTextChanged,
                                    onVerifyClaimClicked = {
                                        viewModel.onVerifyClaimClicked()
                                        checkSubDestination = CheckSubDestination.RESULTS
                                    },
                                    onRetrieveEvidenceClicked = viewModel::onRetrieveEvidenceClicked,
                                    onImageSelected = { uri -> viewModel.onImageSelected(context, uri) },
                                    onImageRemoved = viewModel::onImageRemoved,
                                    onUseExtractedText = viewModel::onUseExtractedText,
                                    onViewResultsClicked = {
                                        checkSubDestination = CheckSubDestination.RESULTS
                                    },
                                    initialInputFocused = initialInputFocused,
                                    modifier = Modifier.padding(innerPadding),
                                )
                            }
                            CheckSubDestination.RESULTS -> {
                                VerificationResultScreen(
                                    uiState = uiState,
                                    onNavigateBack = {
                                        checkSubDestination = CheckSubDestination.INPUT
                                    },
                                    onRetrieveEvidenceClicked = viewModel::onRetrieveEvidenceClicked,
                                    initialScrollToSources = initialScrollToSources,
                                    initialScrollToShare = initialScrollToShare,
                                    initialShowSourceDetails = initialShowSourceDetails,
                                    modifier = Modifier.padding(innerPadding),
                                )
                            }
                        }
                    }
                }
                AppDestination.LIBRARY -> {
                    LibraryScreen(
                        sessionHistory = uiState.sessionHistory,
                        onSelectHistoryItem = { item ->
                            viewModel.onSelectHistoryItem(item)
                            currentDestination = AppDestination.CHECK
                            checkSubDestination = CheckSubDestination.RESULTS
                        },
                        onToggleSave = viewModel::onToggleSaveClaim,
                        onRemoveSaved = viewModel::onRemoveSavedClaim,
                        onNavigateToCheck = { currentDestination = AppDestination.CHECK },
                        initialTab = initialLibraryTab,
                        initialSearchQuery = initialSearchQuery,
                        initialFilter = initialLibraryFilter,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}
