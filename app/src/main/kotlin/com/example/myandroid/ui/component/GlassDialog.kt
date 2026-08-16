package com.example.myandroid.ui.component

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myandroid.ui.theme.*

/**
 * VisionOS-style glass dialog — replaces Material AlertDialog.
 *
 * Features:
 * - Full screen dimmed scrim
 * - Centered glass card with large rounded corners (36dp)
 * - Spring scale-in animation
 * - No Material Dialog component used
 */
@Composable
fun GlassDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String? = null,
    message: String? = null,
    confirmText: String = "确定",
    cancelText: String? = "取消",
    onConfirm: () -> Unit,
    onCancel: (() -> Unit)? = null,
    confirmDestructive: Boolean = false,
    content: @Composable (() -> Unit)? = null
) {
    if (!visible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            // The dialog card
            Column(
                modifier = Modifier
                    .padding(horizontal = 40.dp)
                    .clip(DialogShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.9f),
                                Color.White.copy(alpha = 0.7f)
                            )
                        ),
                        DialogShape
                    )
                    .border(0.5.dp, Color.White.copy(alpha = 0.5f), DialogShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // absorb clicks inside dialog
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (title != null) {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                    Spacer(Modifier.height(8.dp))
                }

                if (message != null) {
                    Text(
                        text = message,
                        color = TextSecondary,
                        fontSize = 15.sp,
                        letterSpacing = (-0.2).sp
                    )
                }

                if (content != null) {
                    content()
                }

                Spacer(Modifier.height(20.dp))

                // Buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (cancelText != null) {
                        GlassTextButton(
                            text = cancelText,
                            onClick = {
                                onCancel?.invoke()
                                onDismiss()
                            },
                            textColor = TextPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                    }

                    GlassTextButton(
                        text = confirmText,
                        onClick = {
                            onConfirm()
                            onDismiss()
                        },
                        textColor = if (confirmDestructive) ErrorRed else Purple500,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Glass bottom sheet / input dialog for category add/edit.
 */
@Composable
fun GlassInputDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String,
    inputValue: String,
    onInputChange: (String) -> Unit,
    inputPlaceholder: String = "",
    confirmText: String = "确定",
    cancelText: String = "取消",
    onConfirm: () -> Unit,
    inputError: String? = null
) {
    if (!visible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .clip(DialogShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.9f),
                                Color.White.copy(alpha = 0.7f)
                            )
                        ),
                        DialogShape
                    )
                    .border(0.5.dp, Color.White.copy(alpha = 0.5f), DialogShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .padding(24.dp)
            ) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                )

                Spacer(Modifier.height(16.dp))

                GlassTextField(
                    value = inputValue,
                    onValueChange = onInputChange,
                    placeholder = inputPlaceholder,
                    isError = inputError != null,
                    modifier = Modifier.fillMaxWidth()
                )

                if (inputError != null) {
                    Text(
                        text = inputError,
                        color = ErrorRed,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    GlassTextButton(
                        text = cancelText,
                        onClick = onDismiss,
                        textColor = TextPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    GlassTextButton(
                        text = confirmText,
                        onClick = onConfirm,
                        textColor = Purple500,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
