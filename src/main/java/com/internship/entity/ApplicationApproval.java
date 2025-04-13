package com.internship.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "application_approvals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationApproval {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internship_id", nullable = false)
    private Internship internship;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id", nullable = false)
    private User approver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InternshipStatus status;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false)
    private LocalDateTime actionDate;

    public void setStatus(InternshipStatus status) {
        this.status = status;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setActionDate(LocalDateTime actionDate) {
        this.actionDate = actionDate;
    }

    public Internship getInternship() {
        return internship;
    }
} 