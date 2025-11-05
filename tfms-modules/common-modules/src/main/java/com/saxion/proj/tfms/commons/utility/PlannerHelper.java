package com.saxion.proj.tfms.commons.utility;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;

@Component
public class Helper {

    /**
     * Compute next valid planned delivery date (skip Sunday)
     */
    public ZonedDateTime ComputePlannedDate(ZonedDateTime plannedDate) {
        ZonedDateTime date = (plannedDate == null)
                ? ZonedDateTime.now().plusDays(1)
                : plannedDate;

        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }
}
