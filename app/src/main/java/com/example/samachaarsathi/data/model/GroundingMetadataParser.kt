package com.example.samachaarsathi.data.model

import android.util.Log
import com.google.firebase.ai.type.GroundingMetadata
import org.json.JSONObject
import java.util.UUID

/**
 * Converts Firebase AI Logic's [GroundingMetadata] to [GroundingMetadataDto].
 */
fun GroundingMetadata.toDto(): GroundingMetadataDto {
    return GroundingMetadataDto(
        webSearchQueries = this.webSearchQueries,
        searchEntryPointHtml = this.searchEntryPoint?.renderedContent,
        groundingChunks = this.groundingChunks.map { chunk ->
            GroundingChunkDto(
                webUri = chunk.web?.uri,
                webTitle = chunk.web?.title,
                webDomain = chunk.web?.domain,
            )
        },
        groundingSupports = this.groundingSupports.map { support ->
            GroundingSupportDto(
                textSegment = support.segment.text,
                groundingChunkIndices = support.groundingChunkIndices,
            )
        },
    )
}

/**
 * Parser and mapper for Google Search Grounding metadata.
 *
 * Extracts clean, deduplicated [EvidenceSource] records and links them to the
 * corresponding assertion without fabricating missing titles, URLs, or dates.
 */
object GroundingMetadataParser {

    private const val TAG = "GroundingMetadataParser"

    /**
     * Parses Firebase SDK's [GroundingMetadata] for a specific assertion.
     */
    fun parse(
        metadata: GroundingMetadata?,
        assertionId: String,
        assertionText: String,
        groundedResponseText: String? = null,
    ): AssertionEvidence {
        return parseDto(
            dto = metadata?.toDto(),
            assertionId = assertionId,
            assertionText = assertionText,
            groundedResponseText = groundedResponseText,
        )
    }

    /**
     * Parses [GroundingMetadataDto] into [AssertionEvidence].
     */
    fun parseDto(
        dto: GroundingMetadataDto?,
        assertionId: String,
        assertionText: String,
        groundedResponseText: String? = null,
    ): AssertionEvidence {
        if (dto == null) {
            return AssertionEvidence(
                assertionId = assertionId,
                assertionText = assertionText,
                sources = emptyList(),
                retrievalStatus = RetrievalStatus.EMPTY_RESULTS,
                groundingMetadata = null,
                searchQueries = emptyList(),
                groundedResponseText = groundedResponseText,
            )
        }

        val chunks = dto.groundingChunks
        if (chunks.isEmpty()) {
            return AssertionEvidence(
                assertionId = assertionId,
                assertionText = assertionText,
                sources = emptyList(),
                retrievalStatus = RetrievalStatus.EMPTY_RESULTS,
                groundingMetadata = dto,
                searchQueries = dto.webSearchQueries,
                groundedResponseText = groundedResponseText,
            )
        }

        // Map chunk index to supporting snippet text from groundingSupports
        val chunkSnippetMap = mutableMapOf<Int, String>()
        for (support in dto.groundingSupports) {
            val segmentText = support.textSegment?.trim()
            if (!segmentText.isNullOrBlank()) {
                for (chunkIndex in support.groundingChunkIndices) {
                    if (chunkIndex in chunks.indices) {
                        if (!chunkSnippetMap.containsKey(chunkIndex)) {
                            chunkSnippetMap[chunkIndex] = segmentText
                        }
                    }
                }
            }
        }

        val deduplicatedSources = mutableMapOf<String, EvidenceSource>()
        chunks.forEachIndexed { index, chunk ->
            val uri = chunk.webUri?.trim()
            if (!uri.isNullOrBlank()) {
                val normalizedUrl = uri.removeSuffix("/")
                val existing = deduplicatedSources[normalizedUrl]
                val title = chunk.webTitle?.trim()?.takeIf { it.isNotBlank() }
                    ?: EvidenceSource.extractDomain(uri)
                    ?: "Web Source"
                val snippet = chunkSnippetMap[index]

                if (existing == null) {
                    deduplicatedSources[normalizedUrl] = EvidenceSource(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        url = uri,
                        domain = chunk.webDomain ?: EvidenceSource.extractDomain(uri),
                        snippet = snippet,
                        publishedDate = null, // WebGroundingChunk does not provide publication date
                        relatedAssertionIds = listOf(assertionId),
                    )
                } else {
                    // Update existing source with snippet or related assertion if new
                    val combinedSnippet = existing.snippet ?: snippet
                    val combinedAssertions = (existing.relatedAssertionIds + assertionId).distinct()
                    deduplicatedSources[normalizedUrl] = existing.copy(
                        snippet = combinedSnippet,
                        relatedAssertionIds = combinedAssertions,
                    )
                }
            }
        }

        val sourceList = deduplicatedSources.values.toList()
        val status = if (sourceList.isNotEmpty()) {
            RetrievalStatus.SUCCESS
        } else {
            RetrievalStatus.EMPTY_RESULTS
        }

        return AssertionEvidence(
            assertionId = assertionId,
            assertionText = assertionText,
            sources = sourceList,
            retrievalStatus = status,
            groundingMetadata = dto,
            searchQueries = dto.webSearchQueries,
            groundedResponseText = groundedResponseText,
        )
    }

