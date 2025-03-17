package com.altester.core.dtos.core_service.subject;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CreateGroupUserListDTO {
        private long userId;
        private String name;
        private String surname;
        private String username;
        private List<String> subjectNames;
}
