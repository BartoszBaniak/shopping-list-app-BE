package com.shoppinglist.springboot.shoppingList;



import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "ProduktZIloscia")
public class ProduktZIloscia implements Comparable<ProduktZIloscia> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "Produkt_id")
    private Produkt produkt;

    private Integer quantity;

    public ProduktZIloscia() {
    }

    public ProduktZIloscia(Produkt produkt, int quantity) {
        this.produkt = produkt;
        this.quantity = quantity;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Produkt getIngredient() {
        return produkt;
    }

    public void setIngredient(Produkt produkt) {
        this.produkt = produkt;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProduktZIloscia that = (ProduktZIloscia) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ProduktZIloscia{" +
                "id=" + id +
                ", Produkt=" + produkt +
                ", ilosc=" + quantity +
                '}';
    }

    @Override
    public int compareTo(ProduktZIloscia produktZIloscia) {
        return this.produkt.getName().compareToIgnoreCase(produktZIloscia.produkt.getName());
    }
}