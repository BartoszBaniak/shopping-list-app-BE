package com.shoppinglist.springboot.keywordMapping;

import com.shoppinglist.springboot.shoppingList.Product;
import com.shoppinglist.springboot.shoppingList.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class KeywordCategoryMappingService {

    private final KeywordCategoryMappingRepository keywordCategoryMappingRepository;
    private final ResourceLoader resourceLoader;
    @Autowired
    private ProductRepository productRepository;

    public KeywordCategoryMappingService(KeywordCategoryMappingRepository keywordCategoryMappingRepository, ResourceLoader resourceLoader) {
        this.keywordCategoryMappingRepository = keywordCategoryMappingRepository;
        this.resourceLoader = resourceLoader;
    }

    @Transactional
    public void saveMappingsFromJsonFile() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            InputStream inputStream = resourceLoader.getResource("classpath:keywords.json").getInputStream();
            Map<String, Object> jsonData = objectMapper.readValue(inputStream, Map.class);

            Map<String, String> mappings = (Map<String, String>) jsonData.get("keywords");
            mappings.forEach((keyword, category) -> {
                Optional<KeywordCategoryMapping> existingMapping = keywordCategoryMappingRepository.findByKeyword(keyword);
                if (existingMapping.isPresent()) {
                    // Jeśli keyword już istnieje, zaktualizuj kategorię
                    KeywordCategoryMapping mapping = existingMapping.get();
                    mapping.setCategory(category);
                    keywordCategoryMappingRepository.save(mapping);
                } else {
                    // Jeśli keyword nie istnieje, dodaj nowy rekord
                    KeywordCategoryMapping mapping = new KeywordCategoryMapping();
                    mapping.setKeyword(keyword);
                    mapping.setCategory(category);
                    keywordCategoryMappingRepository.save(mapping);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Transactional
    public void saveProductFromJsonFile() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            InputStream inputStream = resourceLoader.getResource("classpath:keywords.json").getInputStream();
            Map<String, Object> jsonData = objectMapper.readValue(inputStream, Map.class);

            Map<String, String> mappings = (Map<String, String>) jsonData.get("keywords");

            mappings.forEach((keyword, category) -> {
                // Handle KeywordCategoryMapping
                Optional<KeywordCategoryMapping> existingMapping = keywordCategoryMappingRepository.findByKeyword(keyword);
                if (existingMapping.isPresent()) {
                    KeywordCategoryMapping mapping = existingMapping.get();
                    mapping.setCategory(category);
                    keywordCategoryMappingRepository.save(mapping);
                } else {
                    KeywordCategoryMapping mapping = new KeywordCategoryMapping();
                    mapping.setKeyword(keyword);
                    mapping.setCategory(category);
                    keywordCategoryMappingRepository.save(mapping);
                }

                // Handle Product
                Optional<Product> existingProduct = productRepository.findByName(keyword);
                if (existingProduct.isPresent()) {
                    Product product = existingProduct.get();
                    product.setCategory(category);
                    productRepository.save(product);
                } else {
                    Product product = new Product();
                    product.setName(keyword);
                    product.setCategory(category);
                    productRepository.save(product);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}