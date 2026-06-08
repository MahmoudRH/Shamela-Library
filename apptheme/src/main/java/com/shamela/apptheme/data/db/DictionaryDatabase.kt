package com.shamela.apptheme.data.db

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.shamela.apptheme.data.util.ArabicNormalizer
import com.shamela.apptheme.data.util.DictionaryPaths
import com.shamela.apptheme.domain.model.DictionaryEntry
import io.requery.android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Read-only access to the pre-built Arabic dictionary database.
 *
 * Unlike [DatabaseHelper], this does NOT create or migrate a schema — the file
 * ships fully built (offline) inside the APK at `assets/[DictionaryPaths.ASSET_NAME]`.
 * SQLite can't open a compressed asset directly, so on first use we copy it once to
 * internal storage ([ensureInstalled]) and then open that copy read-only via the
 * Requery SQLite library (the same engine that backs the app's FTS5 search, required
 * because the platform SQLite on older devices lacks FTS5). All Arabic morphology is
 * resolved at build time, so a lookup is a single normalized FTS5 MATCH on the
 * [DictionaryEntry.COL_FORMS] column — mirroring [DatabaseHelper.searchBook].
 */
class DictionaryDatabase private constructor(private val context: Context) {

    @Volatile
    private var db: SQLiteDatabase? = null

    /**
     * True when the dictionary is usable — i.e. already copied to disk, or still
     * available to copy from the bundled asset. Cheap (no copy / no full read).
     */
    fun isAvailable(): Boolean = DictionaryPaths.exists(context) || assetExists()

    private fun assetExists(): Boolean = try {
        context.assets.open(DictionaryPaths.ASSET_NAME).use { true }
    } catch (e: Exception) {
        false
    }

    /**
     * Copy the bundled DB from assets to internal storage if it's missing or was
     * produced by an older [DictionaryPaths.BUNDLED_VERSION]. Copies via a temp file
     * + rename so a crashed copy never leaves a half-written DB to be opened.
     * Caller holds the [openIfNeeded] monitor, so this runs at most once.
     */
    private fun ensureInstalled(): Boolean {
        val dest = DictionaryPaths.file(context)
        val marker = DictionaryPaths.versionFile(context)
        val upToDate = dest.isFile && dest.length() > 0L &&
            marker.takeIf { it.isFile }?.readText()?.trim()?.toIntOrNull() == DictionaryPaths.BUNDLED_VERSION
        if (upToDate) return true
        return try {
            DictionaryPaths.dir(context).mkdirs()
            val tmp = File(dest.parentFile, "${dest.name}.tmp")
            context.assets.open(DictionaryPaths.ASSET_NAME).use { input ->
                tmp.outputStream().use { output -> input.copyTo(output) }
            }
            dest.delete()
            if (!tmp.renameTo(dest)) {
                tmp.copyTo(dest, overwrite = true)
                tmp.delete()
            }
            marker.writeText(DictionaryPaths.BUNDLED_VERSION.toString())
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to install bundled dictionary", e)
            false
        }
    }

    private fun openIfNeeded(): SQLiteDatabase? {
        db?.let { if (it.isOpen) return it }
        return synchronized(this) {
            db?.let { if (it.isOpen) return it }
            if (!ensureInstalled()) return null
            try {
                SQLiteDatabase.openDatabase(
                    DictionaryPaths.file(context).absolutePath,
                    null,
                    SQLiteDatabase.OPEN_READONLY
                ).also { db = it }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open dictionary database", e)
                null
            }
        }
    }

