package com.shoppinglist.springboot.shoppingList;

import com.shoppinglist.springboot.keywordMapping.KeywordCategoryMapping;
import com.shoppinglist.springboot.keywordMapping.KeywordCategoryMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    @Autowired
    private KeywordCategoryMappingRepository keywordCategoryMappingRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Optional<Product> findProductById(Long id) {
        return productRepository.findById(id);
    }

    public Optional<Product> findProductByName(String productName) {
        return productRepository.findByName(productName);
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public String resolveCategoryForProduct(String productName) {
        List<KeywordCategoryMapping> mappings = keywordCategoryMappingRepository.findAll();

        for (KeywordCategoryMapping mapping : mappings) {
            if (productName.toLowerCase().contains(mapping.getKeyword().toLowerCase())) {
                return mapping.getCategory();
            }
        }
        return "Inne";
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

}