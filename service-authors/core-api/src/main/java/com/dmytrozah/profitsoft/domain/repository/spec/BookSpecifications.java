package com.dmytrozah.profitsoft.domain.repository.spec;

import com.dmytrozah.profitsoft.domain.dto.book.query.BookQueryDto;
import com.dmytrozah.profitsoft.domain.dto.book.query.BookQueryDtoFilter;
import com.dmytrozah.profitsoft.domain.entity.BookAuthorData;
import com.dmytrozah.profitsoft.domain.entity.BookData;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class BookSpecifications {
    private BookSpecifications() {
    }

    public static Specification<BookData> fromQuery(BookQueryDto dto) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (dto.getAuthorId() != null && !dto.getAuthorId().equals("-1")) {
                Join<BookData, BookAuthorData> authorJoin =
                        root.join("author", JoinType.INNER);

                predicates.add(
                        cb.equal(
                                authorJoin.get("id"),
                                Long.parseLong(dto.getAuthorId())
                        )
                );
            }

            if (dto.getFilters() != null) {
                for (BookQueryDtoFilter filter : dto.getFilters()) {

                    if (filter.value() == null) continue;

                    switch (filter.attribute()) {

                        case TITLE -> predicates.add(
                                cb.like(
                                        cb.lower(root.get("title")),
                                        "%" + filter.value().toString().toLowerCase() + "%"
                                )
                        );

                        case AUTHOR_CANONICAL_NAME -> predicates.add(
                                cb.like(
                                        cb.lower(root.get("authorCanonicalName")),
                                        filter.value().toString().toLowerCase()
                                )
                        );
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
