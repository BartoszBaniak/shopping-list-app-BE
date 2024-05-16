package com.shoppinglist.springboot.shoppingList;

import java.util.List;
import java.util.Optional;

public interface ShoppingListDAO {
    void addShoppingList(ShoppingList shoppingList);

    Optional<ShoppingList> getShoppingListById(Long id);

    void updateShoppingList(ShoppingList shoppingList);

    void deleteShoppingListById(Long id);

    List<ShoppingList> getAllShoppingLists();
}