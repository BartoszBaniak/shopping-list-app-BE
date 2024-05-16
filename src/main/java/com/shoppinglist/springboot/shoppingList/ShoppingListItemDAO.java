package com.shoppinglist.springboot.shoppingList;

import java.util.Optional;

public interface ShoppingListItemDAO {
    void addShoppingListItem(ShoppingListItem item);

    Optional<ShoppingListItem> getShoppingListItemById(Long id);

    void updateShoppingListItem(ShoppingListItem item);

    void deleteShoppingListItemById(Long id);
}