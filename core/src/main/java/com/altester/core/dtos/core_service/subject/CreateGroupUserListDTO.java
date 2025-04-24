package com.altester.core.dtos.core_service.subject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateGroupUserListDTO {
        private long userId;
        private String name;
        private String surname;
        private String username;
        private List<String> subjectNames;

        private boolean inSameSubject;
        private String subjectName;
        private String subjectShortName;

        public CreateGroupUserListDTO(Long userId, String name, String surname, String username, List<String> subjectNames) {
                this.userId = userId;
                this.name = name;
                this.surname = surname;
                this.username = username;
                this.subjectNames = subjectNames;
                this.inSameSubject = false;
        }
}
