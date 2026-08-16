package com.example.myandroid.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myandroid.ui.theme.*

/**
 * VisionOS-style glass text field — no Material TextField used.
 *
 * Features:
 * - Semi-transparent glass background
 * - Fine white border, highlights to primary on focus
 * - Optional leading icon
 * - Optional trailing icon (e.g., visibility toggle)
 * - Smooth animated border color transition
 * - Large rounded corners (16dp)
 */
@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    passwordVisible: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 3,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    height: androidx.compose.ui.unit.Dp = 52.dp,
    backgroundColor: Color = Color.White.copy(alpha = 0.35f),
    focusedBorderColor: Color = Purple500,
    unfocusedBorderColor: Color = Color.White.copy(alpha = 0.3f),
    errorBorderColor: Color = ErrorRed
) {
    var isFocused by remember { mutableStateOf(false) }

    val borderColor = when {
        isError -> errorBorderColor
        isFocused -> focusedBorderColor
        else -> unfocusedBorderColor
    }

    val visualTransformation = when {
        !passwordVisible && keyboardType == KeyboardType.Password ->
            PasswordVisualTransformation()
        else -> VisualTransformation.None
    }

    val inputKeyboardType = when {
        !passwordVisible && keyboardType == KeyboardType.Password ->
            KeyboardType.Password
        else -> keyboardType
    }

    Row(
        modifier = modifier
            .height(height)
            .clip(TextFieldShape)
            .background(backgroundColor, TextFieldShape)
            .border(0.5.dp, borderColor, TextFieldShape)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            maxLines = maxLines,
            textStyle = TextStyle(
                color = TextPrimary,
                fontSize = 16.sp,
                letterSpacing = (-0.2).sp
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = inputKeyboardType,
                imeAction = imeAction
            ),
            visualTransformation = visualTransformation,
            cursorBrush = SolidColor(Purple500),
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { isFocused = it.isFocused },
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = TextSecondary,
                            fontSize = 16.sp,
                            letterSpacing = (-0.2).sp
                        )
                    }
                    innerTextField()
                }
            }
        )

        if (trailingIcon != null) {
            Spacer(Modifier.width(8.dp))
            trailingIcon()
        }
    }
}

/**
 * Multiline glass text field for notes/descriptions.
 */
@Composable
fun GlassTextFieldMultiline(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    minLines: Int = 3,
    maxLines: Int = 5,
    enabled: Boolean = true,
    isError: Boolean = false,
    backgroundColor: Color = Color.White.copy(alpha = 0.35f),
    focusedBorderColor: Color = Purple500,
    unfocusedBorderColor: Color = Color.White.copy(alpha = 0.3f),
    errorBorderColor: Color = ErrorRed
) {
    var isFocused by remember { mutableStateOf(false) }

    val borderColor = when {
        isError -> errorBorderColor
        isFocused -> focusedBorderColor
        else -> unfocusedBorderColor
    }

    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 52.dp)
            .clip(TextFieldShape)
            .background(backgroundColor, TextFieldShape)
            .border(0.5.dp, borderColor, TextFieldShape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = false,
            minLines = minLines,
            maxLines = maxLines,
            textStyle = TextStyle(
                color = TextPrimary,
                fontSize = 16.sp,
                letterSpacing = (-0.2).sp
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Default
            ),
            cursorBrush = SolidColor(Purple500),
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { isFocused = it.isFocused },
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = TextSecondary,
                            fontSize = 16.sp,
                            letterSpacing = (-0.2).sp
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}
