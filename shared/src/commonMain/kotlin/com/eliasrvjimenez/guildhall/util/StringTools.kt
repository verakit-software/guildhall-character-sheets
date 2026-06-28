package com.eliasrvjimenez.guildhall.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

@Composable
fun BuildBoldStringWithSubtext(subject: String, subtext: String) {
    val styledText = buildAnnotatedString {
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append("$subject: ")
        }
        append("$subtext.")
    }

    Text(
        text = styledText,
        style = MaterialTheme.typography.bodyLarge
    )
}