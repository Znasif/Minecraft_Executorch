package com.example.copilotui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.copilotui.ui.theme.*

@Composable
fun HomeDashboardScreen(
    onNewProject: () -> Unit,
    onContinue: () -> Unit,
    onDemo: () -> Unit,
    onScanner: () -> Unit,
    onSettings: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
    ) {
        StatusBar()

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                Text(
                    "GOOD MORNING",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.W500,
                    color = TextSecondary,
                )
                Text(
                    "Good morning, Builder",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.W700,
                    color = TextPrimary,
                    letterSpacing = (-0.5).sp,
                )
                Text(
                    "What will you build today?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W400,
                    color = TextSecondary,
                )
            }
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CtaDark),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Home, null, tint = Color.White, modifier = Modifier.size(19.dp))
            }
        }

        // Cards
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HeroCard(onClick = onNewProject)

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                HomeCard(
                    icon = Icons.Rounded.PlayCircle,
                    title = "Demo Mode",
                    subtitle = "Try a sample build",
                    modifier = Modifier.weight(1f),
                    onClick = onDemo,
                )
                HomeCard(
                    icon = Icons.Rounded.Settings,
                    title = "Settings",
                    subtitle = "Configure app",
                    modifier = Modifier.weight(1f),
                    onClick = onSettings,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(LightCardBorder)
        )
        HomeNavBar(
            selected = 0,
            onProjects = {},
            onScanner = onScanner,
            onSettings = onSettings,
        )
    }
}

@Composable
private fun HeroCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF006064), Color(0xFF00838F)),
                )
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Add, null, tint = Color.White, modifier = Modifier.size(44.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "Start New Project",
                fontSize = 20.sp,
                fontWeight = FontWeight.W700,
                color = Color.White,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Point your phone at any flat surface",
                fontSize = 14.sp,
                fontWeight = FontWeight.W400,
                color = Color.White.copy(alpha = 0.80f),
            )
        }
    }
}

@Composable
private fun HomeCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 140.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(LightCard)
            .border(1.5.dp, LightCardBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF0F0EC)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = Color(0xFF33332F), modifier = Modifier.size(36.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(
                title,
                fontSize = 20.sp,
                fontWeight = FontWeight.W700,
                color = TextPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.W400,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun HomeNavBar(
    selected: Int,
    onProjects: () -> Unit,
    onScanner: () -> Unit,
    onSettings: () -> Unit,
) {
    val teal = Color(0xFF00838F)
    val inactive = Color(0xFF888884)

    val tabs = listOf(
        Triple(Icons.Rounded.Folder, "Projects", onProjects),
        Triple(Icons.Rounded.CameraAlt, "Scanner", onScanner),
        Triple(Icons.Rounded.Settings, "Settings", onSettings),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(LightSurface)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEachIndexed { index, (icon, label, action) ->
            val isActive = index == selected
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(onClick = action),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = if (isActive) teal else inactive,
                    modifier = Modifier.size(28.dp),
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    label,
                    fontSize = 11.sp,
                    fontWeight = if (isActive) FontWeight.W600 else FontWeight.W400,
                    color = if (isActive) teal else inactive,
                )
            }
        }
    }
}
