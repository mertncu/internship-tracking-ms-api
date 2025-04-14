package com.internship.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "internships")
@Getter
@Setter
@ToString(exclude = {"documents", "workingDays", "approvals", "reports", "sgkDeclaration"})
@EqualsAndHashCode(exclude = {"documents", "workingDays", "approvals", "reports", "sgkDeclaration"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Internship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advisor_id")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private User advisor;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String companyAddress;

    @Column(nullable = false)
    private String companyPhone;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Integer workDays;

    @Column(nullable = false)
    private Boolean insuranceSupport;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InternshipStatus status = InternshipStatus.PENDING;

    private String rejectionReason;

    private String documentPath;

    private String documentName;

    private String documentType;

    private LocalDateTime documentUploadDate;

    @OneToMany(mappedBy = "internship", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("internship")
    private Set<Document> documents = new HashSet<>();

    @OneToMany(mappedBy = "internship", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ApplicationWorkingDays> workingDays = new HashSet<>();

    @OneToMany(mappedBy = "internship", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<ApplicationApproval> approvals = new HashSet<>();

    @OneToMany(mappedBy = "internship", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("internship")
    private Set<InternshipReport> reports = new HashSet<>();

    @OneToOne(mappedBy = "internship", cascade = CascadeType.ALL, orphanRemoval = true)
    private SGKDeclaration sgkDeclaration;

    @Column(nullable = false)
    private Boolean isPaid;

    @Column(nullable = false)
    private Boolean parentalInsuranceCoverage;

    @Column
    private String companyIBAN;

    @Column
    private String bankName;

    @Column
    private String bankBranch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InternshipType type;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public User getAdvisor() {
        return advisor;
    }

    public void setAdvisor(User advisor) {
        this.advisor = advisor;
    }
} 