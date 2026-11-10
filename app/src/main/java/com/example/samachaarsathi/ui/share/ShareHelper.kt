package com.example.samachaarsathi.ui.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.samachaarsathi.data.model.ConfidenceLevel
import com.example.samachaarsathi.data.model.VerdictCategory
import com.example.samachaarsathi.data.model.VerificationVerdict
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility for formatting and executing native Android sharing for fact-checking verifications.
 * Supports both clean plain-text sharing and native branded image cards.
 */
object ShareHelper {

    private const val BRAND_NAME = "SamachaarSathi"
    private const val TAGLINE = "Independent Fact-Checking Engine"
    private const val LIMITATION_NOTE =
        "Note: This assessment reflects evidence retrieved from accredited news and official records at the time of verification. It does not replace independent legal or regulatory judgment."

    /**
     * Formats verification assessment data into clean, readable text suitable for WhatsApp, SMS, and email.
     */
    fun buildShareText(
        claim: String,
        verdict: VerificationVerdict?,
        timestampMs: Long? = null,
    ): String {
        val dateString = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.ENGLISH)
            .format(Date(timestampMs ?: verdict?.timestamp ?: System.currentTimeMillis()))

        val sb = StringBuilder()
        sb.appendLine("🔍 $BRAND_NAME Fact-Check Report")
        sb.appendLine("Verified on $dateString")
        sb.appendLine()
        sb.appendLine("📋 Claim:")
        sb.appendLine("\"${claim.trim()}\"")
        sb.appendLine()

        if (verdict != null) {
            val verdictLabel = when (verdict.category) {
                VerdictCategory.SUPPORTED -> "VERIFIED TRUE"
                VerdictCategory.CONTRADICTED -> "VERIFIED FALSE"
                VerdictCategory.MISLEADING_OR_MISSING_CONTEXT -> "MISLEADING / CONTEXT NEEDED"
                VerdictCategory.UNVERIFIED -> "UNVERIFIED"
            }

            val confidenceLabel = when (verdict.confidence.level) {
                ConfidenceLevel.HIGH -> "High Confidence"
                ConfidenceLevel.MEDIUM -> "Moderate Confidence"
                ConfidenceLevel.LOW -> "Low Confidence"
                ConfidenceLevel.UNCERTAIN -> "Uncertain / Unverified"
            }

            sb.appendLine("⚖️ Verdict: $verdictLabel")
            sb.appendLine("⭐ Confidence: $confidenceLabel")
            sb.appendLine()
            sb.appendLine("📌 Finding: ${verdict.headline}")
            sb.appendLine()
            sb.appendLine("📝 Explanation:")
            sb.appendLine(verdict.reasoning.primaryExplanation.trim())
            sb.appendLine()

            val sources = verdict.evidenceAnalysis.evidenceCollection.allSources
            if (sources.isNotEmpty()) {
                sb.appendLine("🌐 Grounded Evidence Sources:")
                sources.take(4).forEach { source ->
                    val domainText = source.domain ?: "Web Source"
                    sb.appendLine("• $domainText (${source.title}): ${source.url}")
                }
                sb.appendLine()
            }
        } else {
            sb.appendLine("⚖️ Verdict: Captured for verification")
            sb.appendLine("Assessment in progress or pending full evidence collection.")
            sb.appendLine()
        }

