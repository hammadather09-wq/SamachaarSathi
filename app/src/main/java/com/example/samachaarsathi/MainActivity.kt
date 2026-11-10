package com.example.samachaarsathi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.samachaarsathi.data.local.RoomVerificationHistoryRepository
import com.example.samachaarsathi.data.model.DemoFixtures
import com.example.samachaarsathi.ui.CheckSubDestination
import com.example.samachaarsathi.ui.MainAppScreen
import com.example.samachaarsathi.ui.claim.ClaimInputViewModel
import com.example.samachaarsathi.ui.claim.SessionVerificationItem
import com.example.samachaarsathi.ui.component.AppDestination
import com.example.samachaarsathi.ui.library.LibraryVerdictFilter
import com.example.samachaarsathi.ui.theme.SamachaarSathiTheme
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.initialize
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Firebase.initialize(context = this)
        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance(),
        )

        val destinationExtra = intent?.getStringExtra("destination")
        val initialDestination = when (destinationExtra) {
            "library", "saved", "library_history", "library_saved", "library_search", "library_filter", "library_empty", "library_saved_empty" -> AppDestination.LIBRARY
            else -> AppDestination.CHECK
        }

        val initialSubDestination = when (destinationExtra) {
            "results", "results_sources", "source_detail", "progress",
            "results_contradicted", "results_supported", "results_misleading",
            "results_unverified", "results_reasoning", "results_share" -> CheckSubDestination.RESULTS
            else -> CheckSubDestination.INPUT
        }

        val initialLibraryTab = if (destinationExtra in listOf("saved", "library_saved", "library_saved_empty")) 1 else 0
        val initialScrollToSources = (destinationExtra == "results_sources")
        val initialScrollToShare = (destinationExtra == "results_share")
        val initialShowSourceDetails = (destinationExtra == "source_detail")
        val initialInputFocused = (destinationExtra == "check_focused")
        val initialSearchQuery = when (destinationExtra) {
            "library_search" -> "laptop"
            "library_empty" -> "nonexistent search query 123"
            else -> ""
        }
        val initialLibraryFilter = if (destinationExtra == "library_filter") LibraryVerdictFilter.FALSE else LibraryVerdictFilter.ALL

        val historyRepository = RoomVerificationHistoryRepository.getInstance(applicationContext)
        val viewModel = ViewModelProvider(
            this,
            ClaimInputViewModel.provideFactory(historyRepository),
        )[ClaimInputViewModel::class.java]

        lifecycleScope.launch {
            if (historyRepository.getCount() == 0) {
                val demoItem1 = SessionVerificationItem(
                    id = "demo_check_1",
                    claimText = "Government announced free laptop scheme for college students",
                    verdict = DemoFixtures.sampleVerdict,
                    isSaved = true,
                    timestampMs = System.currentTimeMillis() - 3600000L,
                )
                val demoItem2 = SessionVerificationItem(
                    id = "demo_check_2",
                    claimText = "RBI introduces new 5000 rupee currency notes",
                    verdict = DemoFixtures.sampleVerdictMisleading,
                    isSaved = false,
                    timestampMs = System.currentTimeMillis() - 7200000L,
                )
                val demoItem3 = SessionVerificationItem(
                    id = "demo_check_3",
                    claimText = "ISRO successfully launches new weather observation satellite into orbit",
                    verdict = DemoFixtures.sampleVerdictSupported,
                    isSaved = false,
                    timestampMs = System.currentTimeMillis() - 10800000L,
                )
                val demoItem4 = SessionVerificationItem(
                    id = "demo_check_4",
                    claimText = "Metro will operate 24 hours this weekend for holiday shoppers",
                    verdict = DemoFixtures.sampleVerdictUnverified,
                    isSaved = false,
                    timestampMs = System.currentTimeMillis() - 14400000L,
                )
                historyRepository.saveVerifications(listOf(demoItem1, demoItem2, demoItem3, demoItem4))
            }
        }

        if (destinationExtra != null) {
            when (destinationExtra) {
                "check_text" -> {
                    viewModel.onClaimTextChanged("Government is distributing free laptops to all college students. Apply online at scam-link.org")
                }
                "check_url" -> {
                    viewModel.onClaimTextChanged("https://factcheck.org/2026/05/viral-notification-rbi-5000-note-hoax/")
                }
                "check_image" -> {
                    viewModel.populateDemoOcrState()
                }
                "progress" -> {
                    viewModel.populateDemoProgressState("Government announced free laptop scheme for college students")
                }
                "results", "results_contradicted", "results_sources", "source_detail", "results_reasoning", "results_share" -> {
                    viewModel.populateDemoVerdict(DemoFixtures.sampleVerdict)
                }
                "results_supported" -> {
                    viewModel.populateDemoVerdict(DemoFixtures.sampleVerdictSupported)
                }
                "results_misleading" -> {
                    viewModel.populateDemoVerdict(DemoFixtures.sampleVerdictMisleading)
                }
                "results_unverified" -> {
                    viewModel.populateDemoVerdict(DemoFixtures.sampleVerdictUnverified)
                }
            }
        }

        enableEdgeToEdge()
        setContent {
            SamachaarSathiTheme {
                MainAppScreen(
                    viewModel = viewModel,
                    initialDestination = initialDestination,
                    initialSubDestination = initialSubDestination,
                    initialLibraryTab = initialLibraryTab,
                    initialScrollToSources = initialScrollToSources,
                    initialScrollToShare = initialScrollToShare,
                    initialShowSourceDetails = initialShowSourceDetails,
                    initialInputFocused = initialInputFocused,
                    initialSearchQuery = initialSearchQuery,
                    initialLibraryFilter = initialLibraryFilter,
                )
            }
        }
    }
}
