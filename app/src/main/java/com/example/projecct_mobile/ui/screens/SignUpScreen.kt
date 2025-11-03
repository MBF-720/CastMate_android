package com.example.projecct_mobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.ui.theme.*

@Composable
fun SignUpScreen(onSignUpClick: () -> Unit = {}, onLoginClick: () -> Unit = {}) {
    var email by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        // Section bleue décorative
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(DarkBlue)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            
            // Onde décorative
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .background(DarkBlue)
            )
        }

        // Contenu blanc principal
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 80.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                .clip(RoundedCornerShape(15.dp)),
            shape = RoundedCornerShape(15.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            // Bordure pointillée
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = LightBlue,
                        shape = RoundedCornerShape(15.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Titre
                    Text(
                        text = "Sign Up",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Champ Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Email", color = LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LightBlue,
                            unfocusedBorderColor = LightBlue,
                            focusedContainerColor = LightGray,
                            unfocusedContainerColor = LightGray
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Champs First Name et Last Name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            placeholder = { Text("First Name", color = LightGray) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LightBlue,
                                unfocusedBorderColor = LightBlue,
                                focusedContainerColor = LightGray,
                                unfocusedContainerColor = LightGray
                            )
                        )
                        
                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            placeholder = { Text("Last Name", color = LightGray) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LightBlue,
                                unfocusedBorderColor = LightBlue,
                                focusedContainerColor = LightGray,
                                unfocusedContainerColor = LightGray
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Champ Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Password", color = LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LightBlue,
                            unfocusedBorderColor = LightBlue,
                            focusedContainerColor = LightGray,
                            unfocusedContainerColor = LightGray
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Champ Confirm Password
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = { Text("Confirm Password", color = LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LightBlue,
                            unfocusedBorderColor = LightBlue,
                            focusedContainerColor = LightGray,
                            unfocusedContainerColor = LightGray
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Ligne pointillée
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(LightBlue)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Bouton Create Account
                    Button(
                        onClick = onSignUpClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
                    ) {
                        Text("create Account", fontSize = 16.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Ligne pointillée
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(LightBlue)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Lien "Already Have an Account, Login"
                    Text(
                        text = buildAnnotatedString {
                            append("Already Have an Account, ")
                            withStyle(style = SpanStyle(
                                textDecoration = TextDecoration.Underline,
                                color = AccentBlue
                            )) {
                                append("Login")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLoginClick() },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textAlign = TextAlign.Center
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    Projecct_MobileTheme {
        SignUpScreen()
    }
}

