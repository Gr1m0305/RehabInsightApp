package com.example.rehabinsight.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rehabinsight.ui.components.CalmPage
import com.example.rehabinsight.ui.components.GentleError
import com.example.rehabinsight.ui.components.GradientScreenBackground
import com.example.rehabinsight.ui.components.PrimaryButton
import com.example.rehabinsight.ui.components.PrimaryGradient
import com.example.rehabinsight.ui.components.RehabTextField
import com.example.rehabinsight.ui.components.SoftCard
import com.example.rehabinsight.ui.components.softShadow
import com.example.rehabinsight.ui.theme.RehabPrimary
import com.example.rehabinsight.ui.theme.RehabPrimaryDeep
import com.example.rehabinsight.ui.theme.RehabTextSecondary

@Composable
fun AuthScreen(
    authError: String?,
    isBusy: Boolean,
    onClearError: () -> Unit,
    onLogin: (email: String, password: String) -> Unit,
    onSignUp: (name: String, email: String, phone: String, password: String) -> Unit,
    onAdminLoginClick: () -> Unit
) {
    var isSignUpMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    GradientScreenBackground {
        CalmPage(horizontalPadding = 24.dp, horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(16.dp))

            // Logo mark resting in two soft halos
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(RehabPrimary.copy(alpha = 0.06f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .background(RehabPrimary.copy(alpha = 0.09f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .softShadow(MaterialTheme.shapes.large, 12.dp)
                            .background(PrimaryGradient, MaterialTheme.shapes.large),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Favorite,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "REHAB INSIGHT",
                style = MaterialTheme.typography.labelMedium,
                color = RehabPrimaryDeep,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (isSignUpMode) "Create your account" else "Welcome",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "A gentle space to reflect, check in, and keep moving forward.",
                style = MaterialTheme.typography.bodyMedium,
                color = RehabTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(22.dp))

            SoftCard(contentPadding = PaddingValues(22.dp)) {
                if (isSignUpMode) {
                    RehabTextField(
                        value = name,
                        onValueChange = { name = it; onClearError() },
                        label = "Full name",
                        placeholder = "Your name",
                        leadingIcon = Icons.Rounded.Person
                    )
                    Spacer(Modifier.height(16.dp))

                    RehabTextField(
                        value = phone,
                        onValueChange = { phone = it.filter { c -> c.isDigit() }.take(10); onClearError() },
                        label = "Phone number (optional)",
                        placeholder = "Your phone number",
                        leadingIcon = Icons.Rounded.Phone,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    Spacer(Modifier.height(16.dp))
                }

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

                if (!isSignUpMode) {
                    Spacer(Modifier.height(6.dp))
                    TextButton(onClick = {}, modifier = Modifier.align(Alignment.End)) {
                        Text("Forgot password?", style = MaterialTheme.typography.labelMedium, color = RehabPrimary)
                    }
                    Spacer(Modifier.height(2.dp))
                } else {
                    Spacer(Modifier.height(20.dp))
                }

                // Both actions wait on the server, so the button says so rather than appearing to do nothing.
                PrimaryButton(
                    text = when {
                        isBusy && isSignUpMode -> "Creating your account…"
                        isBusy -> "Signing you in…"
                        isSignUpMode -> "Create account"
                        else -> "Sign in"
                    },
                    enabled = !isBusy,
                    onClick = {
                        if (isSignUpMode) {
                            onSignUp(name, email, phone, password)
                        } else {
                            onLogin(email, password)
                        }
                    }
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (isSignUpMode) "Already have an account? " else "New to Rehab Insight? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RehabTextSecondary
                )
                Text(
                    if (isSignUpMode) "Sign in" else "Create account",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = RehabPrimaryDeep,
                    modifier = Modifier.clickable {
                        isSignUpMode = !isSignUpMode
                        onClearError()
                    }
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Admin login",
                style = MaterialTheme.typography.bodySmall,
                color = RehabTextSecondary,
                modifier = Modifier.clickable { onAdminLoginClick() }
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}
