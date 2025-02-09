package admindashboard;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import entities.Commande;
import entities.Panier;
import entities.Produit;
import entities.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import services.CommandeService;
import services.PanierService;
import services.ProduitService;
import services.UtilisateurService;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PointOfSaleManagementController implements Initializable {
    @FXML private JFXTextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private FlowPane productsGrid;
    @FXML private VBox cartItemsContainer;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;
    @FXML private JFXButton checkoutBtn;
    @FXML private JFXButton refreshBtn;
    @FXML private JFXButton addProductBtn;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private VBox recentOrdersContainer;
    @FXML private JFXTextField orderSearchField;
    @FXML private ComboBox<String> orderStatusFilter;
    @FXML private Label cartUserLabel;

    private ProduitService produitService;
    private PanierService panierService;
    private CommandeService commandeService;
    private UtilisateurService utilisateurService;
    private ObservableList<Panier> cartList;
    private static final int CURRENT_USER_ID = 1; // TODO: Get from authentication
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private Utilisateur currentCartUser;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        produitService = new ProduitService();
        panierService = new PanierService();
        commandeService = new CommandeService();
        utilisateurService = new UtilisateurService();
        cartList = FXCollections.observableArrayList();
        currentCartUser = null;

        setupControls();
        
        // Load user's cart from database
        try {
            cartList.addAll(panierService.getUserCart(CURRENT_USER_ID));
        } catch (SQLException e) {
            showError("Error loading cart: " + e.getMessage());
        }
        
        loadInitialData();
    }

    private void setupControls() {
        paymentMethodCombo.getItems().addAll("CASH", "CREDIT_CARD", "DEBIT_CARD", "MOBILE_PAYMENT");
        paymentMethodCombo.setValue("CASH");
        
        searchField.textProperty().addListener((obs, old, newValue) -> loadProducts());
        
        orderSearchField.textProperty().addListener((obs, old, newValue) -> loadOrders());
        
        orderStatusFilter.getItems().addAll("All", "PENDING", "PROCESSING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED");
        orderStatusFilter.setValue("All");
        orderStatusFilter.setOnAction(e -> loadOrders());
        
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
        
        refreshBtn.setOnAction(e -> {
            loadProducts();
            loadOrders();
        });
        
        addProductBtn.setOnAction(e -> showAddProductDialog());
        
        checkoutBtn.setOnAction(e -> handleCheckout());
    }

    private void loadInitialData() {
        loadProducts();
        loadOrders();
        updateTotals();
    }

    private void loadProducts() {
        try {
            productsGrid.getChildren().clear();
            String searchTerm = searchField.getText().toLowerCase();
            String category = categoryFilter.getValue();
            
            List<Produit> products;
            if (category == null || category.equals("All Categories")) {
                products = produitService.afficher();
            } else {
                products = produitService.findByCategory(category);
            }
            
            if (searchTerm != null && !searchTerm.isEmpty()) {
                products = products.stream()
                    .filter(p -> p.getName().toLowerCase().contains(searchTerm))
                    .collect(Collectors.toList());
            }
            
            for (Produit product : products) {
                productsGrid.getChildren().add(createProductCard(product));
            }
        } catch (SQLException e) {
            showError("Error loading products: " + e.getMessage());
        }
    }

    private VBox createProductCard(Produit product) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("product-card", "card");
        card.setAlignment(Pos.CENTER);

        Label nameLabel = new Label(product.getName());
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);

        Label priceLabel = new Label(String.format("%.2f TND", product.getPrice()));
        priceLabel.getStyleClass().add("price-label");

        Label stockLabel = new Label("Stock: " + product.getStock());
        
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        
        JFXButton addButton = new JFXButton("Add to Cart");
        addButton.getStyleClass().add("action-button");
        addButton.setOnAction(e -> showAddToCartDialog(product));

        JFXButton editButton = new JFXButton("");
        editButton.getStyleClass().addAll("action-button", "edit-button");
        editButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.EDIT));
        editButton.setOnAction(e -> showEditProductDialog(product));

        JFXButton deleteButton = new JFXButton("");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.TRASH));
        deleteButton.setOnAction(e -> handleDeleteProduct(product));

        buttonsBox.getChildren().addAll(addButton, editButton, deleteButton);
        card.getChildren().addAll(nameLabel, priceLabel, stockLabel, buttonsBox);
        return card;
    }

    private void loadOrders() {
        try {
            recentOrdersContainer.getChildren().clear();
            List<Commande> orders = commandeService.afficher();
            
            String searchTerm = orderSearchField.getText().toLowerCase();
            String statusFilter = orderStatusFilter.getValue();
            
            if (searchTerm != null && !searchTerm.isEmpty()) {
                orders = orders.stream()
                    .filter(o -> String.valueOf(o.getId()).contains(searchTerm))
                    .collect(Collectors.toList());
            }
            
            if (statusFilter != null && !statusFilter.equals("All")) {
                orders = orders.stream()
                    .filter(o -> o.getStatus().equals(statusFilter))
                    .collect(Collectors.toList());
            }
            
            for (Commande order : orders) {
                recentOrdersContainer.getChildren().add(createOrderCard(order));
            }
        } catch (SQLException e) {
            showError("Error loading orders: " + e.getMessage());
        }
    }

    private VBox createOrderCard(Commande order) {
        VBox orderCard = new VBox(5);
        orderCard.getStyleClass().add("order-card");
        
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label orderIdLabel = new Label("#" + order.getId());
        orderIdLabel.getStyleClass().add("order-id");
        
        Label dateLabel = new Label(order.getOrderDate().format(DATE_FORMATTER));
        
        Label statusLabel = new Label(order.getStatus());
        statusLabel.getStyleClass().addAll("order-status", order.getStatus().toLowerCase());
        
        Label totalLabel = new Label(String.format("%.2f TND", order.getTotal()));
        totalLabel.getStyleClass().add("total-value");
        
        // Add edit status button
        JFXButton editStatusBtn = new JFXButton("");
        editStatusBtn.getStyleClass().addAll("action-button", "edit-button");
        editStatusBtn.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.EDIT));
        editStatusBtn.setOnAction(e -> showEditStatusDialog(order, statusLabel));
        
        headerBox.getChildren().addAll(orderIdLabel, dateLabel, new Region(), statusLabel, editStatusBtn, totalLabel);
        HBox.setHgrow(headerBox.getChildren().get(2), Priority.ALWAYS);
        
        HBox actionsBox = new HBox(10);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);
        
        JFXButton viewBtn = new JFXButton("View Details");
        viewBtn.getStyleClass().add("action-button");
        viewBtn.setOnAction(e -> showOrderDetails(order));
        
        JFXButton printBtn = new JFXButton("Print");
        printBtn.getStyleClass().add("action-button");
        printBtn.setOnAction(e -> printOrder(order));
        
        actionsBox.getChildren().addAll(viewBtn, printBtn);
        
        orderCard.getChildren().addAll(headerBox, actionsBox);
        return orderCard;
    }

    private void showEditStatusDialog(Commande order, Label statusLabel) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Update Order Status");
        dialog.getDialogPane().getStyleClass().add("custom-dialog");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("PENDING", "PROCESSING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED");
        statusCombo.setValue(order.getStatus());
        statusCombo.getStyleClass().add("dialog-combo");
        
        grid.add(new Label("Status:"), 0, 0);
        grid.add(statusCombo, 1, 0);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return statusCombo.getValue();
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(newStatus -> {
            try {
                order.setStatus(newStatus);
                commandeService.modifier(order);
                statusLabel.setText(newStatus);
                statusLabel.getStyleClass().removeAll("pending", "processing", "confirmed", "shipped", "delivered", "cancelled");
                statusLabel.getStyleClass().add(newStatus.toLowerCase());
                showInfo("Order status updated successfully!");
            } catch (SQLException e) {
                showError("Error updating order status: " + e.getMessage());
            }
        });
    }

    private void updateCartDisplay() {
        cartItemsContainer.getChildren().clear();
        for (Panier item : cartList) {
            try {
                Produit product = produitService.getOne(item.getProductId());
                if (product == null) {
                    // Product was deleted, remove from cart
                    cartList.remove(item);
                    panierService.supprimer(item.getId());
                    continue;
                }

                HBox cartItemCard = new HBox(10);
                cartItemCard.getStyleClass().add("cart-item-card");
                cartItemCard.setAlignment(Pos.CENTER_LEFT);
                
                VBox infoBox = new VBox(5);
                Label nameLabel = new Label(product.getName());
                Label priceLabel = new Label(String.format("%.2f TND", item.getSubtotal()));
                infoBox.getChildren().addAll(nameLabel, priceLabel);
                
                Spinner<Integer> quantitySpinner = new Spinner<>(1, product.getStock(), item.getQuantity());
                quantitySpinner.setMaxWidth(80);
                quantitySpinner.valueProperty().addListener((obs, old, newValue) -> {
                    try {
                        item.setQuantity(newValue);
                        panierService.modifier(item);
                        updateTotals();
                        priceLabel.setText(String.format("%.2f TND", item.getSubtotal()));
                    } catch (SQLException e) {
                        showError("Error updating quantity: " + e.getMessage());
                        quantitySpinner.getValueFactory().setValue(old);
                    }
                });
                
                JFXButton removeBtn = new JFXButton("×");
                removeBtn.getStyleClass().add("remove-button");
                removeBtn.setOnAction(e -> {
                    try {
                        panierService.supprimer(item.getId());
                        cartList.remove(item);
                        updateCartDisplay();
                        updateTotals();
                    } catch (SQLException ex) {
                        showError("Error removing item: " + ex.getMessage());
                    }
                });

                cartItemCard.getChildren().addAll(infoBox, new Region(), quantitySpinner, removeBtn);
                HBox.setHgrow(infoBox, Priority.ALWAYS);
                
                cartItemsContainer.getChildren().add(cartItemCard);
            } catch (SQLException e) {
                showError("Error loading cart item: " + e.getMessage());
            }
        }
    }

    private void updateTotals() {
        double subtotal = cartList.stream()
            .mapToDouble(Panier::getSubtotal)
            .sum();
        
        double tax = subtotal * 0.19; // 19% TVA
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

        if (paymentMethodCombo.getValue() == null) {
            showError("Please select a payment method!");
            return;
        }

        try {
            // Verify all products and stock before proceeding
            for (Panier item : cartList) {
                Produit product = produitService.getOne(item.getProductId());
                if (product == null) {
                    showError("Product #" + item.getProductId() + " no longer exists!");
                    return;
                }
                if (product.getStock() < item.getQuantity()) {
                    showError("Insufficient stock for " + product.getName() + "!\nAvailable: " + product.getStock() + ", Requested: " + item.getQuantity());
                    return;
                }
            }

            // Create and process the order
            Commande order = commandeService.createOrderFromCart(currentCartUser.getId(), paymentMethodCombo.getValue());
            
            // Show success message with order details
            showInfo(String.format("Order #%d completed successfully!\nTotal Amount: %.2f TND", 
                order.getId(), order.getTotal()));
                
            // Clear the cart display
            cartList.clear();
            updateCartDisplay();
            updateTotals();
            
            // Refresh the orders list
            loadOrders();
            
        } catch (SQLException e) {
            showError("Error processing order: " + e.getMessage());
            
            // Refresh the cart display to show current state
            try {
                if (currentCartUser != null) {
                    cartList.setAll(panierService.getUserCart(currentCartUser.getId()));
                    updateCartDisplay();
                    updateTotals();
                }
            } catch (SQLException ex) {
                showError("Error refreshing cart: " + ex.getMessage());
            }
        }
    }

    private void clearCart() {
        cartList.clear();
        currentCartUser = null;
        updateCartDisplay();
        updateTotals();
        updateCartUserLabel();
    }

    private void updateCartUserLabel() {
        if (currentCartUser != null) {
            cartUserLabel.setText("Cart for: " + currentCartUser.getUsername() + 
                                " (" + currentCartUser.getNom() + " " + currentCartUser.getPrenom() + ")");
            cartUserLabel.setVisible(true);
        } else {
            cartUserLabel.setText("");
            cartUserLabel.setVisible(false);
        }
    }

    private void showAddToCartDialog(Produit product) {
        try {
            // Verify product exists and get fresh stock data
            Produit freshProduct = produitService.getOne(product.getId());
            if (freshProduct == null) {
                showError("Product no longer exists!");
                return;
            }
            
            if (freshProduct.getStock() <= 0) {
                showError("Product is out of stock!");
                return;
            }

            // If no current cart user, show user selection dialog
            if (currentCartUser == null) {
                Dialog<Utilisateur> userDialog = new Dialog<>();
                userDialog.setTitle("Select User for Cart");
                userDialog.getDialogPane().getStyleClass().add("custom-dialog");
                
                GridPane userGrid = new GridPane();
                userGrid.setHgap(10);
                userGrid.setVgap(10);
                userGrid.setPadding(new Insets(20, 150, 10, 10));
                
                ComboBox<Utilisateur> userCombo = new ComboBox<>();
                userCombo.setPromptText("Select User");
                userCombo.getStyleClass().add("dialog-combo");
                userCombo.setCellFactory(lv -> new ListCell<Utilisateur>() {
                    @Override
                    protected void updateItem(Utilisateur user, boolean empty) {
                        super.updateItem(user, empty);
                        if (empty || user == null) {
                            setText(null);
                        } else {
                            setText(user.getUsername() + " (" + user.getNom() + " " + user.getPrenom() + ")");
                        }
                    }
                });
                userCombo.setButtonCell(userCombo.getCellFactory().call(null));
                
                try {
                    userCombo.setItems(FXCollections.observableArrayList(utilisateurService.afficher()));
                } catch (SQLException e) {
                    showError("Error loading users: " + e.getMessage());
                    return;
                }
                
                userGrid.add(new Label("Select User:"), 0, 0);
                userGrid.add(userCombo, 1, 0);
                
                userDialog.getDialogPane().setContent(userGrid);
                userDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                
                Button okButton = (Button) userDialog.getDialogPane().lookupButton(ButtonType.OK);
                okButton.setDisable(true);
                userCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                    okButton.setDisable(newVal == null);
                });
                
                userDialog.setResultConverter(dialogButton -> {
                    if (dialogButton == ButtonType.OK) {
                        return userCombo.getValue();
                    }
                    return null;
                });
                
                Optional<Utilisateur> userResult = userDialog.showAndWait();
                if (!userResult.isPresent()) {
                    return;
                }
                currentCartUser = userResult.get();
                updateCartUserLabel();
            }

            // Now show quantity dialog
            Dialog<Integer> dialog = new Dialog<>();
            dialog.setTitle("Add to Cart");
            dialog.getDialogPane().getStyleClass().add("custom-dialog");
            
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            Spinner<Integer> quantitySpinner = new Spinner<>(1, freshProduct.getStock(), 1);
            quantitySpinner.setEditable(true);
            
            grid.add(new Label("Available Stock: " + freshProduct.getStock()), 0, 0);
            grid.add(new Label("Quantity:"), 0, 1);
            grid.add(quantitySpinner, 1, 1);
            
            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    return quantitySpinner.getValue();
                }
                return null;
            });
            
            dialog.showAndWait().ifPresent(quantity -> {
                try {
                    Panier cartItem = new Panier();
                    cartItem.setUserId(currentCartUser.getId());
                    cartItem.setProductId(freshProduct.getId());
                    cartItem.setQuantity(quantity);
                    cartItem.setUnitPrice(freshProduct.getPrice());
                    
                    panierService.ajouter(cartItem);
                    cartList.add(cartItem);
                    updateCartDisplay();
                    updateTotals();
                    
                    showInfo("Item added to cart successfully!");
                } catch (SQLException e) {
                    System.out.println("Error adding item to cart: " + e.getMessage());
                    showError("Error adding item to cart: " + e.getMessage());
                }
            });
        } catch (SQLException e) {
            showError("Error verifying product: " + e.getMessage());
        }
    }

    @FXML
    private void showAddProductDialog() {
        Dialog<Produit> dialog = new Dialog<>();
        dialog.setTitle("Add New Product");
        dialog.getDialogPane().getStyleClass().add("custom-dialog");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Create form fields
        JFXTextField nameField = new JFXTextField();
        nameField.setPromptText("Product Name");
        nameField.getStyleClass().add("dialog-field");
        
        JFXTextArea descriptionField = new JFXTextArea();
        descriptionField.setPromptText("Product Description");
        descriptionField.setPrefRowCount(3);
        descriptionField.setWrapText(true);
        descriptionField.getStyleClass().add("dialog-field");
        
        JFXTextField priceField = new JFXTextField();
        priceField.setPromptText("Price");
        priceField.getStyleClass().add("dialog-field");
        
        JFXTextField stockField = new JFXTextField();
        stockField.setPromptText("Stock");
        stockField.getStyleClass().add("dialog-field");
        
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("CLOTHING", "EQUIPMENT", "ACCESSORIES", "FOOD", "OTHER");
        categoryCombo.setPromptText("Select Category");
        categoryCombo.getStyleClass().add("dialog-combo");
        
        // Add fields to grid
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Price:"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Stock:"), 0, 3);
        grid.add(stockField, 1, 3);
        grid.add(new Label("Category:"), 0, 4);
        grid.add(categoryCombo, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        
        // Form validation
        nameField.textProperty().addListener((obs, old, newValue) -> validateProductForm(
            okButton, nameField, descriptionField, priceField, stockField, categoryCombo));
        descriptionField.textProperty().addListener((obs, old, newValue) -> validateProductForm(
            okButton, nameField, descriptionField, priceField, stockField, categoryCombo));
        priceField.textProperty().addListener((obs, old, newValue) -> validateProductForm(
            okButton, nameField, descriptionField, priceField, stockField, categoryCombo));
        stockField.textProperty().addListener((obs, old, newValue) -> validateProductForm(
            okButton, nameField, descriptionField, priceField, stockField, categoryCombo));
        categoryCombo.valueProperty().addListener((obs, old, newValue) -> validateProductForm(
            okButton, nameField, descriptionField, priceField, stockField, categoryCombo));
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    Produit product = new Produit();
                    product.setName(nameField.getText().trim());
                    product.setDescription(descriptionField.getText().trim());
                    product.setPrice((float) Double.parseDouble(priceField.getText().trim()));
                    product.setStock(Integer.parseInt(stockField.getText().trim()));
                    product.setCategory(categoryCombo.getValue());
                    return product;
                } catch (NumberFormatException e) {
                    showError("Invalid number format");
                    return null;
                }
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(product -> {
            try {
                produitService.ajouter(product);
                loadProducts();
                showInfo("Product added successfully!");
            } catch (SQLException e) {
                showError("Error adding product: " + e.getMessage());
            }
        });
    }

    private void validateProductForm(Button okButton, JFXTextField nameField, JFXTextArea descriptionField,
                                   JFXTextField priceField, JFXTextField stockField, ComboBox<String> categoryCombo) {
        boolean isValid = !nameField.getText().trim().isEmpty() &&
                         !descriptionField.getText().trim().isEmpty() &&
                         !priceField.getText().trim().isEmpty() &&
                         !stockField.getText().trim().isEmpty() &&
                         categoryCombo.getValue() != null;
        
        try {
            if (!priceField.getText().trim().isEmpty()) {
                Double.parseDouble(priceField.getText());
            }
            if (!stockField.getText().trim().isEmpty()) {
                Integer.parseInt(stockField.getText());
            }
        } catch (NumberFormatException e) {
            isValid = false;
        }
        
        okButton.setDisable(!isValid);
    }

    private void showEditProductDialog(Produit product) {
        Dialog<Produit> dialog = new Dialog<>();
        dialog.setTitle("Edit Product");
        dialog.getDialogPane().getStyleClass().add("custom-dialog");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        JFXTextField nameField = new JFXTextField(product.getName());
        nameField.getStyleClass().add("dialog-field");
        
        JFXTextArea descriptionField = new JFXTextArea(product.getDescription());
        descriptionField.setPrefRowCount(3);
        descriptionField.setWrapText(true);
        descriptionField.getStyleClass().add("dialog-field");
        
        JFXTextField priceField = new JFXTextField(String.valueOf(product.getPrice()));
        priceField.getStyleClass().add("dialog-field");
        
        JFXTextField stockField = new JFXTextField(String.valueOf(product.getStock()));
        stockField.getStyleClass().add("dialog-field");
        
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("CLOTHING", "EQUIPMENT", "ACCESSORIES", "FOOD", "OTHER");
        categoryCombo.setValue(product.getCategory());
        categoryCombo.getStyleClass().add("dialog-combo");
        
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Price:"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Stock:"), 0, 3);
        grid.add(stockField, 1, 3);
        grid.add(new Label("Category:"), 0, 4);
        grid.add(categoryCombo, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        
        // Form validation
        nameField.textProperty().addListener((obs, old, newValue) -> validateProductForm(
            okButton, nameField, descriptionField, priceField, stockField, categoryCombo));
        descriptionField.textProperty().addListener((obs, old, newValue) -> validateProductForm(
            okButton, nameField, descriptionField, priceField, stockField, categoryCombo));
        priceField.textProperty().addListener((obs, old, newValue) -> validateProductForm(
            okButton, nameField, descriptionField, priceField, stockField, categoryCombo));
        stockField.textProperty().addListener((obs, old, newValue) -> validateProductForm(
            okButton, nameField, descriptionField, priceField, stockField, categoryCombo));
        categoryCombo.valueProperty().addListener((obs, old, newValue) -> validateProductForm(
            okButton, nameField, descriptionField, priceField, stockField, categoryCombo));
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    product.setName(nameField.getText().trim());
                    product.setDescription(descriptionField.getText().trim());
                    product.setPrice((float) Double.parseDouble(priceField.getText().trim()));
                    product.setStock(Integer.parseInt(stockField.getText().trim()));
                    product.setCategory(categoryCombo.getValue());
                    return product;
                } catch (NumberFormatException e) {
                    showError("Invalid number format");
                    return null;
                }
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(updatedProduct -> {
            try {
                produitService.modifier(updatedProduct);
                loadProducts();
                showInfo("Product updated successfully!");
            } catch (SQLException e) {
                showError("Error updating product: " + e.getMessage());
            }
        });
    }

    private void handleDeleteProduct(Produit product) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Product");
        alert.setHeaderText("Delete Product");
        alert.setContentText("Are you sure you want to delete this product?");
        alert.getDialogPane().getStyleClass().add("custom-dialog");
        
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    produitService.supprimer(product.getId());
                    loadProducts();
                    showInfo("Product deleted successfully!");
                } catch (SQLException e) {
                    showError("Error deleting product: " + e.getMessage());
                }
            }
        });
    }

    private void showOrderDetails(Commande order) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Order Details");
        dialog.getDialogPane().getStyleClass().add("custom-dialog");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Label orderIdLabel = new Label("Order #" + order.getId());
        orderIdLabel.getStyleClass().add("section-title");
        
        Label dateLabel = new Label("Date: " + order.getOrderDate().format(DATE_FORMATTER));
        Label statusLabel = new Label("Status: " + order.getStatus());
        Label paymentLabel = new Label("Payment Method: " + order.getPaymentMethod());
        
        content.getChildren().addAll(orderIdLabel, dateLabel, statusLabel, paymentLabel);
        
        try {
            VBox itemsBox = new VBox(5);
            itemsBox.getStyleClass().add("items-container");
            Label itemsTitle = new Label("Items");
            itemsTitle.getStyleClass().add("section-title");
            itemsBox.getChildren().add(itemsTitle);
            
            for (Commande.CommandeItem item : order.getItems()) {
                Produit product = produitService.getOne(item.getProductId());
                HBox itemRow = new HBox(10);
                itemRow.setAlignment(Pos.CENTER_LEFT);
                
                Label nameLabel = new Label(product.getName());
                Label quantityLabel = new Label("x" + item.getQuantity());
                Label priceLabel = new Label(String.format("%.2f TND", item.getSubtotal()));
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                itemRow.getChildren().addAll(nameLabel, quantityLabel, spacer, priceLabel);
                itemsBox.getChildren().add(itemRow);
            }
            
            content.getChildren().add(itemsBox);
            
            HBox totalBox = new HBox(10);
            totalBox.setAlignment(Pos.CENTER_RIGHT);
            Label totalLabel = new Label("Total: " + String.format("%.2f TND", order.getTotal()));
            totalLabel.getStyleClass().add("total-value");
            totalBox.getChildren().add(totalLabel);
            
            content.getChildren().add(totalBox);
        } catch (SQLException e) {
            showError("Error loading order details: " + e.getMessage());
        }
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        dialog.showAndWait();
    }

    private void printOrder(Commande order) {
        showInfo("Printing functionality will be implemented soon!");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStyleClass().add("custom-dialog");
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStyleClass().add("custom-dialog");
        alert.showAndWait();
    }
}