    /**
     * Parses a raw JSON string of grounding metadata into [AssertionEvidence].
     * Useful for testing with local JSON fixtures without calling live APIs.
     */
    fun parseFromJson(
        jsonString: String,
        assertionId: String,
        assertionText: String,
        groundedResponseText: String? = null,
    ): Result<AssertionEvidence> {
        val trimmed = jsonString.trim()
        if (trimmed.isBlank()) {
            return Result.success(
                AssertionEvidence(
                    assertionId = assertionId,
                    assertionText = assertionText,
                    sources = emptyList(),
                    retrievalStatus = RetrievalStatus.EMPTY_RESULTS,
                    groundingMetadata = null,
                    searchQueries = emptyList(),
                    groundedResponseText = groundedResponseText,
                ),
            )
        }

        return try {
            val json = JSONObject(trimmed)

            // Parse search queries
            val searchQueries = mutableListOf<String>()
            val queriesArray = json.optJSONArray("webSearchQueries")
            if (queriesArray != null) {
                for (i in 0 until queriesArray.length()) {
                    val q = queriesArray.optString(i)
                    if (q.isNotBlank()) searchQueries.add(q)
                }
            }

            // Parse chunks
            val chunksList = mutableListOf<GroundingChunkDto>()
            val chunksArray = json.optJSONArray("groundingChunks")
            if (chunksArray != null) {
                for (i in 0 until chunksArray.length()) {
                    val chunkObj = chunksArray.optJSONObject(i)
                    if (chunkObj != null) {
                        val webObj = chunkObj.optJSONObject("web") ?: chunkObj
                        val uri = webObj.optString("uri").takeIf { it.isNotBlank() }
                        val title = webObj.optString("title").takeIf { it.isNotBlank() }
                        val domain = webObj.optString("domain").takeIf { it.isNotBlank() }
                        chunksList.add(GroundingChunkDto(webUri = uri, webTitle = title, webDomain = domain))
                    }
                }
            }

            // Parse supports
            val supportsList = mutableListOf<GroundingSupportDto>()
            val supportsArray = json.optJSONArray("groundingSupports")
            if (supportsArray != null) {
                for (i in 0 until supportsArray.length()) {
                    val supportObj = supportsArray.optJSONObject(i)
                    if (supportObj != null) {
                        val segObj = supportObj.optJSONObject("segment")
                        val text = segObj?.optString("text") ?: supportObj.optString("text")
                        val indices = mutableListOf<Int>()
                        val indicesArray = supportObj.optJSONArray("groundingChunkIndices")
                        if (indicesArray != null) {
                            for (j in 0 until indicesArray.length()) {
                                indices.add(indicesArray.optInt(j))
                            }
                        }
                        supportsList.add(GroundingSupportDto(textSegment = text, groundingChunkIndices = indices))
                    }
                }
            }

            val searchEntryPoint = json.optJSONObject("searchEntryPoint")?.optString("renderedContent")

            val dto = GroundingMetadataDto(
                webSearchQueries = searchQueries,
                searchEntryPointHtml = searchEntryPoint,
                groundingChunks = chunksList,
                groundingSupports = supportsList,
            )

            Result.success(
                parseDto(
                    dto = dto,
                    assertionId = assertionId,
                    assertionText = assertionText,
                    groundedResponseText = groundedResponseText,
                ),
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse grounding JSON", e)
            Result.failure(e)
        }
    }
}
