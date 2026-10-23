package com.anix.android.anixlifetracker.notification.engine

import com.anix.android.anixlifetracker.model.*
import kotlin.time.Clock

object ReminderScheduleCalculator {

    /**
     * Calculates occurrences for a given schedule within a bounded horizon.
     */
    fun calculateOccurrences(
        definition: ReminderDefinitionModel,
        schedule: ReminderScheduleModel,
        horizonDays: Int = 30,
        referenceInstantISO: String = ""
    ): List<ReminderOccurrenceModel> {
        if (!definition.isEnabled) return emptyList()

        val refISO = referenceInstantISO.ifBlank { Clock.System.now().toString() }
        val occurrences = mutableListOf<ReminderOccurrenceModel>()
        val baseDateStr = schedule.localDueDateOrTime.take(10) // YYYY-MM-DD
        val parts = baseDateStr.split("-")
        if (parts.size < 3) return emptyList()

        val year = parts[0].toIntOrNull() ?: 2026
        val month = parts[1].toIntOrNull() ?: 2
        val day = parts[2].toIntOrNull() ?: 1

        val anchor = schedule.anchorDay ?: day

        val iterations = if (!schedule.isRecurring || schedule.cadence == CadenceType.ONE_OFF) 1 else schedule.endCount ?: horizonDays

        val creationTimeISO = if (definition.createdAt.isNotBlank()) definition.createdAt else referenceInstantISO

        for (i in 0 until iterations) {
            val (calcYear, calcMonth, calcDay) = computeNextDate(schedule.cadence, year, month, day, anchor, i)
            val formattedDate = "${calcYear}-${calcMonth.toString().padStart(2, '0')}-${calcDay.toString().padStart(2, '0')}"

            // Check end date bound
            if (schedule.endDate != null && formattedDate > schedule.endDate) {
                break
            }

            val timeSuffix = when (schedule.dateOnlyPolicy) {
                DateOnlyPolicy.LOCAL_DAY_END -> "T23:59:59Z"
                DateOnlyPolicy.LOCAL_DAY_START -> "T00:00:00Z"
                DateOnlyPolicy.EXACT_TIME -> if (schedule.localDueDateOrTime.contains("T")) schedule.localDueDateOrTime.substring(10) else "T12:00:00Z"
            }

            val localDateTimeISO = "${formattedDate}${timeSuffix}"
            val deterministicId = "${definition.id}_${formattedDate}"

            val occurrenceState = if (formattedDate < refISO.take(10)) {
                OccurrenceState.OVERDUE
            } else if (formattedDate == refISO.take(10)) {
                OccurrenceState.DUE
            } else {
                OccurrenceState.UPCOMING
            }

            occurrences.add(
                ReminderOccurrenceModel(
                    id = deterministicId,
                    reminderId = definition.id,
                    ownerId = definition.ownerId,
                    scheduleVersion = schedule.scheduleVersion,
                    scheduledLocalDateTime = localDateTimeISO,
                    resolvedInstant = localDateTimeISO,
                    state = occurrenceState,
                    amount = null,
                    createdAt = creationTimeISO,
                    updatedAt = refISO
                )
            )

            if (!schedule.isRecurring || schedule.cadence == CadenceType.ONE_OFF) break
        }

        return occurrences
    }

    private fun computeNextDate(
        cadence: CadenceType,
        startYear: Int,
        startMonth: Int,
        startDay: Int,
        anchorDay: Int,
        stepIndex: Int
    ): Triple<Int, Int, Int> {
        if (stepIndex == 0) {
            val validDay = startDay.coerceAtMost(daysInMonth(startYear, startMonth))
            return Triple(startYear, startMonth, validDay)
        }

        return when (cadence) {
            CadenceType.ONE_OFF -> Triple(startYear, startMonth, startDay)
            CadenceType.DAILY -> {
                var d = startDay + stepIndex
                var m = startMonth
                var y = startYear
                while (d > daysInMonth(y, m)) {
                    d -= daysInMonth(y, m)
                    m++
                    if (m > 12) {
                        m = 1
                        y++
                    }
                }
                Triple(y, m, d)
            }
            CadenceType.WEEKLY -> {
                var d = startDay + (stepIndex * 7)
                var m = startMonth
                var y = startYear
                while (d > daysInMonth(y, m)) {
                    d -= daysInMonth(y, m)
                    m++
                    if (m > 12) {
                        m = 1
                        y++
                    }
                }
                Triple(y, m, d)
            }
            CadenceType.MONTHLY -> {
                var totalMonths = (startMonth - 1) + stepIndex
                val y = startYear + (totalMonths / 12)
                val m = (totalMonths % 12) + 1
                val validDay = anchorDay.coerceAtMost(daysInMonth(y, m))
                Triple(y, m, validDay)
            }
            CadenceType.YEARLY -> {
                val y = startYear + stepIndex
                val validDay = anchorDay.coerceAtMost(daysInMonth(y, startMonth))
                Triple(y, startMonth, validDay)
            }
        }
    }

    private fun daysInMonth(year: Int, month: Int): Int {
        return when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (isLeapYear(year)) 29 else 28
            else -> 30
        }
    }

    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }
}
