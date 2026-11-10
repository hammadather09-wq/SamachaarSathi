package com.example.samachaarsathi.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Service for local image validation and on-device text recognition.
 */
interface ImageOcrService {
    fun validateImage(context: Context, uri: Uri): ImageValidationResult
    suspend fun processImageUri(context: Context, uri: Uri): Result<OcrExtractionResult>
    suspend fun processBitmap(bitmap: Bitmap, uri: Uri? = null): Result<OcrExtractionResult>
}

/**
 * ML Kit implementation of on-device OCR.
 * Runs 100% locally on the device with zero network calls and zero quota consumption.
 */
class MlKitImageOcrService : ImageOcrService {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    override fun validateImage(context: Context, uri: Uri): ImageValidationResult {
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri)

            val allowedTypes = setOf("image/jpeg", "image/png", "image/webp")
            if (mimeType != null && mimeType !in allowedTypes) {
                return ImageValidationResult(
                    isValid = false,
                    mimeType = mimeType,
                    errorMessage = "Unsupported image format: $mimeType. Please upload JPEG, PNG, or WebP.",
                )
            }

            var sizeBytes = 0L
            contentResolver.openAssetFileDescriptor(uri, "r")?.use { afd ->
                sizeBytes = afd.length
            }

            val maxSizeBytes = 15L * 1024L * 1024L // 15MB limit
            if (sizeBytes > maxSizeBytes) {
                return ImageValidationResult(
                    isValid = false,
                    fileSizeBytes = sizeBytes,
                    mimeType = mimeType,
                    errorMessage = "Image file is too large (${sizeBytes / (1024 * 1024)}MB). Maximum allowed size is 15MB.",
                )
            }

            // Decode bounds to verify image is not corrupted
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            if (options.outWidth <= 0 || options.outHeight <= 0) {
                return ImageValidationResult(
                    isValid = false,
                    errorMessage = "Unable to read image dimensions. The file may be corrupted.",
                )
            }

            ImageValidationResult(
                isValid = true,
                mimeType = mimeType,
                fileSizeBytes = sizeBytes,
                width = options.outWidth,
                height = options.outHeight,
            )
        } catch (e: Exception) {
            ImageValidationResult(
                isValid = false,
                errorMessage = "Validation error: ${e.localizedMessage ?: "Unknown file error"}",
            )
        }
    }

    override suspend fun processImageUri(context: Context, uri: Uri): Result<OcrExtractionResult> =
        withContext(Dispatchers.IO) {
            try {
                val validation = validateImage(context, uri)
                if (!validation.isValid) {
                    return@withContext Result.failure(
                        IllegalArgumentException(validation.errorMessage ?: "Invalid image"),
                    )
                }

                val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                } ?: return@withContext Result.failure(IllegalStateException("Could not decode image from URI"))

                processBitmap(bitmap, uri)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun processBitmap(
        bitmap: Bitmap,
        uri: Uri?,
    ): Result<OcrExtractionResult> = withContext(Dispatchers.Default) {
        suspendCancellableCoroutine { continuation ->
            try {
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val lines = mutableListOf<String>()
                        val regions = mutableListOf<TextRegion>()

                        var lineIdx = 0
                        for (block in visionText.textBlocks) {
                            for (line in block.lines) {
                                val text = line.text.trim()
                                if (text.isNotBlank()) {
                                    lines.add(text)
                                    regions.add(TextRegion(text = text, lineIndex = lineIdx++))
                                }
                            }
                        }

                        val (cleanedClaim, forwardingMeta) = ForwardedMessageParser.parse(lines)

                        val result = OcrExtractionResult(
                            rawExtractedText = visionText.text,
                            cleanedClaimText = cleanedClaim,
                            textRegions = regions,
                            forwardingMetadata = forwardingMeta,
                            hasReadableText = lines.isNotEmpty(),
                            imageUri = uri,
                        )
                        continuation.resume(Result.success(result))
                    }
                    .addOnFailureListener { error ->
                        continuation.resume(Result.failure(error))
                    }
            } catch (e: Exception) {
                continuation.resume(Result.failure(e))
            }
        }
    }
}
