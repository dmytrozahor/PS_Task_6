package com.dmytrozah.profitsoft.domain.repository;

import com.dmytrozah.profitsoft.domain.entity.BookAuthorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookAuthorRepository extends JpaRepository<BookAuthorData, Long> {

    boolean existsByName_FirstNameIgnoreCaseAndName_LastNameIgnoreCase(
            String firstName, String lastName
    );

    boolean existsByCanonicalNameIgnoreCase(String canonicalName);

    Optional<BookAuthorData> findById(long id);

    Optional<BookAuthorData> findByCanonicalName(String canonicalName);
}
