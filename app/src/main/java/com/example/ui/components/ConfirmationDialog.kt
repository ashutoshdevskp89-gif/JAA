package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.PendingConfirmation
import com.example.ui.theme.JaCyanAccent
import com.example.ui.theme.JaPurpleLight
import com.example.ui.theme.JaPurplePrimary
import com.example.ui.theme.JaSurfaceBorder
import com.example.ui.theme.JaSurfaceCard

@Composable
fun ConfirmationDialog(
    confirmation: PendingConfirmation,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = JaSurfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, JaCyanAccent),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("confirmation_dialog_card")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "🛡️ ${confirmation.title}",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = confirmation.message,
                    color = Color(0xFFDDD7F0),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("dialog_dismiss_button")
                    ) {
                        Text("रद्द करें", color = Color.LightGray)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = JaPurplePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("dialog_confirm_button")
                    ) {
                        Text(confirmation.confirmButtonText, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
