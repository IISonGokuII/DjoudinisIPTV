package com.djoudini.iptv.util

import com.djoudini.iptv.data.local.CategoryEntity
import com.djoudini.iptv.data.local.ChannelEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object M3uParser {

    suspend fun parse(inputStream: InputStream, providerId: Long): Pair<List<CategoryEntity>, List<ChannelEntity>> = withContext(Dispatchers.IO) {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val categories = mutableMapOf<String, CategoryEntity>()
        val channels = mutableListOf<ChannelEntity>()
        
        var currentLine: String?
        var currentExtInf: M3uExtInf? = null

        while (reader.readLine().also { currentLine = it } != null) {
            val line = currentLine!!.trim()
            if (line.isEmpty()) continue

            if (line.startsWith("#EXTINF:")) {
                currentExtInf = parseExtInf(line)
            } else if (!line.startsWith("#") && currentExtInf != null) {
                val groupName = currentExtInf.groupTitle ?: "Uncategorized"
                val categoryId = groupName.lowercase().replace(" ", "_")
                
                if (!categories.containsKey(categoryId)) {
                    categories[categoryId] = CategoryEntity(
                        id = categoryId,
                        name = groupName,
                        type = "LIVE",
                        providerId = providerId
                    )
                }

                channels.add(
                    ChannelEntity(
                        streamId = (channels.size + 1).toString(),
                        name = currentExtInf.tvgName ?: currentExtInf.displayName ?: "Unknown",
                        logoUrl = currentExtInf.tvgLogo,
                        streamUrl = line,
                        categoryId = categoryId,
                        providerId = providerId,
                        isFavorite = false,
                        tvgId = currentExtInf.tvgId
                    )
                )
                currentExtInf = null
            }
        }
        
        return@withContext Pair(categories.values.toList(), channels)
    }

    private fun parseExtInf(line: String): M3uExtInf {
        val tvgId = extractAttribute(line, "tvg-id")
        val tvgName = extractAttribute(line, "tvg-name")
        val tvgLogo = extractAttribute(line, "tvg-logo")
        val groupTitle = extractAttribute(line, "group-title")
        val displayName = line.substringAfterLast(",").trim()
        
        return M3uExtInf(tvgId, tvgName, tvgLogo, groupTitle, displayName)
    }

    private fun extractAttribute(line: String, attribute: String): String? {
        val regex = Regex("$attribute=\"([^\"]*)\"")
        return regex.find(line)?.groupValues?.get(1)
    }

    private data class M3uExtInf(
        val tvgId: String?,
        val tvgName: String?,
        val tvgLogo: String?,
        val groupTitle: String?,
        val displayName: String?
    )
}
