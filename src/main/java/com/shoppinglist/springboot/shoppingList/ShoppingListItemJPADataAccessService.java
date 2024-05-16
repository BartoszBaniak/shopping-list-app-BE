package com.shoppinglist.springboot.shoppingList;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ShoppingListItemJPADataAccessService implements ShoppingListItemDAO {

    @Autowired
    private ShoppingListItemRepository shoppingListItemRepository;

    @Override
    public void addShoppingListItem(ShoppingListItem item) {
        shoppingListItemRepository.save(item);
    }

    @Override
    public Optional<ShoppingListItem> getShoppingListItemById(Long id) {
        return shoppingListItemRepository.findById(id);
    }

    @Override
    public void updateShoppingListItem(ShoppingListItem item) {
        shoppingListItemRepository.save(item);
    }

    @Override
    public void deleteShoppingListItemById(Long id) {
        shoppingListItemRepository.deleteById(id);
    }
}