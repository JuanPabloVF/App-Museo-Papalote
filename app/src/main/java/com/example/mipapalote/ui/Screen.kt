import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val icon: ImageVector, val title: String) {
    object Home : Screen("home", Icons.Filled.Map, "Mapa")
    object Dashboard : Screen("dashboard", Icons.Filled.Dashboard, "Dashboard")
    object QR : Screen("qr", Icons.Filled.QrCode, "QR")
    object Feedback : Screen("feedback", Icons.Filled.Comment, "Feedback")
    object Profile : Screen("profile", Icons.Filled.AccountCircle, "Perfil")
    object AdminDashboard : Screen("adminDashboard", Icons.Filled.Dashboard, "Admin")
    object Statistics : Screen(route = "adminStatistics", icon = Icons.Filled.PieChart,title = "Statistics")
}