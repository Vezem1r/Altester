package com.altester.core.model.subject;

import com.altester.core.model.auth.User;
import com.altester.core.model.subject.enums.Semester;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name = "groups")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"students"})
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Semester semester;

    @Column(nullable = false)
    private Integer academicYear;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToMany()
    @JoinTable(
            name = "student_groups",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonManagedReference
    private Set<User> students = new HashSet<>();

    @ManyToOne()
    @JoinColumn(name = "teacher_id")
    private User teacher;

    @OneToMany
    @JoinColumn(name = "group_id")
    private Set<Test> tests = new HashSet<>();
}
