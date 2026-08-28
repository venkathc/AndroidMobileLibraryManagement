package com.venkateshgowda.personallibrary.data

object PriceValidator {
    private val inrPrice = Regex("^(0|[1-9][0-9]*)(\\.[0-9]{1,2})?$")

    fun paise(value: String): Long? {
        if (!inrPrice.matches(value)) return null
        val parts = value.split('.', limit = 2)
        val rupees = parts[0].toLongOrNull() ?: return null
        val fractionalPaise = parts.getOrNull(1)?.padEnd(2, '0')?.toLongOrNull() ?: 0L
        return runCatching { Math.addExact(Math.multiplyExact(rupees, 100), fractionalPaise) }.getOrNull()
    }
}