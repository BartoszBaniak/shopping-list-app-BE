package com.shoppinglist.springboot.shoppingList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {
    Optional<ShoppingList> findById(Long id);
    List<ShoppingList> findByUserId(String userId);
    List<ShoppingList> findByUserIdAndStatus(String userId, Status status);
}