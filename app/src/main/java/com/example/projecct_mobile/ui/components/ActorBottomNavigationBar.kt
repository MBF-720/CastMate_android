package com.example.projecct_mobile.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.ui.theme.*

/**
 * Barre de navigation inférieure réutilisable pour les écrans acteur
 */
@Composable
fun ActorBottomNavigationBar(
    selectedItem: NavigationItem = NavigationItem.HOME,
    onCandidaturesClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Navbar flottante avec transparence
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = DarkBlue.copy(alpha = 0.3f)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = White.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mes candidatures
                ActorNavigationItem(
                    icon = Icons.Default.CalendarToday,
                    label = "Candidatures",
                    onClick = onCandidaturesClick,
                    isSelected = selectedItem == NavigationItem.CANDIDATURES
                )
                
                // Home
                ActorNavigationItem(
                    icon = Icons.Default.Home,
                    label = "Home",
                    onClick = onHomeClick,
                    isSelected = selectedItem == NavigationItem.HOME
                )
                
                // Profile
                ActorNavigationItem(
                    icon = Icons.Default.Person,
                    label = "Profile",
                    onClick = onProfileClick,
                    isSelected = selectedItem == NavigationItem.PROFILE
                )
            }
        }
    }
}

@Composable
private fun ActorNavigationItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    isSelected: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) DarkBlue else GrayBorder,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isSelected) DarkBlue else GrayBorder,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

enum class NavigationItem {
    CANDIDATURES,
    HOME,
    PROFILE
}

