package com.altester.core.model.subject.enums;

import lombok.Getter;

@Getter
public enum Semester {
    WINTER("Winter"),
    SUMMER("Summer"),;

    private final String displayName;

    Semester(String displayName) {
        this.displayName = displayName;
    }
}