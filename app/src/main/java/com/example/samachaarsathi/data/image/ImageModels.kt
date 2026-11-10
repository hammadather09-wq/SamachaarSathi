package com.example.samachaarsathi.data.image

import android.net.Uri

/**
 * Metadata validating the user-selected image.
 */
data class ImageValidationResult(
    val isValid: Boolean,
    val mimeType: String? = null,
    val fileSizeBytes: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val errorMessage: String? = null,
)

/**
 * Represents a discrete bounding region of detected text within an image.
 */
data class TextRegion(
    val text: String,
    val lineIndex: Int,
)

/**
 * Metadata detected from WhatsApp or messaging screenshot formatting.
 */
data class ForwardingMetadata(
    val isForwarded: Boolean,
    val headerMarker: String? = null,
    val timestampDetected: String? = null,
    val containsViralCallToAction: Boolean = false,
    val multipleClaimsDetected: Boolean = false,
)

/**
 * Result of on-device OCR and forwarded message structure analysis.
 *
 * @property rawExtractedText Full text as read by on-device OCR.
 * @property cleanedClaimText Curated claim text with forwarding headers stripped.
 * @property textRegions Discrete lines or blocks detected.
 * @property forwardingMetadata Detected forwarding markers (if screenshot).
 * @property hasReadableText Whether OCR found any legible text.
 * @property imageUri Local URI of the source image.
 */
data class OcrExtractionResult(
    val rawExtractedText: String,
    val cleanedClaimText: String,
    val textRegions: List<TextRegion> = emptyList(),
    val forwardingMetadata: ForwardingMetadata? = null,
    val hasReadableText: Boolean = rawExtractedText.isNotBlank(),
    val imageUri: Uri? = null,
)
