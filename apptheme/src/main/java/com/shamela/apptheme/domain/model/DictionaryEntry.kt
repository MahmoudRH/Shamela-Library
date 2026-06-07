package com.shamela.apptheme.domain.model

/**
 * One dictionary entry returned from a word lookup.
 *
 * The dictionary database is a pre-built FTS5 table (created offline). The
 * matched column is [COL_FORMS], which holds every normalized surface/inflected
 * form of the entry (space-joined) so that looking up an inflected word resolves
 * to its entry without any runtime morphology. [COL_ROOT]/[COL_HEADWORD]/
 * [COL_DEFINITION] are stored UNINDEXED, for display only.
 */
data class DictionaryEntry(
    val headword: String,
    val definition: String,
    val root: String = "",
) {
    companion object {
        const val TABLE_NAME = "dictionary_fts"
        const val COL_ROOT = "root"
        const val COL_HEADWORD = "headword"
        const val COL_DEFINITION = "definition"
        const val COL_FORMS = "forms"
    }
}
