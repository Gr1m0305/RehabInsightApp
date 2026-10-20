package com.example.rehabinsight.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.rehabinsight.data.network.MIN_PASSWORD_LENGTH
import com.example.rehabinsight.data.network.RESET_CODE_MINUTES
import com.example.rehabinsight.data.network.RESET_CODE_RESEND_SECONDS
import com.example.rehabinsight.ui.components.GentleError
import com.example.rehabinsight.ui.components.RehabPasswordField
import com.example.rehabinsight.ui.components.RehabTextField
import com.example.rehabinsight.ui.theme.RehabPrimary
import com.example.rehabinsight.ui.theme.RehabSurface
import com.example.rehabinsight.ui.theme.RehabTextPrimary
import com.example.rehabinsight.ui.theme.RehabTextSecondary
import kotlinx.coroutines.delay

private enum class ResetStep { EMAIL, CODE, DONE }

/**
 * Resetting a forgotten password: the server emails a code to the address given, then the code
 * comes back with a new password. [onFinished] gets the email, so sign-in can start from it.
 */
@Composable
fun ForgotPasswordDialog(
    initialEmail: String,
    onRequestCode: (email: String, onResult: (String?) -> Unit) -> Unit,
    onResetPassword: (email: String, code: String, newPassword: String, onResult: (String?) -> Unit) -> Unit,
    onDismiss: () -> Unit,
    onFinished: (email: String) -> Unit
) {
    var step by remember { mutableStateOf(ResetStep.EMAIL) }
    var email by remember { mutableStateOf(initialEmail.trim()) }
    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordsVisible by remember { mutableStateOf(false) }
    var problem by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    // Counts each code sent, so the wait before another can be asked for starts again each time.
    var codesSent by remember { mutableIntStateOf(0) }
    var canResend by remember { mutableStateOf(false) }

    LaunchedEffect(codesSent) {
        if (codesSent == 0) return@LaunchedEffect
        canResend = false
        delay(RESET_CODE_RESEND_SECONDS * 1000L)
        canResend = true
    }

    fun requestCode() {
        busy = true
        problem = null
        onRequestCode(email) { result ->
            busy = false
            if (result == null) {
                step = ResetStep.CODE
                codesSent++
            } else {
                problem = result
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = RehabSurface,
        titleContentColor = RehabTextPrimary,
        textContentColor = RehabTextPrimary,
        title = {
            Text(
                when (step) {
                    ResetStep.EMAIL -> "Reset your password"
                    ResetStep.CODE -> "Check your email"
                    ResetStep.DONE -> "Password reset"
                },
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                when (step) {
                    ResetStep.EMAIL -> {
                        Text(
                            "Enter the email you signed up with and we'll send you a code to set a new password.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RehabTextSecondary
                        )
                        Spacer(Modifier.height(16.dp))
                        RehabTextField(
                            value = email,
                            onValueChange = { email = it; problem = null },
                            label = "Email address",
                            placeholder = "you@example.com",
                            leadingIcon = Icons.Rounded.Email,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                    }
                    ResetStep.CODE -> {
                        Text(
                            "If ${email.trim()} has an account, we've sent it a 6-digit code. " +
                                "It works for $RESET_CODE_MINUTES minutes.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RehabTextSecondary
                        )
                        Spacer(Modifier.height(16.dp))
                        RehabTextField(
                            value = code,
                            onValueChange = { code = it.filter { c -> c.isDigit() }.take(6); problem = null },
                            label = "Code",
                            placeholder = "6-digit code",
                            leadingIcon = Icons.Rounded.Pin,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                        )
                        Spacer(Modifier.height(16.dp))
                        RehabPasswordField(
                            value = newPassword,
                            onValueChange = { newPassword = it; problem = null },
                            label = "New password",
                            placeholder = "At least $MIN_PASSWORD_LENGTH characters",
                            visible = passwordsVisible,
                            onToggleVisible = { passwordsVisible = !passwordsVisible }
                        )
                        Spacer(Modifier.height(16.dp))
                        RehabPasswordField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it; problem = null },
                            label = "Confirm new password",
                            placeholder = "Type it once more",
                            visible = passwordsVisible,
                            onToggleVisible = { passwordsVisible = !passwordsVisible }
                        )
                        Spacer(Modifier.height(8.dp))
                        if (canResend) {
                            TextButton(enabled = !busy, onClick = ::requestCode) {
                                Text("Send a new code", style = MaterialTheme.typography.labelLarge, color = RehabPrimary)
                            }
                        } else {
                            Text(
                                "Nothing yet? Check your junk folder. You can ask for a new code in a minute.",
                                style = MaterialTheme.typography.bodySmall,
                                color = RehabTextSecondary
                            )
                        }
                    }
                    ResetStep.DONE -> {
                        Text(
                            "All done. You can sign in with your new password now.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = RehabTextSecondary
                        )
                    }
                }
                problem?.let {
                    Spacer(Modifier.height(14.dp))
                    GentleError(it)
                }
            }
        },
        confirmButton = {
            when (step) {
                ResetStep.EMAIL -> TextButton(enabled = !busy, onClick = ::requestCode) {
                    Text(if (busy) "Sending…" else "Send code", style = MaterialTheme.typography.labelLarge, color = RehabPrimary)
                }
                ResetStep.CODE -> TextButton(
                    enabled = !busy,
                    onClick = {
                        if (newPassword != confirmPassword) {
                            problem = "Those new passwords don't match. Please try again."
                        } else {
                            busy = true
                            onResetPassword(email, code, newPassword) { result ->
                                busy = false
                                if (result == null) step = ResetStep.DONE else problem = result
                            }
                        }
                    }
                ) {
                    Text(if (busy) "Saving…" else "Reset password", style = MaterialTheme.typography.labelLarge, color = RehabPrimary)
                }
                ResetStep.DONE -> TextButton(onClick = { onFinished(email.trim()) }) {
                    Text("Sign in", style = MaterialTheme.typography.labelLarge, color = RehabPrimary)
                }
            }
        },
        dismissButton = {
            if (step != ResetStep.DONE) {
                TextButton(enabled = !busy, onClick = onDismiss) {
                    Text("Cancel", style = MaterialTheme.typography.labelLarge, color = RehabTextSecondary)
                }
            }
        }
    )
}
