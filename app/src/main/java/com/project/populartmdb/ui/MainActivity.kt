package com.project.populartmdb.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Button
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.project.populartmdb.database.UserDao
import com.project.populartmdb.model.Movie
import com.project.populartmdb.model.User
import com.example.uasanggaliansyahputra210040085.ui.theme.PopularTMDBTheme
import com.project.populartmdb.api.ApiClient
import com.project.populartmdb.database.AppDatabase
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PopularTMDBTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun MovieItem(movie: Movie) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = movie.title,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
            Text(
                text = movie.overview,
                style = TextStyle(
                    fontSize = 14.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    val movieApiService = ApiClient.create()
    val popularMovies = remember { mutableStateOf<List<Movie>>(emptyList()) }

    LaunchedEffect(Unit) {
        val response = movieApiService.getPopularMovies("2d9f6e6e214d75b979615b2062c6a215", 1)
        popularMovies.value = response.results
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp), // Use the padding parameter here
    ) {
        popularMovies.value.forEach { movie ->
            MovieItem(movie)
        }
    }
}

@Composable
fun ProfileScreen(navController: NavController, userDao: UserDao) {
    val user by produceState<User?>(initialValue = null) {
        value = userDao.getUser()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(user?.name ?: "John Doe", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(user?.email ?: "john.doe@example.com", fontSize = 16.sp)
        Text(user?.phone ?: "08123456789", fontSize = 16.sp)
        Button(
            onClick = { navController.navigate("editProfile") },
            modifier = Modifier.padding(top = 30.dp)
        ) {
            Text(
                text = "Edit Profile",
                color = Color.White
            )
        }
    }
}

@Composable
fun EditProfileScreen(navController: NavController, userDao: UserDao) {
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        TextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
        Button(onClick = {
            val user = User(id = 1, name = name, email = email, phone = phone)
            scope.launch {
                userDao.insertUser(user)
            }
            navController.navigate("profile")
        }) {
            Text(text = "Save", color = Color.White)
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val db = Room.databaseBuilder(
        context,
        AppDatabase::class.java, "database-name"
    ).build()
    val userDao = db.userDao()

    Scaffold(
        bottomBar = {
            BottomNavigation {
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = navController.currentDestination?.route == "home",
                    onClick = {
                        navController.navigate("home")
                    }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = navController.currentDestination?.route == "profile",
                    onClick = {
                        navController.navigate("profile")
                    }
                )
            }
        },
        content = { padding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(16.dp)
            ) {
                composable("home") {
                    HomeScreen(navController)
                }
                composable("profile") {
                    ProfileScreen(navController, userDao)
                }
                composable("editProfile") {
                    EditProfileScreen(navController, userDao)
                }
            }
        }
    )
}
