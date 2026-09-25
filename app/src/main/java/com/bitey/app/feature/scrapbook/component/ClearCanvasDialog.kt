package com.bitey.app.feature.scrapbook.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme

@Composable
fun ClearCanvasDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val theme = LocalNeumorphicTheme.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Clear Story Canvas", color = theme.inkPrimary) },
        text = { Text("Remove all stickers and accessories from this canvas?", color = theme.inkSecondary) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))
            ) { Text("Clear All") }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceVariant, contentColor = theme.inkPrimary)
            ) { Text("Cancel") }
        },
        containerColor = theme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}
