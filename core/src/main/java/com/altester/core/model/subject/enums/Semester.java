package com.altester.core.model.subject.enums;

public enum Semester {
    WINTER("Winter"),
    SUMMER("Summer"),;

    private final String displayName;

    Semester(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}