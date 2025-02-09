package entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents an order in the equitation center system.
 * This class handles order information including user, date, status, total amount and payment method.
 */
public class Commande {
    // Valid status values
    public static final Set<String> VALID_STATUSES = new HashSet<>(Arrays.asList(
        "PENDING", "CONFIRMED", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED"
    ));
    
    // Valid payment methods
    public static final Set<String> VALID_PAYMENT_METHODS = new HashSet<>(Arrays.asList(
        "CASH", "CREDIT_CARD", "BANK_TRANSFER"
    ));

    private int id;
    private int userId;
    private LocalDateTime orderDate;
    private String status;
    public float total;
    private String paymentMethod;
    private List<CommandeItem> items;

    // Inner class to represent order items
    public static class CommandeItem {
        private int productId;
        private int quantity;
        private float unitPrice;
        private float subtotal;

        public CommandeItem(int productId, int quantity, float unitPrice) {
            this.productId = productId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.subtotal = quantity * unitPrice;
        }

        // Getters and setters
        public int getProductId() { return productId; }
        public int getQuantity() { return quantity; }
        public float getUnitPrice() { return unitPrice; }
        public float getSubtotal() { return subtotal; }
    }

    public Commande() {
        this.orderDate = LocalDateTime.now();
        this.status = "PENDING";
        this.items = new ArrayList<>();
    }

    public Commande(int id, int userId, String paymentMethod) {
        this();
        setId(id);
        setUserId(userId);
        setPaymentMethod(paymentMethod);
    }

    // Add an item to the order
    public void addItem(Panier cartItem) {
        CommandeItem item = new CommandeItem(
            cartItem.getProductId(),
            cartItem.getQuantity(),
            cartItem.getUnitPrice()
        );
        items.add(item);
        calculateTotal();
    }

    // Remove an item from the order
    public void removeItem(int productId) {
        items.removeIf(item -> item.getProductId() == productId);
        calculateTotal();
    }

    // Calculate order total
    private void calculateTotal() {
        this.total = 0;
        for (CommandeItem item : items) {
            this.total += item.getSubtotal();
        }
    }

    // Standard getters and setters
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

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        if (orderDate == null) {
            throw new IllegalArgumentException("Order date cannot be null");
        }
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status == null || !VALID_STATUSES.contains(status.toUpperCase())) {
            throw new IllegalArgumentException("Invalid status. Valid values are: " + VALID_STATUSES);
        }
        this.status = status.toUpperCase();
    }

    public float getTotal() {
        return total;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        if (paymentMethod == null || !VALID_PAYMENT_METHODS.contains(paymentMethod.toUpperCase())) {
            throw new IllegalArgumentException("Invalid payment method. Valid values are: " + VALID_PAYMENT_METHODS);
        }
        this.paymentMethod = paymentMethod.toUpperCase();
    }

    public List<CommandeItem> getItems() {
        return new ArrayList<>(items); // Return a copy to prevent external modification
    }

    @Override
    public String toString() {
        return "Commande{" +
                "id=" + id +
                ", userId=" + userId +
                ", orderDate=" + orderDate +
                ", status='" + status + '\'' +
                ", total=" + total +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", items=" + items.size() +
                '}';
    }
}
