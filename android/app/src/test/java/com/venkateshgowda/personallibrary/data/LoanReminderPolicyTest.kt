package com.venkateshgowda.personallibrary.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class LoanReminderPolicyTest {
    private val monday = LocalDate.parse("2026-08-24")

    @Test fun remindsThreeDaysBeforeAndOnDueDate() {
        assertTrue(LoanReminderPolicy.needsReminder("2026-08-27", monday))
        assertTrue(LoanReminderPolicy.needsReminder("2026-08-24", monday))
    }

    @Test fun remindsOverdueLoansWeeklyOnMonday() {
        assertTrue(LoanReminderPolicy.needsReminder("2026-08-20", monday))
        assertFalse(LoanReminderPolicy.needsReminder("2026-08-20", monday.plusDays(1)))
    }
}