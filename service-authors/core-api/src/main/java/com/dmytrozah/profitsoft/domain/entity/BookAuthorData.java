package com.dmytrozah.profitsoft.domain.entity;

import com.dmytrozah.profitsoft.domain.entity.embeds.AuthorLivingAddress;
import com.dmytrozah.profitsoft.domain.entity.embeds.AuthorName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "book_author_data")
@RequiredArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class BookAuthorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String phoneNumber;

    private AuthorName name;

    private String canonicalName;

    private AuthorLivingAddress postalAddress;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookData> books;

}
