package com.example.util

import com.example.data.model.Heir
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object BanglaHelper {

    private val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    private val englishDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

    fun toBanglaDigits(input: String?): String {
        if (input == null) return ""
        val builder = java.lang.StringBuilder()
        for (ch in input) {
            if (ch in '0'..'9') {
                builder.append(banglaDigits[ch - '0'])
            } else {
                builder.append(ch)
            }
        }
        return builder.toString()
    }

    fun toBanglaDigits(number: Number): String {
        return toBanglaDigits(number.toString())
    }

    fun toEnglishDigits(input: String?): String {
        if (input == null) return ""
        val builder = java.lang.StringBuilder()
        for (ch in input) {
            val idx = banglaDigits.indexOf(ch)
            if (idx != -1) {
                builder.append(englishDigits[idx])
            } else {
                builder.append(ch)
            }
        }
        return builder.toString()
    }

    private val banglaMonths = arrayOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )

    fun getCurrentDateBangla(): String {
        val cal = Calendar.getInstance()
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)
        return "${toBanglaDigits(day)} ${banglaMonths[month]}, ${toBanglaDigits(year)} খ্রিঃ"
    }

    fun formatDateToBangla(date: Date): String {
        val cal = Calendar.getInstance()
        cal.time = date
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)
        return "${toBanglaDigits(day)} ${banglaMonths[month]}, ${toBanglaDigits(year)} খ্রিঃ"
    }

    fun generateReferenceNumber(idSeed: Long): String {
        val cal = Calendar.getInstance()
        val year = toBanglaDigits(cal.get(Calendar.YEAR))
        val serial = toBanglaDigits(String.format(Locale.US, "%04d", (idSeed % 9999) + 1))
        return "ইউপি/$year/$serial"
    }

    fun formatCustomFieldsJson(map: Map<String, String>): String {
        val json = JSONObject()
        for ((k, v) in map) {
            json.put(k, v)
        }
        return json.toString()
    }

    fun parseCustomFieldsJson(jsonStr: String?): Map<String, String> {
        if (jsonStr.isNullOrBlank()) return emptyMap()
        val map = mutableMapOf<String, String>()
        try {
            val json = JSONObject(jsonStr)
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = json.optString(key, "")
            }
        } catch (_: Exception) {}
        return map
    }

    fun formatHeirsJson(heirs: List<Heir>): String {
        val array = JSONArray()
        for (h in heirs) {
            val obj = JSONObject()
            obj.put("id", h.id)
            obj.put("name", h.name)
            obj.put("relation", h.relation)
            obj.put("age", h.age)
            obj.put("remarks", h.remarks)
            array.put(obj)
        }
        return array.toString()
    }

    fun parseHeirsJson(jsonStr: String?): List<Heir> {
        if (jsonStr.isNullOrBlank()) return emptyList()
        val list = mutableListOf<Heir>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    Heir(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        name = obj.optString("name", ""),
                        relation = obj.optString("relation", ""),
                        age = obj.optString("age", ""),
                        remarks = obj.optString("remarks", "")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }
}
