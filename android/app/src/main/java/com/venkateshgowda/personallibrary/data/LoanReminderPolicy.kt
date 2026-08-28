package com.venkateshgowda.personallibrary.data

import java.time.LocalDate

object LoanReminderPolicy {
    fun needsReminder(expectedReturnDate: String?, today: LocalDate): Boolean {
        val due = expectedReturnDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return false
        return due == today || due == today.plusDays(3) || (due.isBefore(today) && today.dayOfWeek.value == 1)
    }
}