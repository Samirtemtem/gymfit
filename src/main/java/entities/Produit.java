package entities;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Produit {
    // Valid categories
    public static final Set<String> VALID_CATEGORIES = new HashSet<>(Arrays.asList(
        "EQUIPMENT", "ACCESSORIES", "CLOTHING", "FOOD", "OTHER"
    ));

    private int id;
    private String name;
    private String description;
    private float price;
    private int stock;
    private String category;

    public Produit() {
    }

    public Produit(int id, String name, String description, float price, int stock, String category) {
        setId(id);
        setName(name);
        setDescription(description);
        setPrice(price);
        setStock(stock);
        setCategory(category);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("ID cannot be negative");
        }
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        this.name = name.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        this.description = description.trim();
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        this.stock = stock;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        if (category == null || !VALID_CATEGORIES.contains(category.toUpperCase())) {
            throw new IllegalArgumentException("Invalid category. Valid values are: " + VALID_CATEGORIES);
        }
        this.category = category.toUpperCase();
    }

    // Stock management methods
    public boolean hasEnoughStock(int requestedQuantity) {
        return stock >= requestedQuantity;
    }

    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (!hasEnoughStock(quantity)) {
            throw new IllegalStateException("Not enough stock available");
        }
        this.stock -= quantity;
    }

    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.stock += quantity;
    }

    @Override
    public String toString() {
        return "Produit{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", category='" + category + '\'' +
                '}';
    }
}
