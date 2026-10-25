package com.ptpws.ikikasir.commond

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Komponen GlobalTextField standar di seluruh aplikasi Iki Kasir.
 * Mengadopsi desain text field dari layar Login (Card container 0xFFF2F3F5,
 * RoundedCornerShape 15dp, elevasi 8dp, BasicTextField dengan tipografi Inter).
 */
@Composable
fun GlobalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    label: String? = null,
    labelComposable: (@Composable () -> Unit)? = null,
    isRequired: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    isPassword: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    height: Dp? = if (singleLine) 44.dp else null,
    minHeight: Dp = 44.dp,
    shape: Shape = RoundedCornerShape(15.dp),
    containerColor: Color = Color.White,
    cardElevation: Dp = 8.dp,
    border: BorderStroke? = null,
    cursorColor: Color = Color(0xFF4F46E5),
    textStyle: TextStyle = TextStyle(
        color = Color(0xFF111827),
        fontSize = 12.sp,
        fontFamily = interfamily
    )
) {
    var passwordVisible by remember { mutableStateOf(false) }

    val activeVisualTransformation = when {
        isPassword -> if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
        else -> visualTransformation
    }

    Column(modifier = modifier) {
        if (labelComposable != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                labelComposable()
            }
        } else if (!label.isNullOrBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = interfamily,
                    letterSpacing = 0.8.sp,
                    color = Color(0xFF6B7280)
                )
                if (isRequired) {
                    Text(
                        text = " *",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                }
            }
        }

        val cardModifier = Modifier
            .fillMaxWidth()
            .then(
                if (height != null) Modifier.height(height)
                else Modifier.heightIn(min = minHeight)
            )

        Card(
            modifier = cardModifier,
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = if (enabled) containerColor else Color(0xFFE5E7EB)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (enabled) cardElevation else 0.dp
            ),
            border = if (isError) BorderStroke(1.dp, Color(0xFFEF4444)) else border
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = if (leadingIcon != null) 12.dp else 16.dp,
                        end = if (trailingIcon != null || isPassword) 6.dp else 16.dp,
                        top = if (!singleLine) 10.dp else 0.dp,
                        bottom = if (!singleLine) 10.dp else 0.dp
                    ),
                verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
            ) {
                if (leadingIcon != null) {
                    Box(
                        modifier = Modifier.padding(end = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        leadingIcon()
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .then(if (singleLine) Modifier.fillMaxHeight() else Modifier),
                    contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
                ) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            style = textStyle,
                            color = Color(0x80474747),
                            maxLines = maxLines
                        )
                    }

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = singleLine,
                        maxLines = maxLines,
                        minLines = minLines,
                        readOnly = readOnly,
                        enabled = enabled,
                        textStyle = textStyle,
                        cursorBrush = SolidColor(cursorColor),
                        visualTransformation = activeVisualTransformation,
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions
                    )
                }

                if (trailingIcon != null) {
                    Box(
                        modifier = Modifier.padding(start = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        trailingIcon()
                    }
                } else if (isPassword) {
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Sembunyikan Kata Sandi" else "Tampilkan Kata Sandi",
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = isError && !errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage.orEmpty(),
                fontSize = 11.sp,
                fontFamily = interfamily,
                color = Color(0xFFEF4444),
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}
