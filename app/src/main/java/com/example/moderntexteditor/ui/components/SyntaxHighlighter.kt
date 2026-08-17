package com.example.moderntexteditor.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class KotlinSyntaxTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            highlightKotlin(text.text),
            OffsetMapping.Identity
        )
    }
}

fun highlightKotlin(code: String): AnnotatedString {
    val keywords = setOf(
        "package", "import", "class", "interface", "fun", "val", "var", 
        "if", "else", "when", "for", "while", "return", "null", "true", "false",
        "private", "public", "protected", "internal", "override", "suspend", "object"
    )

    return buildAnnotatedString {
        append(code)
        
        // Match Keywords
        val keywordRegex = Regex("\\b(" + keywords.joinToString("|") + ")\\b")
        keywordRegex.findAll(code).forEach { match ->
            addStyle(
                style = SpanStyle(color = Color(0xFF7F0055), fontWeight = FontWeight.Bold),
                start = match.range.first,
                end = match.range.last + 1
            )
        }

        // Match Annotations (@Annotation)
        val annotationRegex = Regex("@[a-zA-Z0-9_]+")
        annotationRegex.findAll(code).forEach { match ->
            addStyle(
                style = SpanStyle(color = Color(0xFF9E7E00)),
                start = match.range.first,
                end = match.range.last + 1
            )
        }

        // Match Strings
        val stringRegex = Regex("\".*?\"")
        stringRegex.findAll(code).forEach { match ->
            addStyle(
                style = SpanStyle(color = Color(0xFF2A9210)),
                start = match.range.first,
                end = match.range.last + 1
            )
        }

        // Match Markdown Headers (# Header)
        val markdownHeaderRegex = Regex("^#+\\s+.*", RegexOption.MULTILINE)
        markdownHeaderRegex.findAll(code).forEach { match ->
            addStyle(
                style = SpanStyle(color = Color(0xFF0033B3), fontWeight = FontWeight.ExtraBold),
                start = match.range.first,
                end = match.range.last + 1
            )
        }

        // Match Comments
        val commentRegex = Regex("//.*|/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL)
        commentRegex.findAll(code).forEach { match ->
            addStyle(
                style = SpanStyle(color = Color(0xFF3F7F5F)),
                start = match.range.first,
                end = match.range.last + 1
            )
        }
    }
}
