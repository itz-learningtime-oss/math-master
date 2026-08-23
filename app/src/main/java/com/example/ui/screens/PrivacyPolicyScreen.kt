package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("screen_privacy_policy"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("btn_privacy_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Slate700
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Privacy Policy",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Slate900
                    )
                    Text(
                        text = "Math Master • Last updated: August 24, 2026",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }
            }
        }

        // Developer Attribution Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PrimaryIndigo),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Developed by Vishesh Chaturvedi",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Version 3.14 • 100% Free & Local Math App",
                            fontSize = 12.sp,
                            color = AccentCyan
                        )
                    }
                }
            }
        }

        // Section 1: About the App & Developer
        item {
            PolicySectionCard(
                sectionNumber = "1",
                title = "About the App & Developer",
                icon = Icons.Default.AccountCircle,
                iconTint = PrimaryIndigo
            ) {
                Text(
                    text = "Math Master is an educational application developed by Vishesh Chaturvedi. The app is specifically designed to help students master mental math calculation speed and accuracy for competitive exams and academic success.",
                    fontSize = 13.sp,
                    color = Slate700,
                    lineHeight = 20.sp
                )
            }
        }

        // Section 2: Free Usage Commitment
        item {
            PolicySectionCard(
                sectionNumber = "2",
                title = "Free Usage Commitment",
                icon = Icons.Default.Paid,
                iconTint = AccentEmerald
            ) {
                Text(
                    text = "Math Master is 100% completely free forever. There are no subscription fees, hidden charges, or in-app purchases required to access any features.",
                    fontSize = 13.sp,
                    color = Slate700,
                    lineHeight = 20.sp
                )
            }
        }

        // Section 3: Information Collection & Storage
        item {
            PolicySectionCard(
                sectionNumber = "3",
                title = "Information Collection & Storage",
                icon = Icons.Default.Storage,
                iconTint = PrimaryIndigo
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "We strongly value your privacy. Math Master operates purely as a local practice utility:",
                        fontSize = 13.sp,
                        color = Slate700,
                        lineHeight = 19.sp
                    )

                    BulletPoint(
                        heading = "No Server Data Transfer",
                        detail = "The app does not collect, track, or share any personal data with the developer or any third parties."
                    )
                    BulletPoint(
                        heading = "Local Storage Only",
                        detail = "All progress, score history, and user settings are stored exclusively on your device's local memory and are never transmitted externally."
                    )
                    BulletPoint(
                        heading = "No Account Required",
                        detail = "The app does not require logins, accounts, or registration."
                    )
                }
            }
        }

        // Section 4: Permissions
        item {
            PolicySectionCard(
                sectionNumber = "4",
                title = "Permissions",
                icon = Icons.Default.VerifiedUser,
                iconTint = AccentCyan
            ) {
                Text(
                    text = "Math Master requires no special or sensitive permissions (such as location, camera, or contacts) to run.",
                    fontSize = 13.sp,
                    color = Slate700,
                    lineHeight = 20.sp
                )
            }
        }

        // Section 5: Suggestions, Feedback, and Support
        item {
            PolicySectionCard(
                sectionNumber = "5",
                title = "Suggestions, Feedback, and Support",
                icon = Icons.Default.Email,
                iconTint = AccentAmber
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "We welcome your feedback to help us continuously improve the app. If you have suggestions, reviews, or feature requests, feel free to contact the developer directly:",
                        fontSize = 13.sp,
                        color = Slate700,
                        lineHeight = 19.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEFF6FF),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = PrimaryIndigo,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "www.itzlearningtime@gmail.com",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryIndigo
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:www.itzlearningtime@gmail.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "Math Master - Feedback & Suggestions")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Contact: www.itzlearningtime@gmail.com", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_email_developer")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Email Developer",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Enjoy the app, keep learning, and keep exploring!",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryIndigo,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PolicySectionCard(
    sectionNumber: String,
    title: String,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "$sectionNumber. $title",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Slate900
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun BulletPoint(
    heading: String,
    detail: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(PrimaryIndigo)
        )
        Column {
            Text(
                text = heading,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Text(
                text = detail,
                fontSize = 12.sp,
                color = Slate600,
                lineHeight = 17.sp
            )
        }
    }
}
