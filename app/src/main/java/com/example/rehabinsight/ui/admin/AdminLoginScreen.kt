package com.example.rehabinsight.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.rehabinsight.ui.components.GradientScreenBackground
import com.example.rehabinsight.ui.components.PrimaryButton
import com.example.rehabinsight.ui.theme.RehabBlue
import com.example.rehabinsight.ui.theme.RehabTextSecondary

@Composable
fun AdminLoginScreen(
    authError: String?,
    onClearError: () -> Unit,
    onLogin: (String) -> Unit,
    onBack: () -> Unit
) {
    var passcode by remember { mutableStateOf("") }

    GradientScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))
            Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = RehabBlue, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(16.dp))
            Text("Admin access", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Enter the admin passcode to view the dashboard.",
                style = MaterialTheme.typography.bodyMedium,
                color = RehabTextSecondary
            )
            Spacer(Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    OutlinedTextField(
                        value = passcode,
                        onValueChange = { passcode = it; onClearError() },
                        placeholder = { Text("Admin passcode") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (authError != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(authError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(16.dp))
                    PrimaryButton(text = "Enter dashboard", onClick = { onLogin(passcode) })
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "Back to app",
                style = MaterialTheme.typography.bodyMedium,
                color = RehabTextSecondary,
                modifier = Modifier.clickable { onBack() }
            )
        }
    }
}
