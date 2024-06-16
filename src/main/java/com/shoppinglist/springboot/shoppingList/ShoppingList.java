package com.shoppinglist.springboot.shoppingList;

import jakarta.persistence.*;

import java.util.*;

import com.shoppinglist.springboot.user.User;

@Entity
@Table(name = "shopping_lists")
public class ShoppingList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "shoppingList", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShoppingListItem> items = new ArrayList<>();

    @Column(name = "status")
    private String status;
    // Gettery
    public Long getId() {
        return id;
    }
    public String getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public User getUser() {
        return user;
    }

    public List<ShoppingListItem> getItems() {
        return items;
    }

    // Settery
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setItems(List<ShoppingListItem> items) {
        this.items = items;
    }
    public void setStatus(String status) {
        // Validate if the status is one of the allowed values
        if (!isValidStatus(status)) {
            throw new IllegalArgumentException("Invalid status value");
        }
        this.status = status;
    }

    // Helper method to validate status
    private boolean isValidStatus(String status) {
        return status != null && (status.equals("Active") || status.equals("Trash") || status.equals("Deleted"));
    }
}