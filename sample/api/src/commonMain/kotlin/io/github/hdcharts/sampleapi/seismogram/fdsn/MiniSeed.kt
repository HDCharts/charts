package io.github.hdcharts.sampleapi.seismogram.fdsn

import io.github.hdcharts.sampleapi.seismogram.GroundMotion
import kotlin.time.Instant

private const val FIXED_HEADER_SIZE = 48
private const val FRAME_SIZE = 64
private const val BLOCKETTE_1000 = 1000
private const val ENCODING_STEIM1 = 10
private const val ENCODING_STEIM2 = 11
private const val BIG_ENDIAN = 1

// Records from 128 bytes to 64 KiB, the lengths SEED allows in practice.
private val RECORD_LENGTH_EXPONENTS = 7..16

/**
 * Decodes big-endian miniSEED 2 records compressed with Steim1 or Steim2, as FDSN data centers serve them.
 * Records in another encoding or byte order are skipped.
 */
internal fun decodeMiniSeed(bytes: ByteArray): List<GroundMotion> {
    val records = mutableListOf<GroundMotion>()
    var offset = 0
    while (offset + FIXED_HEADER_SIZE <= bytes.size) {
        val format = bytes.recordFormat(offset) ?: break
        val steim = format.encoding == ENCODING_STEIM1 || format.encoding == ENCODING_STEIM2
        if (format.wordOrder == BIG_ENDIAN && steim) {
            val samples =
                bytes.steimSamples(
                    start = offset + bytes.u16(offset + 44),
                    end = minOf(offset + format.recordLength, bytes.size),
                    count = bytes.u16(offset + 30),
                    steim2 = format.encoding == ENCODING_STEIM2,
                )
            if (samples.isNotEmpty()) {
                records +=
                    GroundMotion(
                        start = bytes.startTime(offset),
                        samplesPerSecond =
                            sampleRate(
                                factor = bytes.s16(offset + 32),
                                multiplier =
                                    bytes.s16(
                                        offset + 34,
                                    ),
                            ),
                        samples = samples,
                    )
            }
        }
        offset += format.recordLength
    }
    return records
}

private class RecordFormat(
    val encoding: Int,
    val wordOrder: Int,
    val recordLength: Int,
)

// Blockette 1000 carries the encoding and record length; a record without it, or with a broken chain, cannot be read.
private fun ByteArray.recordFormat(offset: Int): RecordFormat? {
    var blockette = u16(offset + 46)
    while (blockette != 0 && offset + blockette + 8 <= size) {
        val position = offset + blockette
        if (u16(position) == BLOCKETTE_1000) {
            val lengthExponent = u8(position + 6)
            if (lengthExponent !in RECORD_LENGTH_EXPONENTS) return null
            return RecordFormat(
                encoding = u8(position + 4),
                wordOrder = u8(position + 5),
                recordLength = 1 shl lengthExponent,
            )
        }
        val next = u16(position + 2)
        if (next != 0 && next <= blockette) return null
        blockette = next
    }
    return null
}

private fun ByteArray.startTime(offset: Int): Instant {
    val year = u16(offset + 20)
    val dayOfYear = u16(offset + 22)
    val seconds =
        (epochDayOfYearStart(year) + dayOfYear - 1) * 86_400L +
            u8(offset + 24) * 3_600L + u8(offset + 25) * 60L + u8(offset + 26)
    // The last field counts tenths of a millisecond.
    return Instant.fromEpochSeconds(seconds, nanosecondAdjustment = u16(offset + 28) * 100_000L)
}

private fun epochDayOfYearStart(year: Int): Long {
    fun leapDaysThrough(y: Long) = y / 4 - y / 100 + y / 400
    return 365L * (year - 1970) + leapDaysThrough(year - 1L) - leapDaysThrough(1969L)
}

// SEED stores the rate as a factor and a multiplier; a negative value divides instead of multiplies.
private fun sampleRate(
    factor: Int,
    multiplier: Int,
): Double {
    val base = if (factor >= 0) factor.toDouble() else -1.0 / factor
    return when {
        multiplier > 0 -> base * multiplier
        multiplier < 0 -> base / -multiplier
        else -> base
    }
}

/** The first sample is stored whole; every later one adds the next difference. */
private fun ByteArray.steimSamples(
    start: Int,
    end: Int,
    count: Int,
    steim2: Boolean,
): List<Int> {
    if (count == 0) return emptyList()
    val differences = ArrayList<Int>(count)
    var first = 0
    var frame = start
    while (frame + FRAME_SIZE <= end && differences.size < count) {
        val nibbles = s32(frame)
        for (word in 1 until 16) {
            val value = s32(frame + 4 * word)
            when {
                // The first frame opens with the first sample and the last sample, used for checking only.
                frame == start && word == 1 -> first = value
                frame == start && word == 2 -> Unit
                steim2 -> differences.addSteim2(nibble = (nibbles ushr (30 - 2 * word)) and 3, word = value)
                else -> differences.addSteim1(nibble = (nibbles ushr (30 - 2 * word)) and 3, word = value)
            }
        }
        frame += FRAME_SIZE
    }
    if (differences.isEmpty()) return emptyList()
    val samples = IntArray(minOf(count, differences.size))
    samples[0] = first
    for (index in 1 until samples.size) {
        samples[index] = samples[index - 1] + differences[index]
    }
    return samples.asList()
}

private fun MutableList<Int>.addSteim1(
    nibble: Int,
    word: Int,
) {
    when (nibble) {
        1 -> addPacked(word = word, count = 4, bits = 8)
        2 -> addPacked(word = word, count = 2, bits = 16)
        3 -> add(word)
    }
}

private fun MutableList<Int>.addSteim2(
    nibble: Int,
    word: Int,
) {
    val subtype = word ushr 30
    when {
        nibble == 1 -> addPacked(word = word, count = 4, bits = 8)
        nibble == 2 && subtype == 1 -> addPacked(word = word, count = 1, bits = 30)
        nibble == 2 && subtype == 2 -> addPacked(word = word, count = 2, bits = 15)
        nibble == 2 && subtype == 3 -> addPacked(word = word, count = 3, bits = 10)
        nibble == 3 && subtype == 0 -> addPacked(word = word, count = 5, bits = 6)
        nibble == 3 && subtype == 1 -> addPacked(word = word, count = 6, bits = 5)
        nibble == 3 && subtype == 2 -> addPacked(word = word, count = 7, bits = 4)
    }
}

/** Adds [count] signed [bits]-wide differences packed into the low bits of [word], most significant first. */
private fun MutableList<Int>.addPacked(
    word: Int,
    count: Int,
    bits: Int,
) {
    val mask = (1 shl bits) - 1
    for (index in 0 until count) {
        val field = (word ushr (bits * (count - 1 - index))) and mask
        add((field shl (32 - bits)) shr (32 - bits))
    }
}

private fun ByteArray.u8(index: Int): Int = this[index].toInt() and 0xFF

private fun ByteArray.u16(index: Int): Int = (u8(index) shl 8) or u8(index + 1)

private fun ByteArray.s16(index: Int): Int = u16(index).toShort().toInt()

private fun ByteArray.s32(index: Int): Int = (u16(index) shl 16) or u16(index + 2)
