package com.example.copilotui.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.copilotui.ui.theme.*

fun Modifier.arHatchBackground(): Modifier = drawBehind {
    val stripe = 8.dp.toPx()
    drawRect(DarkStripe1)
    var offset = -size.height
    var idx = 0
    while (offset < size.width + size.height) {
        if (idx % 2 == 0) {
            val path = Path().apply {
                moveTo(offset, 0f)
                lineTo(offset + stripe, 0f)
                lineTo(offset + stripe - size.height, size.height)
                lineTo(offset - size.height, size.height)
                close()
            }
            drawPath(path, DarkStripe2)
        }
        idx++
        offset += stripe
    }
}

@Composable
fun StatusBar(dark: Boolean = false, modifier: Modifier = Modifier) {
    val textColor = if (dark) TextDarkMuted else TextMono
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("9:41", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.W600, fontSize = 10.sp, color = textColor)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(Icons.Rounded.SignalCellular4Bar, null, tint = textColor, modifier = Modifier.size(13.dp))
            Icon(Icons.Rounded.Wifi, null, tint = textColor, modifier = Modifier.size(14.dp))
            Icon(Icons.Rounded.BatteryFull, null, tint = textColor, modifier = Modifier.size(17.dp))
        }
    }
}

@Composable
fun NavBar(
    selected: Int,
    onProjects: () -> Unit,
    onScanner: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavBarItem(Icons.Rounded.Apps, "Projects", selected == 0, onProjects)
        NavBarItem(Icons.Rounded.CropFree, "Scanner", selected == 1, onScanner)
        NavBarItem(Icons.Rounded.Tune, "Settings", selected == 2, onSettings)
    }
}

@Composable
fun NavBarItem(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    val color = if (active) CtaDark else Color(0xFFA3A39B)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .width(72.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(19.dp))
        Text(
            label,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (active) FontWeight.W600 else FontWeight.W500,
            color = color,
        )
    }
}
