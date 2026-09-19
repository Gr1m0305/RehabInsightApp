package com.example.rehabinsight.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.rehabinsight.ui.components.GradientScreenBackground
import com.example.rehabinsight.ui.components.PrimaryButton
import com.example.rehabinsight.ui.theme.RehabBlue
import com.example.rehabinsight.ui.theme.RehabTextMuted
import com.example.rehabinsight.ui.theme.RehabTextSecondary

@Composable
fun AuthScreen(
    authError: String?,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(72.dp))

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(color = RehabBlue, shape = RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Favorite, contentDescription = null, tint = Color.White)
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "REHAB INSIGHT",
                style = MaterialTheme.typography.labelLarge,
                color = RehabBlue,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (isSignUpMode) "Create your account" else "Welcome",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "A gentle space to reflect, check in, and keep moving forward.",
                style = MaterialTheme.typography.bodyMedium,
                color = RehabTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    if (isSignUpMode) {
                        Text("Full name", style = MaterialTheme.typography.bodyMedium, color = RehabTextSecondary)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it; onClearError() },
                            placeholder = { Text("Your name") },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors()
                        )
                        Spacer(Modifier.height(16.dp))

                        Text("Phone number (optional)", style = MaterialTheme.typography.bodyMedium, color = RehabTextSecondary)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it.filter { c -> c.isDigit() }.take(10); onClearError() },
                            placeholder = { Text("Your phone number") },
                            leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(16.dp))
                    }

                    Text("Email address", style = MaterialTheme.typography.bodyMedium, color = RehabTextSecondary)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; onClearError() },
                        placeholder = { Text("you@example.com") },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))
                    Text("Password", style = MaterialTheme.typography.bodyMedium, color = RehabTextSecondary)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; onClearError() },
                        placeholder = { Text("Enter your password") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (authError != null) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            authError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (!isSignUpMode) {
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = {}, modifier = Modifier.align(Alignment.End)) {
                            Text("Forgot password?", style = MaterialTheme.typography.bodySmall, color = RehabBlue)
                        }
                    } else {
                        Spacer(Modifier.height(16.dp))
                    }

                    PrimaryButton(
                        text = if (isSignUpMode) "Create account" else "Sign in",
                        onClick = {
                            if (isSignUpMode) {
                                onSignUp(name, email, phone, password)
                            } else {
                                onLogin(email, password)
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Row {
                Text(
                    if (isSignUpMode) "Already have an account? " else "New to Rehab Insight? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RehabTextSecondary
                )
                Text(
                    if (isSignUpMode) "Sign in" else "Create account",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = RehabBlue,
                    modifier = Modifier.clickable {
                        isSignUpMode = !isSignUpMode
                        onClearError()
                    }
                )
            }

            Spacer(Modifier.height(28.dp))
            Text(
                "Admin login",
                style = MaterialTheme.typography.bodySmall,
                color = RehabTextMuted,
                modifier = Modifier.clickable { onAdminLoginClick() }
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
