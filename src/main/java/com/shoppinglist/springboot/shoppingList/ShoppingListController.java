package com.shoppinglist.springboot.shoppingList;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/shoppingLists")
public class ShoppingListController {

    @Autowired
    private ShoppingListService shoppingListService;

    @GetMapping
    public List<ShoppingList> getAllShoppingLists() {
        return shoppingListService.getAllShoppingLists();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShoppingList> getShoppingListById(@PathVariable Long id) {
        Optional<ShoppingList> shoppingList = shoppingListService.getShoppingListById(id);
        if (shoppingList.isPresent()) {
            return ResponseEntity.ok(shoppingList.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ShoppingList createShoppingList(@RequestBody ShoppingList shoppingList) {
        return shoppingListService.saveShoppingList(shoppingList);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShoppingList> updateShoppingList(@PathVariable Long id, @RequestBody ShoppingList shoppingListDetails) {
        Optional<ShoppingList> updatedShoppingList = shoppingListService.updateShoppingList(id, shoppingListDetails);
        if (updatedShoppingList.isPresent()) {
            return ResponseEntity.ok(updatedShoppingList.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShoppingList(@PathVariable Long id) {
        if (shoppingListService.deleteShoppingList(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<ShoppingList> addProductToShoppingList(@PathVariable Long id, @RequestBody ShoppingListItem item) {
        Optional<ShoppingList> shoppingList = shoppingListService.addProductToShoppingList(id, item);
        if (shoppingList.isPresent()) {
            return ResponseEntity.ok(shoppingList.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}