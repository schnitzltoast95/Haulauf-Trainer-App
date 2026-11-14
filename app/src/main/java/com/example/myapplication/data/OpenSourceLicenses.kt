package com.example.myapplication.data

import android.content.res.AssetManager
import org.json.JSONObject

data class OpenSourceLibrary(
    val id: String, val name: String, val version: String, val url: String,
    val licenses: List<String>, val notices: List<String>, val sources: List<String>
)

data class OpenSourceCatalog(val libraries: List<OpenSourceLibrary>, val texts: Map<String, String>)

internal fun loadOpenSourceCatalog(assets: AssetManager): OpenSourceCatalog {
    val root = assets.open("oss/licenses.json").bufferedReader().use { JSONObject(it.readText()) }
    val textObject = root.getJSONObject("texts")
    val texts = textObject.keys().asSequence().associateWith { textObject.getString(it) }
    val array = root.getJSONArray("libraries")
    val libraries = (0 until array.length()).map { index ->
        val item = array.getJSONObject(index)
        fun references(key: String): List<String> {
            val refs = item.getJSONArray(key)
            return (0 until refs.length()).map { refs.getString(it) }.also { ids ->
                require(ids.all { !texts[it].isNullOrBlank() }) { "Missing license text" }
            }
        }
        OpenSourceLibrary(item.getString("id"), item.getString("name"), item.getString("version"),
            item.getString("url"), references("licenses"), references("notices"), references("sources"))
    }
    require(libraries.isNotEmpty() && libraries.map { it.id }.distinct().size == libraries.size)
    return OpenSourceCatalog(libraries.sortedBy { it.name.lowercase(java.util.Locale.ROOT) }, texts)
}
