package com.shoppinglist.springboot.keywordMapping;

import com.shoppinglist.springboot.keywordMapping.KeywordCategoryMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface KeywordCategoryMappingRepository extends JpaRepository<KeywordCategoryMapping, Long> {
    Optional<KeywordCategoryMapping> findByKeyword(String keyword);
    List<KeywordCategoryMapping> findByKeywordContainingIgnoreCase(String keyword);
}