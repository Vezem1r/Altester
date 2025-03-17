package com.altester.core.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
@Getter
public class SemesterConfig {

    @Value("${app.semester.winter-start}")
    private String winterStartStr;

    @Value("${app.semester.winter-end}")
    private String winterEndStr;

    @Value("${app.semester.summer-start}")
    private String summerStartStr;

    @Value("${app.semester.summer-end}")
    private String summerEndStr;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public boolean isSemesterActive(String semester, Integer academicYear) {
        if (semester == null || academicYear == null) {
            return false;
        }

        String currentSemester = getCurrentSemester();
        int currentAcademicYear = getCurrentAcademicYear();

        log.info("Current semester is {}", semester.equals(currentSemester) ? "ACTIVE" : "INACTIVE");

        return semester.equals(currentSemester) && academicYear.equals(currentAcademicYear);
    }

    public String getCurrentSemester() {
        LocalDate now = LocalDate.now();

        try {
            LocalDate winterStart = LocalDate.parse(winterStartStr, dateFormatter);
            LocalDate winterEnd = LocalDate.parse(winterEndStr, dateFormatter);
            LocalDate summerStart = LocalDate.parse(summerStartStr, dateFormatter);
            LocalDate summerEnd = LocalDate.parse(summerEndStr, dateFormatter);

            if ((now.isEqual(summerStart) || now.isAfter(summerStart)) &&
                    (now.isEqual(summerEnd) || now.isBefore(summerEnd))) {
                log.info("Current semester is SUMMER");
                return "SUMMER";
            } else if ((now.isEqual(winterStart) || now.isAfter(winterStart)) &&
                    (now.isEqual(winterEnd) || now.isBefore(winterEnd))) {
                log.info("Current semester is WINTER");
                return "WINTER";
            } else {
                if (now.isBefore(summerStart)) {
                    log.info("Current semester is SUMMER");
                    return "SUMMER";
                } else {
                    log.info("Current semester is WINTER");
                    return "WINTER";
                }
            }
        } catch (Exception e) {
            log.error("Error parsing semester dates: {}", e.getMessage());
            return fallbackGetCurrentSemester();
        }
    }

    private String fallbackGetCurrentSemester() {
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();

        if (month == 2 && day >= 17 || month > 2 && month <= 6 || month == 7) {
            log.info("Current semester is SUMMER");
            return "SUMMER";
        } else {
            log.info("Current semester is WINTER");
            return "WINTER";
        }
    }

    public int getCurrentAcademicYear() {
        LocalDate now = LocalDate.now();

        try {
            LocalDate winterStart = LocalDate.parse(winterStartStr, dateFormatter);
            int winterStartYear = winterStart.getYear();

            if (now.isEqual(winterStart) || now.isAfter(winterStart)) {
                log.info("Current academic year is {}", winterStartYear);
                return winterStartYear;
            } else {
                log.info("Current academic year is {}", winterStartYear - 1);
                return winterStartYear - 1;
            }
        } catch (Exception e) {
            log.error("Error parsing winter start date: {}", e.getMessage());
            int year = now.getYear();
            int month = now.getMonthValue();

            if (month >= 9) {
                return year;
            } else {
                return year - 1;
            }
        }
    }
}