package com.example.samachaarsathi.data.local

import com.example.samachaarsathi.data.model.AssertionAnalysis
import com.example.samachaarsathi.data.model.AssertionEvidence
import com.example.samachaarsathi.data.model.ClaimUnderstanding
import com.example.samachaarsathi.data.model.ConfidenceAssessment
import com.example.samachaarsathi.data.model.ConfidenceLevel
import com.example.samachaarsathi.data.model.EvidenceAnalysis
import com.example.samachaarsathi.data.model.EvidenceCollection
import com.example.samachaarsathi.data.model.EvidenceConflict
import com.example.samachaarsathi.data.model.EvidenceGap
import com.example.samachaarsathi.data.model.EvidenceSource
import com.example.samachaarsathi.data.model.EvidenceStance
import com.example.samachaarsathi.data.model.GroundingChunkDto
import com.example.samachaarsathi.data.model.GroundingMetadataDto
import com.example.samachaarsathi.data.model.GroundingSupportDto
import com.example.samachaarsathi.data.model.RetrievalStatus
import com.example.samachaarsathi.data.model.SourceAssessment
import com.example.samachaarsathi.data.model.SourceReliabilityTier
import com.example.samachaarsathi.data.model.SourceType
import com.example.samachaarsathi.data.model.VerdictCategory
import com.example.samachaarsathi.data.model.VerdictReasoning
import com.example.samachaarsathi.data.model.VerificationLimitation
import com.example.samachaarsathi.data.model.VerificationVerdict
import org.json.JSONArray
import org.json.JSONObject

/**
 * Robust JSON serializer and deserializer for [VerificationVerdict].
 *
 * Uses platform-native `org.json` without external reflection dependencies,
 * ensuring high performance, zero external network requirements, and compatibility
 * across Android runtimes and JVM test environments.
 */
object VerdictJsonConverter {

    fun serialize(verdict: VerificationVerdict): String {
        val root = JSONObject()
        root.put("category", verdict.category.name)
        root.put("headline", verdict.headline)
        root.put("timestamp", verdict.timestamp)

        // Reasoning
        val reasoningObj = JSONObject()
        reasoningObj.put("primaryExplanation", verdict.reasoning.primaryExplanation)
        reasoningObj.put("supportedAssertions", JSONArray(verdict.reasoning.supportedAssertions))
        reasoningObj.put("contradictedAssertions", JSONArray(verdict.reasoning.contradictedAssertions))
        reasoningObj.put("missingContextNotes", JSONArray(verdict.reasoning.missingContextNotes))
        reasoningObj.put("keyEvidencePoints", JSONArray(verdict.reasoning.keyEvidencePoints))
        root.put("reasoning", reasoningObj)

        // Confidence
        val confidenceObj = JSONObject()
        confidenceObj.put("level", verdict.confidence.level.name)
        confidenceObj.put("rationale", verdict.confidence.rationale)
        confidenceObj.put("contributingFactors", JSONArray(verdict.confidence.contributingFactors))
        confidenceObj.put("deductions", JSONArray(verdict.confidence.deductions))
        root.put("confidence", confidenceObj)

        // Limitations
        val limitationsArr = JSONArray()
        for (lim in verdict.limitations) {
            val limObj = JSONObject()
            limObj.put("description", lim.description)
            limObj.put("impact", lim.impact)
            limitationsArr.put(limObj)
        }
        root.put("limitations", limitationsArr)

        // Claim Understanding
        root.put("claimUnderstanding", serializeClaimUnderstanding(verdict.claimUnderstanding))

        // Evidence Analysis
        root.put("evidenceAnalysis", serializeEvidenceAnalysis(verdict.evidenceAnalysis))

        return root.toString()
    }

