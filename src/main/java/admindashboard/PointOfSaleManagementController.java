package admindashboard;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import entities.Commande;
import entities.Panier;
import entities.Produit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import services.CommandeService;
import services.PanierService;
import services.ProduitService;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import javafx.scene.layout.HBox;
import java.util.stream.Collectors;

public class PointOfSaleManagementController implements Initializable {
    @FXML private JFXTextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private FlowPane productsGrid;
    @FXML private ListView<Panier> cartItems;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;
    @FXML private JFXButton checkoutBtn;

    private ProduitService produitService;
    private PanierService panierService;
    private CommandeService commandeService;
    private ObservableList<Panier> cartList;
    private static final int CURRENT_USER_ID = 1; // TODO: Get from authentication

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        produitService = new ProduitService();
        panierService = new PanierService();
        commandeService = new CommandeService();
        cartList = FXCollections.observableArrayList();

        setupSearchAndFilter();
        setupCartList();
        loadProducts();
        updateTotals();
    }

    private void setupSearchAndFilter() {
        // Setup category filter
        try {
            List<String> categories = produitService.afficher().stream()
                .map(Produit::getCategory)
                .distinct()
                .collect(Collectors.toList());
            categoryFilter.getItems().add("All Categories");
            categoryFilter.getItems().addAll(categories);
            categoryFilter.setValue("All Categories");
            categoryFilter.setOnAction(e -> loadProducts());
        } catch (SQLException e) {
            showError("Error loading categories: " + e.getMessage());
        }

        // Setup search field
        searchField.textProperty().addListener((obs, old, newValue) -> loadProducts());
    }

    private void setupCartList() {
        cartItems.setItems(cartList);
        cartItems.setCellFactory(lv -> new ListCell<Panier>() {
            @Override
            protected void updateItem(Panier item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox container = new HBox(10);
                    container.setAlignment(Pos.CENTER_LEFT);

                    // Get the product for this cart item
                    try {
                        Produit product = produitService.getOne(item.getProductId());
                        Label nameLabel = new Label(product.getName());
                        Label quantityLabel = new Label("x" + item.getQuantity());
                        Label priceLabel = new Label(String.format("%.2f TND", item.getUnitPrice() * item.getQuantity()));
                        Button removeBtn = new Button("X");
                        
                        removeBtn.setOnAction(e -> {
                            cartList.remove(item);
                            updateTotals();
                        });

                        container.getChildren().addAll(nameLabel, quantityLabel, priceLabel, removeBtn);
                    } catch (SQLException e) {
                        Label errorLabel = new Label("Error loading product");
                        container.getChildren().add(errorLabel);
                    }
                    setGraphic(container);
                }
            }
        });
    }

    private void loadProducts() {
        try {
            productsGrid.getChildren().clear();
            String searchTerm = searchField.getText();
            String category = categoryFilter.getValue();
            
            List<Produit> products;
            if (category == null || category.equals("All Categories")) {
                products = produitService.afficher();
            } else {
                products = produitService.findByCategory(category);
            }

            for (Produit product : products) {
                if (searchTerm != null && !searchTerm.isEmpty() && 
                    !product.getName().toLowerCase().contains(searchTerm.toLowerCase())) {
                    continue;
                }

                VBox productCard = createProductCard(product);
                productsGrid.getChildren().add(productCard);
            }
        } catch (SQLException e) {
            showError("Error loading products: " + e.getMessage());
        }
    }

    private VBox createProductCard(Produit product) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        card.setAlignment(Pos.CENTER);

        Label nameLabel = new Label(product.getName());
        nameLabel.getStyleClass().add("product-name");

        Label priceLabel = new Label(String.format("%.2f TND", product.getPrice()));
        priceLabel.getStyleClass().add("product-price");

        Label stockLabel = new Label("In Stock: " + product.getStock());
        stockLabel.getStyleClass().add("product-stock");

        Spinner<Integer> quantitySpinner = new Spinner<>(1, product.getStock(), 1);
        quantitySpinner.setEditable(true);

        JFXButton addButton = new JFXButton("Add to Cart");
        addButton.getStyleClass().add("action-button");
        addButton.setOnAction(e -> addToCart(product, quantitySpinner.getValue()));

        card.getChildren().addAll(nameLabel, priceLabel, stockLabel, quantitySpinner, addButton);
        return card;
    }

    private void addToCart(Produit product, int quantity) {
        if (quantity > product.getStock()) {
            showError("Not enough stock available!");
            return;
        }

        // Check if product already in cart
        for (Panier item : cartList) {
            if (item.getProductId() == product.getId()) {
                int newQuantity = item.getQuantity() + quantity;
                if (newQuantity > product.getStock()) {
                    showError("Not enough stock available!");
                    return;
                }
                item.setQuantity(newQuantity);
                cartItems.refresh();
                updateTotals();
                return;
            }
        }

        // Add new item to cart
        Panier cartItem = new Panier(0, CURRENT_USER_ID, product.getId(), quantity, product.getPrice());
        cartList.add(cartItem);
        updateTotals();
    }

    private void updateTotals() {
        double subtotal = cartList.stream()
            .mapToDouble(Panier::getSubtotal)
            .sum();
        
        double tax = subtotal * 0.20; // 20% tax
        double total = subtotal + tax;

        subtotalLabel.setText(String.format("%.2f TND", subtotal));
        taxLabel.setText(String.format("%.2f TND", tax));
        totalLabel.setText(String.format("%.2f TND", total));
    }

    @FXML
    private void handleCheckout() {
        if (cartList.isEmpty()) {
            showError("Cart is empty!");
            return;
        }

        try {
            // Create order from cart items
            Commande order = new Commande();
            for (Panier item : cartList) {
                order.addItem(item);
            }

            // Save order and update stock
            commandeService.ajouter(order);
            
            // Clear cart
            cartList.clear();
            updateTotals();
            
            showInfo("Order completed successfully!");
        } catch (SQLException e) {
            showError("Error processing order: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
