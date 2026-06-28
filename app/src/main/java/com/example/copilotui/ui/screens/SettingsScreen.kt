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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.copilotui.ui.theme.*

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var offlineMode by remember { mutableStateOf(true) }
    var batterySaver by remember { mutableStateOf(false) }
    var darkMode by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize().background(LightBg)) {
        StatusBar()
        Text(
            "Settings",
            fontSize = 17.sp,
            fontWeight = FontWeight.W600,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Preferences
            SettingsSection(label = "PREFERENCES") {
                ToggleRow("Offline Mode", offlineMode, first = true) { offlineMode = it }
                ToggleRow("Battery Saver", batterySaver) { batterySaver = it }
                ToggleRow("Dark Mode", darkMode, last = true) { darkMode = it }
            }

            // On-device AI
            SettingsSection(label = "ON-DEVICE AI") {
                StatusRow("Device AI Status", "Hexagon NPU", first = true)
                StatusRow("Model Version", "v2.4.1")
                InfoRow("Storage Used", "1.2 GB", last = true)
            }

            // About
            SettingsSection(label = "ABOUT") {
                NavSettingsRow("App Version", "1.0.0 (build 42)", first = true)
                NavSettingsRow("Privacy Policy", null)
                NavSettingsRow("Terms of Service", null, last = true)
            }

            // Sign out
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.5.dp, LightCardBorder, RoundedCornerShape(12.dp))
                    .clickable {}
                    .padding(12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Sign Out", fontSize = 13.sp, fontWeight = FontWeight.W600, color = Color(0xFFBE4A4A))
            }
        }
    }
}

@Composable
private fun SettingsSection(label: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.W500, color = TextSecondary)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(LightCard)
                .border(1.5.dp, LightCardBorder, RoundedCornerShape(12.dp)),
            content = content,
        )
    }
}

@Composable
private fun ColumnScope.ToggleRow(label: String, checked: Boolean, first: Boolean = false, last: Boolean = false, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!last) Modifier.border(width = 1.dp, color = LightDivider, shape = RoundedCornerShape(0.dp)) else Modifier)
            .padding(horizontal = 13.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.W500, color = TextPrimary)
        Toggle(checked = checked, onToggle = onToggle)
    }
}

@Composable
private fun Toggle(checked: Boolean, onToggle: (Boolean) -> Unit) {
    val trackColor = if (checked) CtaDark else Color(0xFFD6D6CD)
    val thumbX = if (checked) 18.dp else 2.dp
    Box(
        modifier = Modifier
            .size(width = 36.dp, height = 20.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(trackColor)
            .clickable { onToggle(!checked) },
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp, start = thumbX)
                .size(16.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
private fun ColumnScope.StatusRow(label: String, status: String, first: Boolean = false, last: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!last) Modifier.border(width = 1.dp, color = LightDivider, shape = RoundedCornerShape(0.dp)) else Modifier)
            .padding(horizontal = 13.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.W500, color = TextPrimary)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(OnlineDot))
            Text(status, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.W500, color = OnlineDot)
        }
    }
}

@Composable
private fun ColumnScope.InfoRow(label: String, value: String, last: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!last) Modifier.border(width = 1.dp, color = LightDivider, shape = RoundedCornerShape(0.dp)) else Modifier)
            .padding(horizontal = 13.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.W500, color = TextPrimary)
        Text(value, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.W500, color = TextSecondary)
    }
}

@Composable
private fun ColumnScope.NavSettingsRow(label: String, value: String?, first: Boolean = false, last: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!last) Modifier.border(width = 1.dp, color = LightDivider, shape = RoundedCornerShape(0.dp)) else Modifier)
            .clickable {}
            .padding(horizontal = 13.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.W500, color = TextPrimary)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (value != null) Text(value, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
            Icon(Icons.Rounded.ChevronRight, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        }
    }
}
