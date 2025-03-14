package com.altester.core.dtos.core_service.subject;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GroupUserList {
    private long userId;
    private String name;
    private String surname;
    private String username;
}
