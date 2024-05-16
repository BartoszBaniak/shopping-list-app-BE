package com.shoppinglist.springboot.shoppingList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShoppingListJPADataAccessService implements ShoppingListDAO {

    @Autowired
    private ShoppingListRepository shoppingListRepository;

    @Override
    public void addShoppingList(ShoppingList shoppingList) {
        shoppingListRepository.save(shoppingList);
    }

    @Override
    public Optional<ShoppingList> getShoppingListById(Long id) {
        return shoppingListRepository.findById(id);
    }

    @Override
    public void updateShoppingList(ShoppingList shoppingList) {
        shoppingListRepository.save(shoppingList);
    }

    @Override
    public void deleteShoppingListById(Long id) {
        shoppingListRepository.deleteById(id);
    }

    @Override
    public List<ShoppingList> getAllShoppingLists() {
        return shoppingListRepository.findAll();
    }
}