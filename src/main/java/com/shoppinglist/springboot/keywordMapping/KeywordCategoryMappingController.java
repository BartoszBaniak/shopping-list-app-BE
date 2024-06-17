package com.shoppinglist.springboot.keywordMapping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class KeywordCategoryMappingController {

    @Autowired
    private KeywordCategoryMappingRepository keywordCategoryMappingRepository;

    @GetMapping("/autocomplete")
    public List<String> autocomplete(@RequestParam String query) {
        List<KeywordCategoryMapping> mappings = keywordCategoryMappingRepository.findByKeywordContainingIgnoreCase(query);
        return mappings.stream().map(KeywordCategoryMapping::getKeyword).collect(Collectors.toList());
    }
}