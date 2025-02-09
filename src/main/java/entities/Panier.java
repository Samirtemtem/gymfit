package entities;

import java.time.LocalDateTime;

public class Panier {
    private int id;
    private int userId;
    private int productId;
    private int quantity;
    private LocalDateTime addedDate;
    private float unitPrice; // Store current price when adding to cart
    private float subtotal; // quantity * unitPrice

    public Panier() {
        this.addedDate = LocalDateTime.now();
    }

    public Panier(int id, int userId, int productId, int quantity, float unitPrice) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.addedDate = LocalDateTime.now();
        calculateSubtotal();
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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        if (userId < 0) {
            throw new IllegalArgumentException("User ID cannot be negative");
        }
        this.userId = userId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        if (productId < 0) {
            throw new IllegalArgumentException("Product ID cannot be negative");
        }
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantity = quantity;
        calculateSubtotal();
    }

    public LocalDateTime getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(LocalDateTime addedDate) {
        if (addedDate == null) {
            throw new IllegalArgumentException("Added date cannot be null");
        }
        this.addedDate = addedDate;
    }

    public float getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(float unitPrice) {
        if (unitPrice < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        this.unitPrice = unitPrice;
        calculateSubtotal();
    }

    public float getSubtotal() {
        return subtotal;
    }

    private void calculateSubtotal() {
        this.subtotal = this.quantity * this.unitPrice;
    }

    @Override
    public String toString() {
        return "Panier{" +
                "id=" + id +
                ", userId=" + userId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", addedDate=" + addedDate +
                ", unitPrice=" + unitPrice +
                ", subtotal=" + subtotal +
                '}';
    }
}
