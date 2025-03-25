package com.internship.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sgk_declarations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SGKDeclaration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internship_id", nullable = false)
    private Internship internship;

    @Column(nullable = false)
    private String declarationNumber;

    @Column(nullable = false)
    private LocalDateTime generationDate;

    @Column(nullable = false)
    private String documentPath;

    @Column(nullable = false)
    private Boolean isProcessed;

    @Column
    private String processNotes;
} 