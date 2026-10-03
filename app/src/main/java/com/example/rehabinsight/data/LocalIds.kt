package com.example.rehabinsight.data

/**
 * Primary keys for rows that exist only on this device: the demo data, and anything created
 * while the server could not save it. They start far above any id the database will hand out,
 * so a row's id alone says whether the server knows about it.
 */
object LocalIds {
    const val FIRST = 1_000_000_000

    fun isLocal(id: Int): Boolean = id >= FIRST
}
