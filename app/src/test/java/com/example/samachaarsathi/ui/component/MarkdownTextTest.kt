package com.example.samachaarsathi.ui.component

import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownTextTest {

    @Test
    fun parseMarkdownBlocks_identifiesHeadingsCorrectly() {
        val markdown = """
            # Main Title
            ## Subheading
            ### Section Header
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)
        assertEquals(3, blocks.size)
        assertEquals(MarkdownBlock.Heading(level = 1, text = "Main Title"), blocks[0])
        assertEquals(MarkdownBlock.Heading(level = 2, text = "Subheading"), blocks[1])
        assertEquals(MarkdownBlock.Heading(level = 3, text = "Section Header"), blocks[2])
    }

    @Test
    fun parseMarkdownBlocks_identifiesBulletItemsCorrectly() {
        val markdown = """
            * Asterisk bullet
            - Hyphen bullet
            • Dot bullet
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)
        assertEquals(3, blocks.size)
        assertEquals(MarkdownBlock.BulletItem(text = "Asterisk bullet"), blocks[0])
        assertEquals(MarkdownBlock.BulletItem(text = "Hyphen bullet"), blocks[1])
        assertEquals(MarkdownBlock.BulletItem(text = "Dot bullet"), blocks[2])
    }

    @Test
    fun parseMarkdownBlocks_identifiesNumberedItemsCorrectly() {
        val markdown = """
            1. First step
            2. Second step
            10. Tenth step
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)
        assertEquals(3, blocks.size)
        assertEquals(MarkdownBlock.NumberedItem(number = "1", text = "First step"), blocks[0])
        assertEquals(MarkdownBlock.NumberedItem(number = "2", text = "Second step"), blocks[1])
        assertEquals(MarkdownBlock.NumberedItem(number = "10", text = "Tenth step"), blocks[2])
    }

    @Test
    fun parseMarkdownBlocks_handlesParagraphsAndEmptyLines() {
        val markdown = """
            This is paragraph one.

            This is paragraph two.
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)
        assertEquals(3, blocks.size)
        assertEquals(MarkdownBlock.Paragraph(text = "This is paragraph one."), blocks[0])
        assertEquals(MarkdownBlock.EmptyLine, blocks[1])
        assertEquals(MarkdownBlock.Paragraph(text = "This is paragraph two."), blocks[2])
    }

    @Test
    fun parseInlineMarkdown_appliesBoldStyle() {
        val input = "This is **bold** text."
        val annotated = parseInlineMarkdown(input)
        assertEquals("This is bold text.", annotated.text)

        val spanStyles = annotated.spanStyles
        assertEquals(1, spanStyles.size)
        val span = spanStyles[0]
        assertEquals(FontWeight.Bold, span.item.fontWeight)
        assertEquals(8, span.start)
        assertEquals(12, span.end)
    }

    @Test
    fun parseInlineMarkdown_appliesItalicStyle() {
        val input = "This is *italic* and _also italic_ text."
        val annotated = parseInlineMarkdown(input)
        assertEquals("This is italic and also italic text.", annotated.text)

        val spanStyles = annotated.spanStyles
        assertEquals(2, spanStyles.size)
        assertTrue(spanStyles[0].item.fontStyle == FontStyle.Italic)
        assertTrue(spanStyles[1].item.fontStyle == FontStyle.Italic)
    }

    @Test
    fun parseInlineMarkdown_appliesBoldItalicStyle() {
        val input = "Critical: ***very important*** warning."
        val annotated = parseInlineMarkdown(input)
        assertEquals("Critical: very important warning.", annotated.text)

        val spanStyles = annotated.spanStyles
        assertEquals(1, spanStyles.size)
        assertEquals(FontWeight.Bold, spanStyles[0].item.fontWeight)
        assertTrue(spanStyles[0].item.fontStyle == FontStyle.Italic)
    }

    @Test
    fun parseInlineMarkdown_handlesUnclosedFormattingGracefully() {
        val unclosed = "Streaming incomplete **bold text in progress"
        val annotated = parseInlineMarkdown(unclosed)
        assertEquals("Streaming incomplete **bold text in progress", annotated.text)
        assertTrue(annotated.spanStyles.isEmpty())
    }
}
