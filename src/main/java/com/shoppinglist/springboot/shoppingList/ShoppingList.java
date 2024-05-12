package com.shoppinglist.springboot.shoppingList;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "shopping_lists")
public class ShoppingList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "shopping_list_id")
    private Set<ProduktZIloscia> Produkty = new HashSet<>();

    public void addProduktZIloscia(ProduktZIloscia produkty) {
        Produkty.add(produkty);
    }

    public void removeProduktZIloscia(ProduktZIloscia produkty) {
        Produkty.remove(produkty);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<ProduktZIloscia> getProdukty() {
        return new HashSet<>(Produkty);
    }

    public void setProdukty(Set<ProduktZIloscia> produkty) {
        this.Produkty = produkty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShoppingList that = (ShoppingList) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ShoppingList{" +
                "id=" + id +
                '}';
    }
}