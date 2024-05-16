package com.shoppinglist.springboot.shoppingList;

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
        return shoppingListDAO.getShoppingListById(id).map(existingShoppingList -> {
            item.setShoppingList(existingShoppingList);
            shoppingListItemDAO.addShoppingListItem(item);
            existingShoppingList.getItems().add(item);
            shoppingListDAO.updateShoppingList(existingShoppingList);
            return existingShoppingList;
        });
    }
}