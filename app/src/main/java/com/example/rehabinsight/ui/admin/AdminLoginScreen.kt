package com.example.rehabinsight.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.rehabinsight.ui.components.CalmPage
import com.example.rehabinsight.ui.components.GentleError
import com.example.rehabinsight.ui.components.GradientScreenBackground
import com.example.rehabinsight.ui.components.HaloIcon
import com.example.rehabinsight.ui.components.PrimaryButton
import com.example.rehabinsight.ui.components.RehabTextField
import com.example.rehabinsight.ui.components.SoftCard
import com.example.rehabinsight.ui.theme.RehabPrimaryDeep
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
        CalmPage(horizontalPadding = 24.dp, horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(48.dp))
            HaloIcon(Icons.Rounded.AdminPanelSettings, diameter = 132.dp)
            Spacer(Modifier.height(18.dp))
            Text("Admin access", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                "Enter the admin passcode to view the dashboard.",
                style = MaterialTheme.typography.bodyMedium,
                color = RehabTextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))

            SoftCard {
                RehabTextField(
                    value = passcode,
                    onValueChange = { passcode = it; onClearError() },
                    placeholder = "Admin passcode",
                    leadingIcon = Icons.Rounded.Lock,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation()
                )
                if (authError != null) {
                    Spacer(Modifier.height(14.dp))
                    GentleError(authError)
                }
                Spacer(Modifier.height(18.dp))
                PrimaryButton(text = "Enter dashboard", onClick = { onLogin(passcode) })
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "Back to app",
                style = MaterialTheme.typography.labelLarge,
                color = RehabPrimaryDeep,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable { onBack() }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
