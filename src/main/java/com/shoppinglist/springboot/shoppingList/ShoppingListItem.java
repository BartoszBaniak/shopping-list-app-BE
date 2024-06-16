package com.shoppinglist.springboot.shoppingList;

import jakarta.persistence.Entity;
import jakarta.persistence.*;

@Entity
public class ShoppingListItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "shopping_list_id", nullable = false)
    private ShoppingList shoppingList;

    private boolean purchased;

    private int quantity; // Dodane pole quantity

    // Gettery
    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public ShoppingList getShoppingList() {
        return shoppingList;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public int getQuantity() { // Getter dla quantity
        return quantity;
    }

    // Settery
    public void setId(Long id) {
        this.id = id;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setShoppingList(ShoppingList shoppingList) {
        this.shoppingList = shoppingList;
    }

    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }

    public void setQuantity(int quantity) { // Setter dla quantity
        this.quantity = quantity;
    }
}