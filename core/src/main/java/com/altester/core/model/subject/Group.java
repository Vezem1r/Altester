package com.altester.core.model.subject;

import com.altester.core.model.auth.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name = "groups")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToOne()
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToMany()
    @JoinTable(
            name = "student_groups",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> students = new HashSet<>();

    @ManyToOne()
    @JoinColumn(name = "teacher_id")
    private User teacher;
}
