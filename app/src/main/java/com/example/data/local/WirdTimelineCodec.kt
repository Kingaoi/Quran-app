package com.example.data.local

import org.json.JSONObject

internal object WirdTimelineCodec {
    fun decodeIntLongMap(raw: String?): Map<Int, Long> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching { decodeIntLongMap(JSONObject(raw)) }.getOrDefault(emptyMap())
    }

    fun decodeIntLongMap(obj: JSONObject?): Map<Int, Long> {
        if (obj == null) return emptyMap()
        val result = LinkedHashMap<Int, Long>()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            val idx = k.toIntOrNull() ?: continue
            val day = obj.optLong(k, Long.MIN_VALUE)
            if (idx >= 0 && day != Long.MIN_VALUE) {
                result[idx] = day
            }
        }
        return result
    }

    fun encodeIntLongMap(map: Map<Int, Long>): JSONObject = JSONObject().apply {
        map.forEach { (k, v) -> put(k.toString(), v) }
    }

    fun encodeIntLongMapString(map: Map<Int, Long>): String =
        if (map.isEmpty()) "" else encodeIntLongMap(map).toString()
}
