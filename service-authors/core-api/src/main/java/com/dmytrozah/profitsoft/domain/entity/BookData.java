package com.dmytrozah.profitsoft.domain.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "books")
@Getter @Setter
@RequiredArgsConstructor
public class BookData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private BookAuthorData author;

    private String title;

    @Column(name = "author_canonical_name")
    private String authorCanonicalName;

    private String genres;

    @Column(nullable = false)
    private LocalDate publication;

    @UpdateTimestamp
    private Instant lastUpdateTime;

    @PrePersist
    public void prePersist() {
        if (publication == null) {
            publication = LocalDate.now();
        }
    }
}