    fun deserialize(jsonStr: String): VerificationVerdict? {
        if (jsonStr.isBlank()) return null
        return try {
            val root = JSONObject(jsonStr)
            val category = try {
                VerdictCategory.valueOf(root.getString("category"))
            } catch (_: Exception) {
                VerdictCategory.UNVERIFIED
            }
            val headline = root.optString("headline", "")
            val timestamp = root.optLong("timestamp", System.currentTimeMillis())

            // Reasoning
            val reasoningObj = root.optJSONObject("reasoning") ?: JSONObject()
            val reasoning = VerdictReasoning(
                primaryExplanation = reasoningObj.optString("primaryExplanation", ""),
                supportedAssertions = reasoningObj.optJSONArray("supportedAssertions").toStringList(),
                contradictedAssertions = reasoningObj.optJSONArray("contradictedAssertions").toStringList(),
                missingContextNotes = reasoningObj.optJSONArray("missingContextNotes").toStringList(),
                keyEvidencePoints = reasoningObj.optJSONArray("keyEvidencePoints").toStringList(),
            )

            // Confidence
            val confidenceObj = root.optJSONObject("confidence") ?: JSONObject()
            val level = try {
                ConfidenceLevel.valueOf(confidenceObj.optString("level", ConfidenceLevel.UNCERTAIN.name))
            } catch (_: Exception) {
                ConfidenceLevel.UNCERTAIN
            }
            val confidence = ConfidenceAssessment(
                level = level,
                rationale = confidenceObj.optString("rationale", ""),
                contributingFactors = confidenceObj.optJSONArray("contributingFactors").toStringList(),
                deductions = confidenceObj.optJSONArray("deductions").toStringList(),
            )

            // Limitations
            val limitationsArr = root.optJSONArray("limitations")
            val limitations = mutableListOf<VerificationLimitation>()
            if (limitationsArr != null) {
                for (i in 0 until limitationsArr.length()) {
                    val limObj = limitationsArr.optJSONObject(i) ?: continue
                    limitations.add(
                        VerificationLimitation(
                            description = limObj.optString("description", ""),
                            impact = limObj.optString("impact", ""),
                        )
                    )
                }
            }

            // Claim Understanding
            val claimUnderstanding = deserializeClaimUnderstanding(root.optJSONObject("claimUnderstanding"))
                ?: ClaimUnderstanding(
                    originalClaim = headline,
                    coreClaim = headline,
                    claimType = "other",
                    keyAssertions = listOf(headline),
                )

            // Evidence Analysis
            val evidenceAnalysis = deserializeEvidenceAnalysis(
                root.optJSONObject("evidenceAnalysis"),
                claimUnderstanding,
            )

            VerificationVerdict(
                category = category,
                headline = headline,
                reasoning = reasoning,
                confidence = confidence,
                limitations = limitations,
                claimUnderstanding = claimUnderstanding,
                evidenceAnalysis = evidenceAnalysis,
                timestamp = timestamp,
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun serializeClaimUnderstanding(understanding: ClaimUnderstanding): JSONObject {
        val obj = JSONObject()
        obj.put("originalClaim", understanding.originalClaim)
        obj.put("coreClaim", understanding.coreClaim)
        obj.put("claimType", understanding.claimType)
        obj.put("entities", JSONArray(understanding.entities))
        obj.put("dateReferences", JSONArray(understanding.dateReferences))
        obj.put("locationReferences", JSONArray(understanding.locationReferences))
        obj.put("keyAssertions", JSONArray(understanding.keyAssertions))
        obj.put("evidenceRequirements", JSONArray(understanding.evidenceRequirements))
        obj.put("ambiguityNotes", JSONArray(understanding.ambiguityNotes))
        understanding.language?.let { obj.put("language", it) }
        return obj
    }

    private fun deserializeClaimUnderstanding(obj: JSONObject?): ClaimUnderstanding? {
        if (obj == null) return null
        return ClaimUnderstanding(
            originalClaim = obj.optString("originalClaim", ""),
            coreClaim = obj.optString("coreClaim", ""),
            claimType = obj.optString("claimType", "other"),
            entities = obj.optJSONArray("entities").toStringList(),
            dateReferences = obj.optJSONArray("dateReferences").toStringList(),
            locationReferences = obj.optJSONArray("locationReferences").toStringList(),
            keyAssertions = obj.optJSONArray("keyAssertions").toStringList(),
            evidenceRequirements = obj.optJSONArray("evidenceRequirements").toStringList(),
            ambiguityNotes = obj.optJSONArray("ambiguityNotes").toStringList(),
            language = obj.optString("language").takeIf { it.isNotBlank() },
        )
    }

    private fun serializeEvidenceAnalysis(analysis: EvidenceAnalysis): JSONObject {
        val root = JSONObject()
        root.put("overallStance", analysis.overallStance.name)
        root.put("totalConflicts", analysis.totalConflicts)
        root.put("totalGaps", analysis.totalGaps)
        root.put("analysisTimestamp", analysis.analysisTimestamp)

        // Source diversity map
        val diversityObj = JSONObject()
        for ((tier, count) in analysis.sourceDiversity) {
            diversityObj.put(tier.name, count)
        }
        root.put("sourceDiversity", diversityObj)

        // Evidence Collection
        root.put("evidenceCollection", serializeEvidenceCollection(analysis.evidenceCollection))

        // Assertion analyses
        val analysesArr = JSONArray()
        for (aa in analysis.assertionAnalyses) {
            val aaObj = JSONObject()
            aaObj.put("assertionId", aa.assertionId)
            aaObj.put("assertionText", aa.assertionText)
            aaObj.put("stance", aa.stance.name)
            aaObj.put("summary", aa.summary)
            aaObj.put("supportingSources", serializeSourceAssessments(aa.supportingSources))
            aaObj.put("contradictingSources", serializeSourceAssessments(aa.contradictingSources))
            aaObj.put("inconclusiveSources", serializeSourceAssessments(aa.inconclusiveSources))

            // Conflicts
            val conflictsArr = JSONArray()
            for (c in aa.conflicts) {
                val cObj = JSONObject()
                cObj.put("assertionId", c.assertionId)
                cObj.put("supportingSourceIds", JSONArray(c.supportingSourceIds))
                cObj.put("contradictingSourceIds", JSONArray(c.contradictingSourceIds))
                cObj.put("explanation", c.explanation)
                conflictsArr.put(cObj)
            }
            aaObj.put("conflicts", conflictsArr)

            // Gaps
            val gapsArr = JSONArray()
            for (g in aa.gaps) {
                val gObj = JSONObject()
                gObj.put("assertionId", g.assertionId)
                gObj.put("missingAspect", g.missingAspect)
                gObj.put("impactOnVerdict", g.impactOnVerdict)
                gapsArr.put(gObj)
            }
            aaObj.put("gaps", gapsArr)

            analysesArr.put(aaObj)
        }
        root.put("assertionAnalyses", analysesArr)

        return root
    }

    private fun deserializeEvidenceAnalysis(
        obj: JSONObject?,
        claimUnderstanding: ClaimUnderstanding,
    ): EvidenceAnalysis {
        if (obj == null) {
            return EvidenceAnalysis(
                claimUnderstanding = claimUnderstanding,
                evidenceCollection = EvidenceCollection(originalClaim = claimUnderstanding.originalClaim),
            )
        }

        val overallStance = try {
            EvidenceStance.valueOf(obj.optString("overallStance", EvidenceStance.INSUFFICIENT_EVIDENCE.name))
        } catch (_: Exception) {
            EvidenceStance.INSUFFICIENT_EVIDENCE
        }
        val totalConflicts = obj.optInt("totalConflicts", 0)
        val totalGaps = obj.optInt("totalGaps", 0)
        val analysisTimestamp = obj.optLong("analysisTimestamp", System.currentTimeMillis())

        val diversityObj = obj.optJSONObject("sourceDiversity")
        val sourceDiversity = mutableMapOf<SourceReliabilityTier, Int>()
        if (diversityObj != null) {
            val keys = diversityObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                try {
                    val tier = SourceReliabilityTier.valueOf(key)
                    sourceDiversity[tier] = diversityObj.optInt(key, 0)
                } catch (_: Exception) {}
            }
        }

        val evidenceCollection = deserializeEvidenceCollection(
            obj.optJSONObject("evidenceCollection"),
            claimUnderstanding,
        )

        val analysesArr = obj.optJSONArray("assertionAnalyses")
        val assertionAnalyses = mutableListOf<AssertionAnalysis>()
        if (analysesArr != null) {
            for (i in 0 until analysesArr.length()) {
                val aaObj = analysesArr.optJSONObject(i) ?: continue
                val stance = try {
                    EvidenceStance.valueOf(aaObj.optString("stance", EvidenceStance.INCONCLUSIVE.name))
                } catch (_: Exception) {
                    EvidenceStance.INCONCLUSIVE
                }

                val conflictsArr = aaObj.optJSONArray("conflicts")
                val conflicts = mutableListOf<EvidenceConflict>()
                if (conflictsArr != null) {
                    for (cIdx in 0 until conflictsArr.length()) {
                        val cObj = conflictsArr.optJSONObject(cIdx) ?: continue
                        conflicts.add(
                            EvidenceConflict(
                                assertionId = cObj.optString("assertionId", ""),
                                supportingSourceIds = cObj.optJSONArray("supportingSourceIds").toStringList(),
                                contradictingSourceIds = cObj.optJSONArray("contradictingSourceIds").toStringList(),
                                explanation = cObj.optString("explanation", ""),
                            )
                        )
                    }
                }

                val gapsArr = aaObj.optJSONArray("gaps")
                val gaps = mutableListOf<EvidenceGap>()
                if (gapsArr != null) {
                    for (gIdx in 0 until gapsArr.length()) {
                        val gObj = gapsArr.optJSONObject(gIdx) ?: continue
                        gaps.add(
                            EvidenceGap(
                                assertionId = gObj.optString("assertionId", ""),
                                missingAspect = gObj.optString("missingAspect", ""),
                                impactOnVerdict = gObj.optString("impactOnVerdict", ""),
                            )
                        )
                    }
                }

                assertionAnalyses.add(
                    AssertionAnalysis(
                        assertionId = aaObj.optString("assertionId", ""),
                        assertionText = aaObj.optString("assertionText", ""),
                        stance = stance,
                        supportingSources = deserializeSourceAssessments(aaObj.optJSONArray("supportingSources")),
                        contradictingSources = deserializeSourceAssessments(aaObj.optJSONArray("contradictingSources")),
                        inconclusiveSources = deserializeSourceAssessments(aaObj.optJSONArray("inconclusiveSources")),
                        conflicts = conflicts,
                        gaps = gaps,
                        summary = aaObj.optString("summary", ""),
                    )
                )
            }
        }

        return EvidenceAnalysis(
            claimUnderstanding = claimUnderstanding,
            evidenceCollection = evidenceCollection,
            assertionAnalyses = assertionAnalyses,
            overallStance = overallStance,
            sourceDiversity = sourceDiversity,
            totalConflicts = totalConflicts,
            totalGaps = totalGaps,
            analysisTimestamp = analysisTimestamp,
        )
    }

    private fun serializeEvidenceCollection(collection: EvidenceCollection): JSONObject {
        val root = JSONObject()
        root.put("originalClaim", collection.originalClaim)
        root.put("overallRetrievalStatus", collection.overallRetrievalStatus.name)
        root.put("retrievalTimestamp", collection.retrievalTimestamp)
        root.put("errorsOrWarnings", JSONArray(collection.errorsOrWarnings))

        val assertionsArr = JSONArray()
        for (ae in collection.assertionEvidence) {
            val aeObj = JSONObject()
            aeObj.put("assertionId", ae.assertionId)
            aeObj.put("assertionText", ae.assertionText)
            aeObj.put("retrievalStatus", ae.retrievalStatus.name)
            aeObj.put("searchQueries", JSONArray(ae.searchQueries))
            ae.groundedResponseText?.let { aeObj.put("groundedResponseText", it) }

            val sourcesArr = JSONArray()
            for (src in ae.sources) {
                sourcesArr.put(serializeEvidenceSource(src))
            }
            aeObj.put("sources", sourcesArr)
            assertionsArr.put(aeObj)
        }
        root.put("assertionEvidence", assertionsArr)
        return root
    }

    private fun deserializeEvidenceCollection(
        obj: JSONObject?,
        claimUnderstanding: ClaimUnderstanding,
    ): EvidenceCollection {
        if (obj == null) {
            return EvidenceCollection(originalClaim = claimUnderstanding.originalClaim)
        }

        val originalClaim = obj.optString("originalClaim", claimUnderstanding.originalClaim)
        val overallStatus = try {
            RetrievalStatus.valueOf(obj.optString("overallRetrievalStatus", RetrievalStatus.NOT_STARTED.name))
        } catch (_: Exception) {
            RetrievalStatus.NOT_STARTED
        }
        val retrievalTimestamp = obj.optLong("retrievalTimestamp", System.currentTimeMillis())
        val errorsOrWarnings = obj.optJSONArray("errorsOrWarnings").toStringList()

        val assertionsArr = obj.optJSONArray("assertionEvidence")
        val assertionEvidence = mutableListOf<AssertionEvidence>()
        if (assertionsArr != null) {
            for (i in 0 until assertionsArr.length()) {
                val aeObj = assertionsArr.optJSONObject(i) ?: continue
                val status = try {
                    RetrievalStatus.valueOf(aeObj.optString("retrievalStatus", RetrievalStatus.NOT_STARTED.name))
                } catch (_: Exception) {
                    RetrievalStatus.NOT_STARTED
                }

                val sourcesArr = aeObj.optJSONArray("sources")
                val sources = mutableListOf<EvidenceSource>()
                if (sourcesArr != null) {
                    for (sIdx in 0 until sourcesArr.length()) {
                        val sObj = sourcesArr.optJSONObject(sIdx) ?: continue
                        sources.add(deserializeEvidenceSource(sObj))
                    }
                }

                assertionEvidence.add(
                    AssertionEvidence(
                        assertionId = aeObj.optString("assertionId", ""),
                        assertionText = aeObj.optString("assertionText", ""),
                        sources = sources,
                        retrievalStatus = status,
                        searchQueries = aeObj.optJSONArray("searchQueries").toStringList(),
                        groundedResponseText = aeObj.optString("groundedResponseText").takeIf { it.isNotBlank() },
                    )
                )
            }
        }

        return EvidenceCollection(
            originalClaim = originalClaim,
            structuredClaim = claimUnderstanding,
            assertionEvidence = assertionEvidence,
            overallRetrievalStatus = overallStatus,
            retrievalTimestamp = retrievalTimestamp,
            errorsOrWarnings = errorsOrWarnings,
        )
    }

    private fun serializeEvidenceSource(source: EvidenceSource): JSONObject {
        val obj = JSONObject()
        obj.put("id", source.id)
        obj.put("title", source.title)
        obj.put("url", source.url)
        source.domain?.let { obj.put("domain", it) }
        source.snippet?.let { obj.put("snippet", it) }
        source.publishedDate?.let { obj.put("publishedDate", it) }
        obj.put("sourceType", source.sourceType.name)
        obj.put("relatedAssertionIds", JSONArray(source.relatedAssertionIds))
        obj.put("retrievalTimestamp", source.retrievalTimestamp)
        return obj
    }

    private fun deserializeEvidenceSource(obj: JSONObject): EvidenceSource {
        val sourceType = try {
            SourceType.valueOf(obj.optString("sourceType", SourceType.WEB_SEARCH.name))
        } catch (_: Exception) {
            SourceType.WEB_SEARCH
        }

        return EvidenceSource(
            id = obj.optString("id", ""),
            title = obj.optString("title", ""),
            url = obj.optString("url", ""),
            domain = obj.optString("domain").takeIf { it.isNotBlank() },
            snippet = obj.optString("snippet").takeIf { it.isNotBlank() },
            publishedDate = obj.optString("publishedDate").takeIf { it.isNotBlank() },
            sourceType = sourceType,
            relatedAssertionIds = obj.optJSONArray("relatedAssertionIds").toStringList(),
            retrievalTimestamp = obj.optLong("retrievalTimestamp", System.currentTimeMillis()),
        )
    }

    private fun serializeSourceAssessments(assessments: List<SourceAssessment>): JSONArray {
        val arr = JSONArray()
        for (sa in assessments) {
            val obj = JSONObject()
            obj.put("source", serializeEvidenceSource(sa.source))
            obj.put("reliabilityTier", sa.reliabilityTier.name)
            obj.put("relevanceScore", sa.relevanceScore)
            obj.put("stance", sa.stance.name)
            obj.put("reasoning", sa.reasoning)
            arr.put(obj)
        }
        return arr
    }

    private fun deserializeSourceAssessments(arr: JSONArray?): List<SourceAssessment> {
        if (arr == null) return emptyList()
        val list = mutableListOf<SourceAssessment>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val srcObj = obj.optJSONObject("source") ?: continue
            val source = deserializeEvidenceSource(srcObj)
            val tier = try {
                SourceReliabilityTier.valueOf(obj.optString("reliabilityTier", SourceReliabilityTier.GENERAL_WEB.name))
            } catch (_: Exception) {
                SourceReliabilityTier.GENERAL_WEB
            }
            val stance = try {
                EvidenceStance.valueOf(obj.optString("stance", EvidenceStance.INCONCLUSIVE.name))
            } catch (_: Exception) {
                EvidenceStance.INCONCLUSIVE
            }

            list.add(
                SourceAssessment(
                    source = source,
                    reliabilityTier = tier,
                    relevanceScore = obj.optString("relevanceScore", "DIRECT"),
                    stance = stance,
                    reasoning = obj.optString("reasoning", ""),
                )
            )
        }
        return list
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        val list = ArrayList<String>(length())
        for (i in 0 until length()) {
            val item = optString(i, "").trim()
            if (item.isNotEmpty()) {
                list.add(item)
            }
        }
        return list
    }
}
