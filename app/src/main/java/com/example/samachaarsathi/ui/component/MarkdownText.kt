package com.example.samachaarsathi.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed interface MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock
    data class BulletItem(val text: String) : MarkdownBlock
    data class NumberedItem(val number: String, val text: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    data object EmptyLine : MarkdownBlock
}

private val NUMBERED_REGEX = Regex("""^(\d+)\.\s+(.*)""")
private val INLINE_REGEX = Regex("""(\*\*\*(.+?)\*\*\*)|(\*\*(.+?)\*\*)|(\*(.+?)\*)|(_(.+?)_)|(`(.+?)`)""")

fun parseMarkdownBlocks(markdown: String): List<MarkdownBlock> {
    val lines = markdown.lines()
    val blocks = mutableListOf<MarkdownBlock>()

    for (rawLine in lines) {
        val trimmed = rawLine.trim()
        if (trimmed.isEmpty()) {
            blocks.add(MarkdownBlock.EmptyLine)
            continue
        }

        when {
            trimmed.startsWith("### ") -> {
                blocks.add(MarkdownBlock.Heading(level = 3, text = trimmed.removePrefix("### ").trim()))
            }
            trimmed.startsWith("## ") -> {
                blocks.add(MarkdownBlock.Heading(level = 2, text = trimmed.removePrefix("## ").trim()))
            }
            trimmed.startsWith("# ") -> {
                blocks.add(MarkdownBlock.Heading(level = 1, text = trimmed.removePrefix("# ").trim()))
            }
            trimmed.startsWith("* ") -> {
                blocks.add(MarkdownBlock.BulletItem(text = trimmed.removePrefix("* ").trim()))
            }
            trimmed.startsWith("- ") -> {
                blocks.add(MarkdownBlock.BulletItem(text = trimmed.removePrefix("- ").trim()))
            }
            trimmed.startsWith("• ") -> {
                blocks.add(MarkdownBlock.BulletItem(text = trimmed.removePrefix("• ").trim()))
            }
            NUMBERED_REGEX.matches(trimmed) -> {
                val match = NUMBERED_REGEX.find(trimmed)
                if (match != null) {
                    val num = match.groupValues[1]
                    val itemText = match.groupValues[2]
                    blocks.add(MarkdownBlock.NumberedItem(number = num, text = itemText))
                } else {
                    blocks.add(MarkdownBlock.Paragraph(text = trimmed))
                }
            }
            else -> {
                blocks.add(MarkdownBlock.Paragraph(text = trimmed))
            }
        }
    }
    return blocks
}

fun parseInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var currentIndex = 0
        for (match in INLINE_REGEX.findAll(text)) {
            if (match.range.first > currentIndex) {
                append(text.substring(currentIndex, match.range.first))
            }
            when {
                // Group 1 & 2: ***bold italic***
                match.groups[2] != null -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {
                        append(match.groups[2]!!.value)
                    }
                }
                // Group 3 & 4: **bold**
                match.groups[4] != null -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(match.groups[4]!!.value)
                    }
                }
                // Group 5 & 6: *italic*
                match.groups[6] != null -> {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(match.groups[6]!!.value)
                    }
                }
                // Group 7 & 8: _italic_
                match.groups[8] != null -> {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(match.groups[8]!!.value)
                    }
                }
                // Group 9 & 10: `code`
                match.groups[10] != null -> {
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                        append(match.groups[10]!!.value)
                    }
                }
            }
            currentIndex = match.range.last + 1
        }
        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }
}

/**
 * Lightweight, native Compose Markdown renderer for Gemini responses.
 * Formats headings, bullet lists, numbered lists, inline bold/italic, and paragraphs
 * using Material 3 theme styling and dynamic light/dark colors.
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
) {
    val blocks = remember(markdown) { parseMarkdownBlocks(markdown) }

    Column(modifier = modifier) {
        var previousWasEmpty = false
        for (block in blocks) {
            when (block) {
                is MarkdownBlock.Heading -> {
                    previousWasEmpty = false
                    Spacer(modifier = Modifier.height(8.dp))
                    val style = when (block.level) {
                        1 -> MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        2 -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        else -> MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    }
                    val color = when (block.level) {
                        1, 2 -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                    Text(
                        text = parseInlineMarkdown(block.text),
                        style = style,
                        color = color,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                is MarkdownBlock.BulletItem -> {
                    previousWasEmpty = false
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp, end = 8.dp),
                        )
                        Text(
                            text = parseInlineMarkdown(block.text),
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                is MarkdownBlock.NumberedItem -> {
                    previousWasEmpty = false
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = "${block.number}.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .widthIn(min = 20.dp)
                                .padding(end = 6.dp),
                        )
                        Text(
                            text = parseInlineMarkdown(block.text),
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                is MarkdownBlock.Paragraph -> {
                    previousWasEmpty = false
                    Text(
                        text = parseInlineMarkdown(block.text),
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 3.dp),
                    )
                }
                is MarkdownBlock.EmptyLine -> {
                    if (!previousWasEmpty) {
                        Spacer(modifier = Modifier.height(6.dp))
                        previousWasEmpty = true
                    }
                }
            }
        }
    }
}
