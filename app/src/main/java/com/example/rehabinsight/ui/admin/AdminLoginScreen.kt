package com.example.rehabinsight.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.VisualTransformation
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
    isBusy: Boolean,
    onClearError: () -> Unit,
    onLogin: (email: String, password: String) -> Unit,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    GradientScreenBackground {
        CalmPage(horizontalPadding = 24.dp, horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(48.dp))
            HaloIcon(Icons.Rounded.AdminPanelSettings, diameter = 132.dp)
            Spacer(Modifier.height(18.dp))
            Text("Admin access", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                "Sign in with your admin account to view the dashboard.",
                style = MaterialTheme.typography.bodyMedium,
                color = RehabTextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))

            SoftCard {
                RehabTextField(
                    value = email,
                    onValueChange = { email = it; onClearError() },
                    label = "Email address",
                    placeholder = "you@example.com",
                    leadingIcon = Icons.Rounded.Email,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                Spacer(Modifier.height(16.dp))
                RehabTextField(
                    value = password,
                    onValueChange = { password = it; onClearError() },
                    label = "Password",
                    placeholder = "Enter your password",
                    leadingIcon = Icons.Rounded.Lock,
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
                )
                if (authError != null) {
                    Spacer(Modifier.height(14.dp))
                    GentleError(authError)
                }
                Spacer(Modifier.height(18.dp))
                // Signing in waits on the server, so the button says so rather than appearing to do nothing.
                PrimaryButton(
                    text = if (isBusy) "Signing you in…" else "Enter dashboard",
                    enabled = !isBusy,
                    onClick = { onLogin(email, password) }
                )
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
