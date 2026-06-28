package com.example.copilotui.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current

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
                    "Let's build.",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.W700,
                    color = TextPrimary,
                    letterSpacing = (-0.4).sp,
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

        // 2×2 grid
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(11.dp), modifier = Modifier.fillMaxWidth()) {
                HomeCard(
                    icon = Icons.Rounded.Add,
                    label = "Start New\nProject",
                    dark = true,
                    modifier = Modifier.weight(1f),
                    onClick = onNewProject,
                )
                HomeCard(
                    icon = Icons.Rounded.PlayArrow,
                    label = "Continue\nProject",
                    dark = false,
                    modifier = Modifier.weight(1f),
                    onClick = onContinue,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(11.dp), modifier = Modifier.fillMaxWidth()) {
                HomeCard(
                    icon = Icons.Rounded.Description,
                    label = "Load\nBlueprint",
                    dark = false,
                    modifier = Modifier.weight(1f),
                    onClick = { Toast.makeText(context, "Coming Soon", Toast.LENGTH_SHORT).show() },
                )
                HomeCard(
                    icon = Icons.Rounded.PlayCircle,
                    label = "Demo\nMode",
                    dark = false,
                    modifier = Modifier.weight(1f),
                    onClick = onDemo,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(LightCardBorder)
        )
        NavBar(
            selected = 0,
            onProjects = {},
            onScanner = onScanner,
            onSettings = onSettings,
            modifier = Modifier.background(LightSurface),
        )
    }
}

@Composable
private fun HomeCard(
    icon: ImageVector,
    label: String,
    dark: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val bg = if (dark) CtaDark else LightCard
    val iconBg = if (dark) Color.White.copy(alpha = 0.14f) else Color(0xFFF0F0EC)
    val iconTint = if (dark) Color.White else Color(0xFF33332F)
    val textColor = if (dark) Color.White else TextPrimary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .then(
                if (!dark) Modifier.border(1.5.dp, LightCardBorder, RoundedCornerShape(16.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
            .defaultMinSize(minHeight = 106.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text(
                label,
                fontSize = 14.sp,
                fontWeight = FontWeight.W600,
                color = textColor,
                lineHeight = 18.sp,
            )
        }
    }
}
