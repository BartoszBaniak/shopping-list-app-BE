package com.shoppinglist.springboot.keywordMapping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KeywordCategoryMappingRepository extends JpaRepository<KeywordCategoryMapping, Long> {
    Optional<KeywordCategoryMapping> findByKeyword(String keyword);

    List<KeywordCategoryMapping> findByKeywordContainingIgnoreCase(String keyword);
}