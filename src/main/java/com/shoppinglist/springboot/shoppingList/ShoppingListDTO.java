package com.shoppinglist.springboot.shoppingList;

public class ShoppingListDTO {
    private Long id;
    private String name;
    private String status; // Dodaj pole status

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    // Dodaj gettery i settery dla innych pól według potrzeb
}