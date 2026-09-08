package ir.cutte.nava.engine

import ir.cutte.nava.model.MatchResult

object MessageMatcher {

    fun match(
        rawSender: String,
        body: String,
        whitelist: Set<String>,
        keywords: Set<String>
    ): MatchResult {
        val normalizedSender = SenderNormalizer.normalize(rawSender)
        val normalizedWhitelist = whitelist
            .map { SenderNormalizer.normalize(it) }
            .filter { it.isNotEmpty() }
            .toSet()

        val senderMatched = normalizedSender.isNotEmpty() && normalizedWhitelist.contains(normalizedSender)

        val lowerBody = body.lowercase()
        val matchingKeyword = keywords.firstOrNull { keyword ->
            val trimmed = keyword.trim()
            trimmed.isNotEmpty() && lowerBody.contains(trimmed.lowercase())
        }

        return when {
            matchingKeyword != null -> MatchResult(isMatched = true, matchedKeyword = matchingKeyword.trim())
            senderMatched -> MatchResult(isMatched = true, matchedKeyword = "WHITELIST:$normalizedSender")
            else -> MatchResult(isMatched = false, matchedKeyword = null)
        }
    }
}
