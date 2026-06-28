package com.example.copilotui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.copilotui.ui.theme.*

@Composable
fun CompletionScreen(onShare: () -> Unit, onNewProject: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        StatusBar(dark = true)

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .padding(top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Check icon
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(74.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(38.dp))
            }

            Text(
                "Project Complete",
                fontSize = 23.sp,
                fontWeight = FontWeight.W700,
                color = TextDark,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.3).sp,
                modifier = Modifier.padding(top = 20.dp),
            )
            Text(
                "Backyard Cabin",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.W500,
                color = TextDarkSecondary,
                modifier = Modifier.padding(top = 6.dp),
            )

            // Stats
            Row(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                listOf(
                    Triple("14d", "TIME SPENT", Modifier.weight(1f)),
                    Triple("96%", "ACCURACY", Modifier.weight(1f)),
                    Triple("38", "MATERIALS", Modifier.weight(1f)),
                ).forEach { (value, label, mod) ->
                    Column(
                        modifier = mod
                            .clip(RoundedCornerShape(11.dp))
                            .background(DarkCard)
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(11.dp))
                            .padding(11.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(value, fontSize = 17.sp, fontWeight = FontWeight.W700, color = TextDark)
                        Text(label, fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.W500, color = TextDarkSecondary, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }
        }

        Column(
            modifier = Modifier.padding(16.dp, 14.dp, 16.dp, 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(13.dp))
                    .background(Color.White)
                    .clickable(onClick = onShare)
                    .padding(14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Share, null, tint = Color(0xFF1B1C20), modifier = Modifier.size(17.dp))
                    Text("Share Project", fontSize = 14.sp, fontWeight = FontWeight.W600, color = Color(0xFF1B1C20))
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(13.dp))
                    .border(1.5.dp, Color(0xFF3A3B42), RoundedCornerShape(13.dp))
                    .clickable(onClick = onNewProject)
                    .padding(13.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Add, null, tint = TextDark, modifier = Modifier.size(17.dp))
                    Text("Start New Project", fontSize = 13.sp, fontWeight = FontWeight.W600, color = TextDark)
                }
            }
        }
    }
}
