package com.shoppinglist.springboot.shoppingList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.shoppinglist.springboot.keywordMapping.KeywordCategoryMappingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@Service
public class ShoppingListService {

    private final ShoppingListItemRepository shoppingListItemRepository;
    private final ProductService productService;
    private final ShoppingListRepository shoppingListRepository;
    private final KeywordCategoryMappingRepository keywordCategoryMappingRepository;

    @Autowired
    public ShoppingListService(ShoppingListItemRepository shoppingListItemRepository, ProductService productService, ShoppingListRepository shoppingListRepository, KeywordCategoryMappingRepository keywordCategoryMappingRepository) {
        this.shoppingListItemRepository = shoppingListItemRepository;
        this.productService = productService;
        this.shoppingListRepository = shoppingListRepository;
        this.keywordCategoryMappingRepository = keywordCategoryMappingRepository;
    }

    public ShoppingList createShoppingList(ShoppingList shoppingList) {
        return shoppingListRepository.save(shoppingList);
    }

    @Transactional
    public void saveShoppingList(ShoppingList shoppingList, Map<String, Integer> productQuantities) {
        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            String productName = entry.getKey();
            Integer quantity = entry.getValue();

            Optional<Product> optionalProduct = productService.findProductByName(productName);
            Product product;
            if (optionalProduct.isPresent()) {
                product = optionalProduct.get();
            } else {
                product = new Product();
                product.setName(productName);
                String category = productService.resolveCategoryForProduct(productName);
                product.setCategory(category);
                productService.saveProduct(product);
            }

            ShoppingListItem newItem = new ShoppingListItem();
            newItem.setProduct(product);
            newItem.setShoppingList(shoppingList);
            newItem.setPurchased(false);
            newItem.setQuantity(quantity);
            newItem.setProductName(productName); // Set product name

            shoppingList.getItems().add(newItem);
        }

        shoppingListRepository.save(shoppingList);
    }

    public Optional<ShoppingList> findShoppingListById(Long id) {
        return shoppingListRepository.findById(id);
    }

    @Transactional
    public void updateProductQuantities(ShoppingList shoppingList, Map<String, Integer> productQuantities) throws Exception {
        List<ShoppingListItem> items = shoppingList.getItems();
        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            String productName = entry.getKey();
            Integer quantity = entry.getValue();

            // Find the corresponding ShoppingListItem by productName
            Optional<ShoppingListItem> optionalItem = items.stream()
                    .filter(item -> item.getProductName().equals(productName))
                    .findFirst();

            if (optionalItem.isPresent()) {
                ShoppingListItem item = optionalItem.get();
                item.setQuantity(quantity);
            } else {
                throw new Exception("Product '" + productName + "' not found in shopping list");
            }
        }

        shoppingListRepository.save(shoppingList);
    }

    @Transactional
    public void deleteProductsFromList(ShoppingList shoppingList, List<String> productNames) throws Exception {
        // Pobierz listę elementów
        List<ShoppingListItem> items = shoppingList.getItems();

        // Usuń produkty, które są na liście productNames
        boolean found = items.removeIf(item -> productNames.contains(item.getProduct().getName()));

        if (!found) {
            throw new Exception("One or more products specified for deletion are not present in the shopping list");
        }

        // Zapisz zaktualizowaną listę zakupów
        shoppingListRepository.save(shoppingList);
    }
    @Transactional
    public void deleteShoppingList(ShoppingList shoppingList) {
        shoppingListRepository.delete(shoppingList);
    }
    public List<ShoppingListDTO> findShoppingListsByUserId(String userId) {
        List<ShoppingList> shoppingLists = shoppingListRepository.findByUserId(userId);
        List<ShoppingListDTO> shoppingListDTOs = new ArrayList<>();
        for (ShoppingList shoppingList : shoppingLists) {
            ShoppingListDTO shoppingListDTO = new ShoppingListDTO();
            shoppingListDTO.setId(shoppingList.getId());
            shoppingListDTO.setName(shoppingList.getName());
            // Ustaw inne pola według potrzeb
            shoppingListDTOs.add(shoppingListDTO);
        }
        return shoppingListDTOs;
    }
    public List<ShoppingListItem> findAllItemsByShoppingListId(Long shoppingListId) {
        List<ShoppingListItem> items = shoppingListItemRepository.findAllByShoppingListId(shoppingListId);
        for (ShoppingListItem item : items) {
            Product product = item.getProduct();
            item.setProductName(product.getName());
        }
        return items;
    }
    @Transactional
    public void updateShoppingListName(ShoppingList shoppingList) {
        shoppingListRepository.save(shoppingList);
    }


}