        sb.appendLine("⚠️ $LIMITATION_NOTE")
        sb.appendLine("Verified with $BRAND_NAME.")
        return sb.toString().trim()
    }

    /**
     * Generates a native branded Bitmap card representing the verification verdict.
     * Dimensions: 1080 x 1350 px (4:5 aspect ratio optimized for mobile messaging).
     */
    fun renderShareCardBitmap(
        claim: String,
        verdict: VerificationVerdict?,
        timestampMs: Long? = null,
    ): Bitmap {
        val width = 1080
        val height = 1350
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Background (Futuristic Deep Space #060911)
        val bgPaint = Paint().apply {
            color = Color.parseColor("#060911")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 2. Card Frame (Cyber Surface & Subtle Cyan Glow Border)
        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0D1424")
            style = Paint.Style.FILL
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#223553")
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        val cardRect = RectF(60f, 60f, (width - 60).toFloat(), (height - 60).toFloat())
        canvas.drawRoundRect(cardRect, 32f, 32f, framePaint)
        canvas.drawRoundRect(cardRect, 32f, 32f, borderPaint)

        // 3. Header Branding (Electric Cyan)
        val brandPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00E5FF")
            textSize = 38f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(BRAND_NAME, 100f, 130f, brandPaint)

        val taglinePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#B5C4DA")
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        canvas.drawText(TAGLINE, 100f, 165f, taglinePaint)

        val dateString = SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH)
            .format(Date(timestampMs ?: verdict?.timestamp ?: System.currentTimeMillis()))
        val datePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 22f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(dateString, 980f, 135f, datePaint)

        // Divider
        val dividerPaint = Paint().apply {
            color = Color.parseColor("#223553")
            strokeWidth = 2f
        }
        canvas.drawLine(100f, 195f, 980f, 195f, dividerPaint)

        var currentY = 240f

        // 4. Verdict Badge Banner (Futuristic Neon Luminescence)
        val (verdictText, verdictBgColor, verdictTextColor) = when (verdict?.category) {
            VerdictCategory.SUPPORTED -> Triple("VERIFIED TRUE", "#082618", "#00E676")
            VerdictCategory.CONTRADICTED -> Triple("VERIFIED FALSE", "#330911", "#FF1744")
            VerdictCategory.MISLEADING_OR_MISSING_CONTEXT -> Triple("MISLEADING / CONTEXT NEEDED", "#332500", "#FFD600")
            VerdictCategory.UNVERIFIED, null -> Triple("UNVERIFIED", "#092036", "#00B0FF")
        }

        val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor(verdictBgColor)
            style = Paint.Style.FILL
        }
        val badgeBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor(verdictTextColor)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        val badgeRect = RectF(100f, currentY - 35f, 520f, currentY + 30f)
        canvas.drawRoundRect(badgeRect, 16f, 16f, badgeBgPaint)
        canvas.drawRoundRect(badgeRect, 16f, 16f, badgeBorderPaint)

        val badgeTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor(verdictTextColor)
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(verdictText, 125f, currentY + 10f, badgeTextPaint)

        // Confidence badge next to verdict
        val confidenceText = when (verdict?.confidence?.level) {
            ConfidenceLevel.HIGH -> "High Confidence"
            ConfidenceLevel.MEDIUM -> "Moderate Confidence"
            ConfidenceLevel.LOW -> "Low Confidence"
            ConfidenceLevel.UNCERTAIN, null -> "Uncertain"
        }
        val confBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            style = Paint.Style.FILL
        }
        val confRect = RectF(540f, currentY - 35f, 840f, currentY + 30f)
        canvas.drawRoundRect(confRect, 16f, 16f, confBgPaint)
        val confTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        canvas.drawText(confidenceText, 560f, currentY + 8f, confTextPaint)

        currentY += 80f

        // 5. Claim Box with dynamic height
        val claimBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#152033")
            style = Paint.Style.FILL
        }

        val claimLabelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#477FD1")
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val claimTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F1F5F9")
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val claimLayout = StaticLayout.Builder.obtain(
            "\"${claim.take(180)}\"",
            0,
            "\"${claim.take(180)}\"".length,
            claimTextPaint,
            810,
        )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setMaxLines(3)
            .build()

        val claimBoxHeight = 50f + claimLayout.height + 25f
        val claimBoxRect = RectF(100f, currentY, 980f, currentY + claimBoxHeight)
        canvas.drawRoundRect(claimBoxRect, 16f, 16f, claimBoxPaint)
        canvas.drawText("ANALYZED CLAIM", 125f, currentY + 36f, claimLabelPaint)

        canvas.save()
        canvas.translate(125f, currentY + 52f)
        claimLayout.draw(canvas)
        canvas.restore()

        currentY += claimBoxHeight + 35f

        // 6. Headline / Finding
        if (verdict != null) {
            val headlinePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFFFFF")
                textSize = 34f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val headlineLayout = StaticLayout.Builder.obtain(
                verdict.headline,
                0,
                verdict.headline.length,
                headlinePaint,
                880,
            )
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setMaxLines(3)
                .build()

            canvas.save()
            canvas.translate(100f, currentY)
            headlineLayout.draw(canvas)
            canvas.restore()

            currentY += headlineLayout.height + 25f

            // 7. Dynamic Explanation lines bounded by available card height
            val sources = verdict.evidenceAnalysis.evidenceCollection.allSources
            val spaceReservedForSources = if (sources.isNotEmpty()) 80f else 0f
            val maxAvailableForExplanation = 1170f - currentY - spaceReservedForSources
            val maxLinesForExplanation = (maxAvailableForExplanation / 34f).toInt().coerceIn(2, 6)

            val explanationPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#CBD5E1")
                textSize = 26f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            val explanationLayout = StaticLayout.Builder.obtain(
                verdict.reasoning.primaryExplanation,
                0,
                verdict.reasoning.primaryExplanation.length,
                explanationPaint,
                880,
            )
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setMaxLines(maxLinesForExplanation)
                .build()

            canvas.save()
            canvas.translate(100f, currentY)
            explanationLayout.draw(canvas)
            canvas.restore()

            currentY += explanationLayout.height + 30f

            // 8. Sources summary
            if (sources.isNotEmpty() && currentY <= 1140f) {
                val sourcesLabelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#477FD1")
                    textSize = 22f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText("CORROBORATING SOURCES", 100f, currentY, sourcesLabelPaint)
                currentY += 28f

                val sourceList = sources.take(3).joinToString("  •  ") { it.domain ?: "Verified Source" }
                val sourceTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#94A3B8")
                    textSize = 22f
                }
                canvas.drawText(sourceList, 100f, currentY, sourceTextPaint)
            }
        }

        // 9. Disclaimer at footer
        val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 20f
        }
        val footerLayout = StaticLayout.Builder.obtain(
            LIMITATION_NOTE,
            0,
            LIMITATION_NOTE.length,
            footerPaint,
            880,
        )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setMaxLines(3)
            .build()

        canvas.save()
        canvas.translate(100f, 1200f)
        footerLayout.draw(canvas)
        canvas.restore()

        return bitmap
    }

    /**
     * Executes native Android chooser share for verification text.
     */
    fun shareText(
        context: Context,
        claim: String,
        verdict: VerificationVerdict?,
        timestampMs: Long? = null,
    ) {
        val shareBody = buildShareText(claim = claim, verdict = verdict, timestampMs = timestampMs)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "SamachaarSathi Fact-Check: ${verdict?.headline ?: claim.take(40)}")
            putExtra(Intent.EXTRA_TEXT, shareBody)
        }
        val chooser = Intent.createChooser(shareIntent, "Share Fact-Check Report via")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Generates a branded image card, stores it in the local cache, and triggers native chooser.
     */
    fun shareImageCard(
        context: Context,
        claim: String,
        verdict: VerificationVerdict?,
        timestampMs: Long? = null,
    ) {
        val bitmap = renderShareCardBitmap(claim = claim, verdict = verdict, timestampMs = timestampMs)
        val imageUri = saveBitmapToCache(context, bitmap)
        val shareBody = buildShareText(claim = claim, verdict = verdict, timestampMs = timestampMs)

        if (imageUri != null) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                putExtra(Intent.EXTRA_TEXT, shareBody)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Share Verification Card via")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } else {
            // Fallback to text sharing if file writing is not available
            shareText(context, claim, verdict, timestampMs)
        }
    }

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val imagesFolder = File(context.cacheDir, "images").apply { mkdirs() }
            val file = File(imagesFolder, "samachaarsathi_check_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            null
        }
    }
}
