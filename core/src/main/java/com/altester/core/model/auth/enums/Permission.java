package com.altester.core.model.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Permission {

    ADMIN_READ("admin:read"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_CREATE("admin:create"),
    ADMIN_DELETE("admin:delete"),

    TEACHER_READ("teacher:read"),
    TEACHER_UPDATE("teacher:update"),
    TEACHER_CREATE("teacher:create"),
    TEACHER_DELETE("teacher:delete"),

    STUDENT_READ("user:read"),
    STUDENT_UPDATE("user:update"),
    STUDENT_CREATE("user:create"),
    STUDENT_DELETE("user:delete"),
    ;

    @Getter
    private final String permission;
}
