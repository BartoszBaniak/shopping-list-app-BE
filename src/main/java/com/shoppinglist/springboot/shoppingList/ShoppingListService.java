package com.shoppinglist.springboot.shoppingList;

import com.shoppinglist.springboot.keywordMapping.KeywordCategoryMapping;
import com.shoppinglist.springboot.keywordMapping.KeywordCategoryMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShoppingListService {

    @Autowired
    private ShoppingListDAO shoppingListDAO;

    @Autowired
    private ShoppingListItemDAO shoppingListItemDAO;
    private final KeywordCategoryMappingRepository keywordCategoryMappingRepository;

    public ShoppingListService(ShoppingListDAO shoppingListDAO, ShoppingListItemDAO shoppingListItemDAO, KeywordCategoryMappingRepository keywordCategoryMappingRepository) {
        this.shoppingListDAO = shoppingListDAO;
        this.shoppingListItemDAO = shoppingListItemDAO;
        this.keywordCategoryMappingRepository = keywordCategoryMappingRepository;
    }

    public List<ShoppingList> getAllShoppingLists() {
        return shoppingListDAO.getAllShoppingLists();
    }

    public Optional<ShoppingList> getShoppingListById(Long id) {
        return shoppingListDAO.getShoppingListById(id);
    }

    public ShoppingList saveShoppingList(ShoppingList shoppingList) {
        shoppingListDAO.addShoppingList(shoppingList);
        return shoppingList;
    }

    public Optional<ShoppingList> updateShoppingList(Long id, ShoppingList shoppingListDetails) {
        return shoppingListDAO.getShoppingListById(id).map(existingShoppingList -> {
            existingShoppingList.setName(shoppingListDetails.getName());
            existingShoppingList.setItems(shoppingListDetails.getItems());
            shoppingListDAO.updateShoppingList(existingShoppingList);
            return existingShoppingList;
        });
    }

    public boolean deleteShoppingList(Long id) {
        if (shoppingListDAO.getShoppingListById(id).isPresent()) {
            shoppingListDAO.deleteShoppingListById(id);
            return true;
        } else {
            return false;
        }
    }

    public Optional<ShoppingList> addProductToShoppingList(Long id, ShoppingListItem item) {
        String productName = item.getProduct().getName();

        // Znajdź kategorię na podstawie nazwy produktu w bazie danych
        Optional<KeywordCategoryMapping> mappingOptional = keywordCategoryMappingRepository.findByKeyword(productName.toLowerCase());
        String categoryName = mappingOptional.map(KeywordCategoryMapping::getCategory).orElse("Inne");

        Category category = new Category();
        category.setName(categoryName);
        item.getProduct().setCategory(category);

        return shoppingListDAO.getShoppingListById(id).map(existingShoppingList -> {
            item.setShoppingList(existingShoppingList);
            shoppingListItemDAO.addShoppingListItem(item);
            existingShoppingList.getItems().add(item);
            shoppingListDAO.updateShoppingList(existingShoppingList);
            return existingShoppingList;
        });
    }

}