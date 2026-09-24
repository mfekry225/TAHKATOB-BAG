package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.AudioRecorderHelper
import com.example.ui.SpeechViewModel
import com.example.ui.TtsHelper
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private lateinit var ttsHelper: TtsHelper
    private lateinit var audioHelper: AudioRecorderHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize helper tools
        ttsHelper = TtsHelper(this)
        audioHelper = AudioRecorderHelper(this)

        setContent {
            MyApplicationTheme {
                val speechViewModel: SpeechViewModel = viewModel()
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "dashboard"
                ) {
                    composable("dashboard") {
                        DashboardScreen(
                            viewModel = speechViewModel,
                            onNavigate = { route -> navController.navigate(route) }
                        )
                    }

                    composable("first_words") {
                        FirstWordsScreen(
                            viewModel = speechViewModel,
                            audioHelper = audioHelper,
                            onSpeak = { text -> if (!speechViewModel.isMuted.value) ttsHelper.speak(text) },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("cards") {
                        FlashcardsScreen(
                            viewModel = speechViewModel,
                            audioHelper = audioHelper,
                            onSpeak = { text -> if (!speechViewModel.isMuted.value) ttsHelper.speak(text) },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("letters") {
                        LettersNumbersScreen(
                            viewModel = speechViewModel,
                            onSpeak = { text -> if (!speechViewModel.isMuted.value) ttsHelper.speak(text) },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("drawing") {
                        DrawingScreen(
                            viewModel = speechViewModel,
                            onSpeak = { text -> if (!speechViewModel.isMuted.value) ttsHelper.speak(text) },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("draw_and_play") {
                        DrawAndPlayScreen(
                            viewModel = speechViewModel,
                            onSpeak = { text -> if (!speechViewModel.isMuted.value) ttsHelper.speak(text) },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("games") {
                        SmartGamesScreen(
                            viewModel = speechViewModel,
                            onSpeak = { text -> if (!speechViewModel.isMuted.value) ttsHelper.speak(text) },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("visual_cancellation") {
                        VisualCancellationScreen(
                            viewModel = speechViewModel,
                            onSpeak = { text -> if (!speechViewModel.isMuted.value) ttsHelper.speak(text) },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("smart_maze") {
                        SmartMazeScreen(
                            viewModel = speechViewModel,
                            onSpeak = { text -> if (!speechViewModel.isMuted.value) ttsHelper.speak(text) },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("animated_matching_lines") {
                        AnimatedMatchingLinesScreen(
                            viewModel = speechViewModel,
                            onSpeak = { text -> if (!speechViewModel.isMuted.value) ttsHelper.speak(text) },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("clock") {
                        ClockLearningScreen(
                            viewModel = speechViewModel,
                            onSpeak = { text -> if (!speechViewModel.isMuted.value) ttsHelper.speak(text) },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("matching") {
                        MatchingGameScreen(
                            viewModel = speechViewModel,
                            onSpeak = { text -> if (!speechViewModel.isMuted.value) ttsHelper.speak(text) },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("math") {
                        MathLearningScreen(
                            viewModel = speechViewModel,
                            onSpeak = { text -> if (!speechViewModel.isMuted.value) ttsHelper.speak(text) },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("verbs") {
                        VerbsLearningScreen(
                            viewModel = speechViewModel,
                            onSpeak = { text -> if (!speechViewModel.isMuted.value) ttsHelper.speak(text) },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("children_list") {
                        ChildrenListScreen(
                            viewModel = speechViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("blog_info") {
                        BlogInfoScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("speech_exercises") {
                        SpeechExercisesScreen(
                            viewModel = speechViewModel,
                            audioHelper = audioHelper,
                            onSpeak = { text -> if (!speechViewModel.isMuted.value) ttsHelper.speak(text) },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("lisping_treatment") {
                        LispingTreatmentScreen(
                            viewModel = speechViewModel,
                            onSpeak = { text -> if (!speechViewModel.isMuted.value) ttsHelper.speak(text) },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("non_verbal") {
                        NonVerbalCommunicationScreen(
                            viewModel = speechViewModel,
                            onSpeak = { text -> if (!speechViewModel.isMuted.value) ttsHelper.speak(text) },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("attention_focus") {
                        AttentionFocusScreen(
                            viewModel = speechViewModel,
                            onSpeak = { text -> if (!speechViewModel.isMuted.value) ttsHelper.speak(text) },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("cms_console") {
                        CmsConsoleScreen(
                            viewModel = speechViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsHelper.shutdown()
        audioHelper.stopRecording()
        audioHelper.stopPlaying()
    }
}
