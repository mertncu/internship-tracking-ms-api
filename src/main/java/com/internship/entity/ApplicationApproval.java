package com.internship.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "application_approvals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationApproval {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internship_id", nullable = false)
    private Internship internship;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id", nullable = false)
    private User approver;

    @Column(nullable = false)
    private String approverRole; // FACULTY_ADVISOR, DEPARTMENT_COORDINATOR, UNIVERSITY_COORDINATOR

    @Column(nullable = false)
    private LocalDateTime actionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InternshipStatus resultStatus;

    @Column
    private String comments; // Red veya revizyon durumunda açıklama

    @Column(nullable = false)
    private Boolean isApproved;
} 