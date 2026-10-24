package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Member
import com.example.data.repository.SupabaseRepository
import com.example.ui.admin.AdminScreen
import com.example.ui.admin.AdminViewModel
import com.example.ui.auth.AuthState
import com.example.ui.auth.AuthViewModel
import com.example.ui.auth.LoginScreen
import com.example.ui.auth.RegisterScreen
import com.example.ui.home.HomeScreen
import com.example.ui.quiz.QuizScreen
import com.example.ui.quiz.QuizViewModel
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val repository = SupabaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkCanvas),
                    color = DarkCanvas
                ) {
                    ExacTopApp(repository = repository)
                }
            }
        }
    }
}

@Composable
fun ExacTopApp(repository: SupabaseRepository) {
    val authViewModel = remember { AuthViewModel(repository) }
    val quizViewModel = remember { QuizViewModel(repository) }
    val adminViewModel = remember { AdminViewModel(repository) }

    var currentScreen by remember { mutableStateOf(Screen.LOGIN) }
    var activeMember by remember { mutableStateOf<Member?>(null) }

    val authState by authViewModel.authState.collectAsState()
    val currentMemberFromVm by authViewModel.currentMember.collectAsState()

    // Sync active member when authenticated or updated
    LaunchedEffect(authState, currentMemberFromVm) {
        if (authState is AuthState.Authenticated) {
            activeMember = (authState as AuthState.Authenticated).member
        } else if (currentMemberFromVm != null) {
            activeMember = currentMemberFromVm
        }
    }

    when (currentScreen) {
        Screen.LOGIN -> {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegister = {
                    authViewModel.clearError()
                    currentScreen = Screen.REGISTER
                },
                onLoginSuccess = {
                    currentScreen = Screen.HOME
                }
            )
        }

        Screen.REGISTER -> {
            BackHandler {
                authViewModel.clearError()
                currentScreen = Screen.LOGIN
            }
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateBackToLogin = {
                    authViewModel.clearError()
                    currentScreen = Screen.LOGIN
                },
                onRegisterSuccess = {
                    currentScreen = Screen.HOME
                }
            )
        }

        Screen.HOME -> {
            val user = currentMemberFromVm ?: activeMember ?: Member(
                nama = "Siswa STEM",
                grade = 10,
                skor_akumulasi = "[]"
            )
            HomeScreen(
                member = user,
                authViewModel = authViewModel,
                onStartQuiz = { subject ->
                    quizViewModel.startQuiz(subject, user)
                    currentScreen = Screen.QUIZ
                },
                onOpenAdmin = {
                    currentScreen = Screen.ADMIN
                },
                onLogout = {
                    authViewModel.logout()
                    activeMember = null
                    currentScreen = Screen.LOGIN
                }
            )
        }

        Screen.QUIZ -> {
            val user = currentMemberFromVm ?: activeMember ?: Member(
                nama = "Siswa STEM",
                grade = 10
            )
            BackHandler {
                quizViewModel.reset()
                currentScreen = Screen.HOME
            }
            QuizScreen(
                member = user,
                quizViewModel = quizViewModel,
                onQuizCancelled = {
                    quizViewModel.reset()
                    currentScreen = Screen.HOME
                },
                onQuizCompleted = {
                    quizViewModel.reset()
                    authViewModel.refreshCurrentMember(user.email)
                    currentScreen = Screen.HOME
                }
            )
        }

        Screen.ADMIN -> {
            BackHandler {
                currentScreen = Screen.HOME
            }
            AdminScreen(
                adminViewModel = adminViewModel,
                onBackToHome = {
                    currentScreen = Screen.HOME
                }
            )
        }
    }
}
