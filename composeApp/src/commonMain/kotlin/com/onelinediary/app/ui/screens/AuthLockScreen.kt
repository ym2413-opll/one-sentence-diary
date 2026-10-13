package com.onelinediary.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onelinediary.app.ui.theme.SpaceCardBackground
import com.onelinediary.app.ui.theme.SpaceDeepBlue
import com.onelinediary.app.ui.theme.SpaceSurfaceTranslucent
import com.onelinediary.app.ui.theme.StarDim
import com.onelinediary.app.ui.theme.StarGold
import com.onelinediary.app.ui.theme.StarlightBlue
import com.onelinediary.app.ui.theme.StarWhite

@Composable
fun AuthLockScreen(
    isFirstTimeSetup: Boolean,
    onSetupPassword: (password: String) -> Boolean,
    onUnlock: (password: String) -> Boolean,
    modifier: Modifier = Modifier
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceDeepBlue)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .border(
                    width = 1.dp,
                    color = StarlightBlue.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SpaceSurfaceTranslucent)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isFirstTimeSetup) "🔐 암호 키 설정" else "🔒 한줄일기 잠금 해제",
                    color = StarWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isFirstTimeSetup)
                        "작성한 일기는 이 암호 키로 강력하게 암호화되어 안전하게 보관됩니다. (4자 이상)"
                    else
                        "안전한 일기 열람을 위해 설정하신 암호 키를 입력하세요.",
                    color = StarDim,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 비밀번호 입력
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("암호 키 (비밀번호)", color = StarDim, fontSize = 13.sp) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StarlightBlue,
                        unfocusedBorderColor = StarlightBlue.copy(alpha = 0.3f),
                        focusedTextColor = StarWhite,
                        unfocusedTextColor = StarWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        imeAction = if (isFirstTimeSetup) ImeAction.Next else ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (!isFirstTimeSetup) {
                                val success = onUnlock(password)
                                if (!success) {
                                    errorMessage = "암호 키가 일치하지 않습니다."
                                }
                            }
                        }
                    )
                )

                // 최초 설정 시 비밀번호 확인 입력창
                if (isFirstTimeSetup) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("암호 키 확인", color = StarDim, fontSize = 13.sp) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StarlightBlue,
                            unfocusedBorderColor = StarlightBlue.copy(alpha = 0.3f),
                            focusedTextColor = StarWhite,
                            unfocusedTextColor = StarWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (password != confirmPassword) {
                                    errorMessage = "암호 키가 서로 일치하지 않습니다."
                                } else if (password.length < 4) {
                                    errorMessage = "암호 키는 4자리 이상이어야 합니다."
                                } else {
                                    onSetupPassword(password)
                                }
                            }
                        )
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFFF7B7B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (isFirstTimeSetup) {
                            if (password != confirmPassword) {
                                errorMessage = "암호 키가 서로 일치하지 않습니다."
                            } else if (password.length < 4) {
                                errorMessage = "암호 키는 4자리 이상이어야 합니다."
                            } else {
                                onSetupPassword(password)
                            }
                        } else {
                            val success = onUnlock(password)
                            if (!success) {
                                errorMessage = "암호 키가 일치하지 않습니다."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = StarlightBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isFirstTimeSetup) "보안 일기장 시작하기" else "잠금 해제",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
