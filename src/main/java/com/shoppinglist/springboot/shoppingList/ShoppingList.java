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

    @Enumerated(EnumType.STRING)
    private Status status;

    // Gettery
    public Long getId() {
        return id;
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

    public Status getStatus() {
        return status;
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

    public void setStatus(Status status) {
        this.status = status;
    }
}