    /**
     * Look up a selected word. Takes the first whitespace-delimited token (the
     * feature is single-word clarification), normalizes it the same way the FTS5
     * index was built, and returns matching entries ranked by FTS5 relevance.
     */
    suspend fun search(rawQuery: String): List<DictionaryEntry> = withContext(Dispatchers.IO) {
        // Take the first run of Arabic letters/diacritics from the selection,
        // dropping any leading/trailing punctuation, tatweel, Latin or digits so
        // a word like "الكتابِ،" still resolves.
        val term = ARABIC_WORD.find(rawQuery)?.value.orEmpty()
        if (term.isEmpty()) return@withContext emptyList()
        val database = openIfNeeded() ?: return@withContext emptyList()
        val normalized = ArabicNormalizer().normalize(term)
        if (normalized.isEmpty()) return@withContext emptyList()

        // 1) Exact match first — these rank highest and are the best answer.
        queryForms(database, normalized).let { if (it.isNotEmpty()) return@withContext it }

        // 2) Arabic stacks clitics on words ("وصدورهم" = و+صدور+هم, "والكتاب" = وال+كتاب).
        //    The dictionary is keyed by base forms, so on an exact miss retry with
        //    affix-stripped variants — least-stripped first — and return the first hit.
        for (stem in candidateStems(normalized)) {
            queryForms(database, stem).let { if (it.isNotEmpty()) return@withContext it }
        }
        emptyList()
    }

    @SuppressLint("Range")
    private fun queryForms(database: SQLiteDatabase, term: String): List<DictionaryEntry> {
        val results = mutableListOf<DictionaryEntry>()
        try {
            val cursor = database.query(
                DictionaryEntry.TABLE_NAME,
                arrayOf(
                    DictionaryEntry.COL_ROOT,
                    DictionaryEntry.COL_HEADWORD,
                    DictionaryEntry.COL_DEFINITION
                ),
                "${DictionaryEntry.COL_FORMS} MATCH ?",
                arrayOf("\"$term\""),
                null,
                null,
                "rank"
            )
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                results.add(
                    DictionaryEntry(
                        root = cursor.getString(cursor.getColumnIndex(DictionaryEntry.COL_ROOT)).orEmpty(),
                        headword = cursor.getString(cursor.getColumnIndex(DictionaryEntry.COL_HEADWORD)).orEmpty(),
                        definition = cursor.getString(cursor.getColumnIndex(DictionaryEntry.COL_DEFINITION)).orEmpty(),
                    )
                )
                cursor.moveToNext()
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e(TAG, "Dictionary query failed for '$term'", e)
        }
        return results
    }

    companion object {
        private const val TAG = "DictionaryDatabase"
        // Arabic letters (hamza..yaa) + tashkeel; excludes punctuation, tatweel, Latin, digits.
        private val ARABIC_WORD = Regex("[\\u0621-\\u064A\\u064B-\\u0652]+")

        // Clitics to strip from a looked-up word (kept in sync with tools/arabic_common.py
        // candidate_stems). Proclitics (and stacked combos) + enclitic pronoun/inflection
        // suffixes. NOT a morphological analyzer — recovers the common clitic-attached
        // surface forms that dominate real text.
        private val PREFIXES = listOf(
            "", "و", "ف", "ال", "وال", "فال", "ب", "ك", "ل", "بال", "كال", "لل",
            "ول", "فل", "وب", "فب", "س", "ولل", "فلل"
        )
        private val SUFFIXES = listOf(
            "", "ها", "هما", "هم", "هن", "كما", "كم", "كن", "نا", "ني",
            "ات", "ون", "ين", "ان", "تين", "ية", "ته", "تها", "وا", "تم",
            "ة", "ه", "ك", "ي", "ا", "ت"
        )
        private const val MIN_STEM = 3

        /** Candidate stems after removing 0–1 proclitic AND 0–1 enclitic; least-stripped first. */
        private fun candidateStems(word: String): List<String> {
            val out = LinkedHashSet<String>()
            for (p in PREFIXES) {
                if (p.isNotEmpty() && !word.startsWith(p)) continue
                val mid = word.substring(p.length)
                for (s in SUFFIXES) {
                    if (s.isNotEmpty() && !mid.endsWith(s)) continue
                    val stem = if (s.isEmpty()) mid else mid.substring(0, mid.length - s.length)
                    if (stem.length >= MIN_STEM) out.add(stem)
                }
            }
            out.remove(word)
            // Longer stem = fewer affixes removed = closest to the original → try first.
            return out.sortedByDescending { it.length }
        }

        @Volatile
        private var INSTANCE: DictionaryDatabase? = null

        fun getInstance(context: Context): DictionaryDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DictionaryDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
