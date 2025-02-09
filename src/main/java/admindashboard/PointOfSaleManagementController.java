package admindashboard;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXComboBox;
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
import javafx.util.Pair;
import services.CommandeService;
import services.PanierService;
import services.ProduitService;
import services.UtilisateurService;
import javafx.scene.Node;

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
        dialog.getDialogPane().setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        
        VBox content = new VBox(15);
        content.setStyle("-fx-padding: 20; -fx-background-color: #1a1a1a; -fx-background-radius: 5; -fx-border-radius: 5;");
        content.setPrefWidth(300);
        
        Label titleLabel = new Label("Update Order Status");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        titleLabel.getStyleClass().add("dialog-title");
        
        VBox statusBox = new VBox(5);
        JFXComboBox<String> statusCombo = new JFXComboBox<>();
        statusCombo.getItems().addAll("PENDING", "PROCESSING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED");
        statusCombo.setValue(order.getStatus());
        statusCombo.setPromptText("Status");
        statusCombo.setStyle("-fx-text-fill: white; -fx-prompt-text-fill: #808080;");
        statusCombo.getStyleClass().add("jfx-combo-box");
        Label statusErrorLabel = new Label();
        statusErrorLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-size: 11px;");
        statusErrorLabel.setManaged(false);
        statusErrorLabel.setVisible(false);
        statusBox.getChildren().addAll(statusCombo, statusErrorLabel);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        JFXButton cancelButton = new JFXButton("Cancel");
        cancelButton.setStyle("-fx-text-fill: #808080;");
        cancelButton.getStyleClass().add("dialog-cancel-button");
        JFXButton saveButton = new JFXButton("Save");
        saveButton.setStyle("-fx-background-color: #FF6B00; -fx-text-fill: white;");
        saveButton.getStyleClass().add("dialog-save-button");
        buttonBox.getChildren().addAll(cancelButton, saveButton);
        
        content.getChildren().addAll(
            titleLabel,
            statusBox,
            buttonBox
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Hide the default buttons
        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setVisible(false);
        Node cancelButtonDefault = dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButtonDefault.setVisible(false);
        
        // Handle custom button actions
        cancelButton.setOnAction(e -> dialog.close());
        saveButton.setOnAction(e -> {
            if (statusCombo.getValue() != null) {
                try {
                    order.setStatus(statusCombo.getValue());
                    commandeService.modifier(order);
                    statusLabel.setText(statusCombo.getValue());
                    statusLabel.getStyleClass().removeAll("pending", "processing", "confirmed", "shipped", "delivered", "cancelled");
                    statusLabel.getStyleClass().add(statusCombo.getValue().toLowerCase());
                    showInfo("Order status updated successfully!");
                    dialog.close();
                } catch (SQLException ex) {
                    showError("Error updating order status: " + ex.getMessage());
                }
            } else {
                statusErrorLabel.setText("Please select a status");
                statusErrorLabel.setVisible(true);
            }
        });
        
        dialog.showAndWait();
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
                userDialog.getDialogPane().setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
                
                VBox content = new VBox(15);
                content.setStyle("-fx-padding: 20; -fx-background-color: #1a1a1a; -fx-background-radius: 5; -fx-border-radius: 5;");
                content.setPrefWidth(400);
                
                Label titleLabel = new Label("Select User");
                titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
                titleLabel.getStyleClass().add("dialog-title");
                
                VBox userBox = new VBox(5);
                JFXComboBox<Utilisateur> userCombo = new JFXComboBox<>();
                userCombo.setPromptText("Select User");
                userCombo.setStyle("-fx-text-fill: white; -fx-prompt-text-fill: #808080;");
                userCombo.getStyleClass().add("jfx-combo-box");
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
                
                Label userErrorLabel = new Label();
                userErrorLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-size: 11px;");
                userErrorLabel.setManaged(false);
                userErrorLabel.setVisible(false);
                userBox.getChildren().addAll(userCombo, userErrorLabel);
                
                // Buttons
                HBox buttonBox = new HBox(10);
                buttonBox.setAlignment(Pos.CENTER_RIGHT);
                JFXButton cancelButton = new JFXButton("Cancel");
                cancelButton.setStyle("-fx-text-fill: #808080;");
                cancelButton.getStyleClass().add("dialog-cancel-button");
                JFXButton selectButton = new JFXButton("Select");
                selectButton.setStyle("-fx-background-color: #FF6B00; -fx-text-fill: white;");
                selectButton.getStyleClass().add("dialog-save-button");
                buttonBox.getChildren().addAll(cancelButton, selectButton);
                
                content.getChildren().addAll(titleLabel, userBox, buttonBox);
                
                userDialog.getDialogPane().setContent(content);
                userDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                
                // Hide default buttons
                Node okButton = userDialog.getDialogPane().lookupButton(ButtonType.OK);
                okButton.setVisible(false);
                Node cancelButtonDefault = userDialog.getDialogPane().lookupButton(ButtonType.CANCEL);
                cancelButtonDefault.setVisible(false);
                
                // Handle button actions
                cancelButton.setOnAction(e -> userDialog.close());
                selectButton.setOnAction(e -> {
                    if (userCombo.getValue() != null) {
                        currentCartUser = userCombo.getValue();
                        updateCartUserLabel();
                        userDialog.close();
                    } else {
                        userErrorLabel.setText("Please select a user");
                        userErrorLabel.setVisible(true);
                    }
                });
                
                Optional<Utilisateur> userResult = userDialog.showAndWait();
                if (!userResult.isPresent()) {
                    return;
                }
            }

            // Now show quantity dialog
            Dialog<Integer> dialog = new Dialog<>();
            dialog.setTitle("Add to Cart");
            dialog.getDialogPane().getStyleClass().add("custom-dialog");
            dialog.getDialogPane().setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
            
            VBox content = new VBox(15);
            content.setStyle("-fx-padding: 20; -fx-background-color: #1a1a1a; -fx-background-radius: 5; -fx-border-radius: 5;");
            content.setPrefWidth(300);
            
            Label titleLabel = new Label("Add to Cart");
            titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
            titleLabel.getStyleClass().add("dialog-title");
            
            Label stockLabel = new Label("Available Stock: " + freshProduct.getStock());
            stockLabel.setStyle("-fx-text-fill: white;");
            
            VBox quantityBox = new VBox(5);
            Label quantityLabel = new Label("Quantity:");
            quantityLabel.setStyle("-fx-text-fill: white;");
            Spinner<Integer> quantitySpinner = new Spinner<>(1, freshProduct.getStock(), 1);
            quantitySpinner.setEditable(true);
            quantitySpinner.getStyleClass().add("jfx-spinner");
            quantityBox.getChildren().addAll(quantityLabel, quantitySpinner);
            
            // Buttons
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            JFXButton cancelButton = new JFXButton("Cancel");
            cancelButton.setStyle("-fx-text-fill: #808080;");
            cancelButton.getStyleClass().add("dialog-cancel-button");
            JFXButton addButton = new JFXButton("Add");
            addButton.setStyle("-fx-background-color: #FF6B00; -fx-text-fill: white;");
            addButton.getStyleClass().add("dialog-save-button");
            buttonBox.getChildren().addAll(cancelButton, addButton);
            
            content.getChildren().addAll(titleLabel, stockLabel, quantityBox, buttonBox);
            
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            // Hide default buttons
            Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.setVisible(false);
            Node cancelButtonDefault = dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
            cancelButtonDefault.setVisible(false);
            
            // Handle button actions
            cancelButton.setOnAction(e -> dialog.close());
            addButton.setOnAction(e -> {
                try {
                    Panier cartItem = new Panier();
                    cartItem.setUserId(currentCartUser.getId());
                    cartItem.setProductId(freshProduct.getId());
                    cartItem.setQuantity(quantitySpinner.getValue());
                    cartItem.setUnitPrice(freshProduct.getPrice());
                    
                    panierService.ajouter(cartItem);
                    cartList.add(cartItem);
                    updateCartDisplay();
                    updateTotals();
                    
                    showInfo("Item added to cart successfully!");
                    dialog.close();
                } catch (SQLException ex) {
                    showError("Error adding item to cart: " + ex.getMessage());
                }
            });
            
            dialog.showAndWait();
            
        } catch (SQLException e) {
            showError("Error verifying product: " + e.getMessage());
        }
    }

    @FXML
    private void showAddProductDialog() {
        Dialog<Produit> dialog = new Dialog<>();
        dialog.setTitle("Add New Product");
        dialog.getDialogPane().getStyleClass().add("custom-dialog");
        dialog.getDialogPane().setStyle("-fx-background-color: #262626; -fx-border-color: transparent;");
        
        VBox content = new VBox(15);
        content.setStyle("-fx-padding: 20; -fx-background-color: #262626;");
        content.setPrefWidth(400);
        
        Label titleLabel = new Label("Add New Product");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        
        // Name field with validation
        VBox nameBox = new VBox(5);
        Label nameLabel = new Label("Name");
        nameLabel.setStyle("-fx-text-fill: #808080;");
        JFXTextField nameField = new JFXTextField();
        nameField.setPromptText("Enter product name");
        nameField.setStyle("-fx-text-fill: white; -fx-prompt-text-fill: #808080;");
        Label nameErrorLabel = new Label();
        nameErrorLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-size: 11px;");
        nameErrorLabel.setManaged(true);
        nameErrorLabel.setVisible(false);
        nameBox.getChildren().addAll(nameLabel, nameField, nameErrorLabel);
        
        // Description field with validation
        VBox descBox = new VBox(5);
        Label descLabel = new Label("Description");
        descLabel.setStyle("-fx-text-fill: #808080;");
        JFXTextArea descriptionField = new JFXTextArea();
        descriptionField.setPromptText("Enter product description");
        descriptionField.setStyle("-fx-text-fill: white; -fx-prompt-text-fill: #808080;");
        descriptionField.setPrefRowCount(3);
        Label descErrorLabel = new Label();
        descErrorLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-size: 11px;");
        descErrorLabel.setManaged(true);
        descErrorLabel.setVisible(false);
        descBox.getChildren().addAll(descLabel, descriptionField, descErrorLabel);
        
        // Price field with validation
        VBox priceBox = new VBox(5);
        Label priceLabel = new Label("Price");
        priceLabel.setStyle("-fx-text-fill: #808080;");
        JFXTextField priceField = new JFXTextField();
        priceField.setPromptText("Enter product price");
        priceField.setStyle("-fx-text-fill: white; -fx-prompt-text-fill: #808080;");
        Label priceErrorLabel = new Label();
        priceErrorLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-size: 11px;");
        priceErrorLabel.setManaged(true);
        priceErrorLabel.setVisible(false);
        priceBox.getChildren().addAll(priceLabel, priceField, priceErrorLabel);
        
        // Stock field with validation
        VBox stockBox = new VBox(5);
        Label stockLabel = new Label("Stock");
        stockLabel.setStyle("-fx-text-fill: #808080;");
        JFXTextField stockField = new JFXTextField();
        stockField.setPromptText("Enter product stock");
        stockField.setStyle("-fx-text-fill: white; -fx-prompt-text-fill: #808080;");
        Label stockErrorLabel = new Label();
        stockErrorLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-size: 11px;");
        stockErrorLabel.setManaged(true);
        stockErrorLabel.setVisible(false);
        stockBox.getChildren().addAll(stockLabel, stockField, stockErrorLabel);
        
        // Category combo with validation
        VBox categoryBox = new VBox(5);
        Label categoryLabel = new Label("Category");
        categoryLabel.setStyle("-fx-text-fill: #808080;");
        JFXComboBox<String> categoryCombo = new JFXComboBox<>();
        categoryCombo.getItems().addAll("CLOTHING", "EQUIPMENT", "ACCESSORIES", "FOOD", "OTHER");
        categoryCombo.setPromptText("Select category");
        categoryCombo.setStyle("-fx-text-fill: white; -fx-prompt-text-fill: #808080;");
        Label categoryErrorLabel = new Label();
        categoryErrorLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-size: 11px;");
        categoryErrorLabel.setManaged(true);
        categoryErrorLabel.setVisible(false);
        categoryBox.getChildren().addAll(categoryLabel, categoryCombo, categoryErrorLabel);
        
        // Real-time validation
        nameField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.trim().isEmpty()) {
                nameErrorLabel.setText("Product name is required");
                nameErrorLabel.setVisible(true);
            } else {
                nameErrorLabel.setVisible(false);
            }
        });
        
        descriptionField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.trim().isEmpty()) {
                descErrorLabel.setText("Product description is required");
                descErrorLabel.setVisible(true);
            } else {
                descErrorLabel.setVisible(false);
            }
        });
        
        priceField.textProperty().addListener((obs, old, newValue) -> {
            try {
                if (newValue.trim().isEmpty()) {
                    priceErrorLabel.setText("Price is required");
                    priceErrorLabel.setVisible(true);
                } else {
                    float price = Float.parseFloat(newValue.trim());
                    if (price <= 0) {
                        priceErrorLabel.setText("Price must be greater than 0");
                        priceErrorLabel.setVisible(true);
                    } else {
                        priceErrorLabel.setVisible(false);
                    }
                }
            } catch (NumberFormatException e) {
                priceErrorLabel.setText("Please enter a valid price");
                priceErrorLabel.setVisible(true);
            }
        });
        
        stockField.textProperty().addListener((obs, old, newValue) -> {
            try {
                if (newValue.trim().isEmpty()) {
                    stockErrorLabel.setText("Stock quantity is required");
                    stockErrorLabel.setVisible(true);
                } else {
                    int stock = Integer.parseInt(newValue.trim());
                    if (stock < 0) {
                        stockErrorLabel.setText("Stock cannot be negative");
                        stockErrorLabel.setVisible(true);
                    } else {
                        stockErrorLabel.setVisible(false);
                    }
                }
            } catch (NumberFormatException e) {
                stockErrorLabel.setText("Please enter a valid number");
                stockErrorLabel.setVisible(true);
            }
        });
        
        categoryCombo.valueProperty().addListener((obs, old, newValue) -> {
            if (newValue == null) {
                categoryErrorLabel.setText("Please select a category");
                categoryErrorLabel.setVisible(true);
            } else {
                categoryErrorLabel.setVisible(false);
            }
        });
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        JFXButton cancelButton = new JFXButton("Cancel");
        cancelButton.setStyle("-fx-text-fill: #808080;");
        JFXButton saveButton = new JFXButton("Save");
        saveButton.setStyle("-fx-background-color: #FF6B00; -fx-text-fill: white;");
        buttonBox.getChildren().addAll(cancelButton, saveButton);
        
        content.getChildren().addAll(
            titleLabel,
            nameBox,
            descBox,
            priceBox,
            stockBox,
            categoryBox,
            buttonBox
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Hide the default buttons
        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setVisible(false);
        Node cancelButtonDefault = dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButtonDefault.setVisible(false);
        
        // Handle custom button actions
        cancelButton.setOnAction(e -> dialog.close());
        saveButton.setOnAction(e -> {
            if (validateProductForm(nameField, descriptionField, priceField, stockField, categoryCombo,
                                  nameErrorLabel, descErrorLabel, priceErrorLabel, stockErrorLabel, categoryErrorLabel)) {
                try {
                    Produit product = new Produit();
                    product.setName(nameField.getText().trim());
                    product.setDescription(descriptionField.getText().trim());
                    product.setPrice(Float.parseFloat(priceField.getText().trim()));
                    product.setStock(Integer.parseInt(stockField.getText().trim()));
                    product.setCategory(categoryCombo.getValue());
                    
                    produitService.ajouter(product);
                    loadProducts();
                    showInfo("Product added successfully!");
                    dialog.close();
                } catch (SQLException ex) {
                    showError("Error adding product: " + ex.getMessage());
                }
            }
        });
        
        dialog.showAndWait();
    }

    private boolean validateProductForm(JFXTextField nameField, JFXTextArea descriptionField,
                                    JFXTextField priceField, JFXTextField stockField,
                                    JFXComboBox<String> categoryCombo,
                                    Label nameError, Label descError, Label priceError,
                                    Label stockError, Label categoryError) {
        // Clear previous errors
        nameError.setVisible(false);
        descError.setVisible(false);
        priceError.setVisible(false);
        stockError.setVisible(false);
        categoryError.setVisible(false);
        
        boolean isValid = true;
        
        if (nameField.getText().trim().isEmpty()) {
            nameError.setText("Please enter product name");
            nameError.setVisible(true);
            isValid = false;
        }
        
        if (descriptionField.getText().trim().isEmpty()) {
            descError.setText("Please enter product description");
            descError.setVisible(true);
            isValid = false;
        }
        
        try {
            if (priceField.getText().trim().isEmpty()) {
                priceError.setText("Price is required");
                priceError.setVisible(true);
                isValid = false;
            } else {
                float price = Float.parseFloat(priceField.getText().trim());
                if (price <= 0) {
                    priceError.setText("Price must be greater than 0");
                    priceError.setVisible(true);
                    isValid = false;
                }
            }
        } catch (NumberFormatException e) {
            priceError.setText("Please enter a valid price");
            priceError.setVisible(true);
            isValid = false;
        }
        
        try {
            if (stockField.getText().trim().isEmpty()) {
                stockError.setText("Stock quantity is required");
                stockError.setVisible(true);
                isValid = false;
            } else {
                int stock = Integer.parseInt(stockField.getText().trim());
                if (stock < 0) {
                    stockError.setText("Stock cannot be negative");
                    stockError.setVisible(true);
                    isValid = false;
                }
            }
        } catch (NumberFormatException e) {
            stockError.setText("Please enter a valid stock number");
            stockError.setVisible(true);
            isValid = false;
        }
        
        if (categoryCombo.getValue() == null) {
            categoryError.setText("Please select a category");
            categoryError.setVisible(true);
            isValid = false;
        }
        
        return isValid;
    }

    private void showEditProductDialog(Produit product) {
        Dialog<Produit> dialog = new Dialog<>();
        dialog.setTitle("Edit Product");
        dialog.getDialogPane().getStyleClass().add("custom-dialog");
        dialog.getDialogPane().setStyle("-fx-background-color: #262626; -fx-border-color: transparent;");
        
        VBox content = new VBox(15);
        content.setStyle("-fx-padding: 20; -fx-background-color: #262626;");
        content.setPrefWidth(400);
        
        Label titleLabel = new Label("Edit Product");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        
        // Name field
        VBox nameBox = new VBox(5);
        Label nameLabel = new Label("Name");
        nameLabel.setStyle("-fx-text-fill: #808080;");
        JFXTextField nameField = new JFXTextField(product.getName());
        nameField.setPromptText("Product name");
        nameField.setStyle("-fx-text-fill: white; -fx-prompt-text-fill: #808080;");
        Label nameErrorLabel = new Label();
        nameErrorLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-size: 11px;");
        nameErrorLabel.setManaged(false);
        nameErrorLabel.setVisible(false);
        nameBox.getChildren().addAll(nameLabel, nameField, nameErrorLabel);
        
        // Description field
        VBox descBox = new VBox(5);
        Label descLabel = new Label("Description");
        descLabel.setStyle("-fx-text-fill: #808080;");
        JFXTextArea descriptionField = new JFXTextArea(product.getDescription());
        descriptionField.setPromptText("Product description");
        descriptionField.setStyle("-fx-text-fill: white; -fx-prompt-text-fill: #808080;");
        descriptionField.setPrefRowCount(3);
        Label descErrorLabel = new Label();
        descErrorLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-size: 11px;");
        descErrorLabel.setManaged(false);
        descErrorLabel.setVisible(false);
        descBox.getChildren().addAll(descLabel, descriptionField, descErrorLabel);
        
        // Price field
        VBox priceBox = new VBox(5);
        Label priceLabel = new Label("Price");
        priceLabel.setStyle("-fx-text-fill: #808080;");
        JFXTextField priceField = new JFXTextField(String.valueOf(product.getPrice()));
        priceField.setPromptText("Product price");
        priceField.setStyle("-fx-text-fill: white; -fx-prompt-text-fill: #808080;");
        Label priceErrorLabel = new Label();
        priceErrorLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-size: 11px;");
        priceErrorLabel.setManaged(false);
        priceErrorLabel.setVisible(false);
        priceBox.getChildren().addAll(priceLabel, priceField, priceErrorLabel);
        
        // Stock field
        VBox stockBox = new VBox(5);
        Label stockLabel = new Label("Stock");
        stockLabel.setStyle("-fx-text-fill: #808080;");
        JFXTextField stockField = new JFXTextField(String.valueOf(product.getStock()));
        stockField.setPromptText("Product stock");
        stockField.setStyle("-fx-text-fill: white; -fx-prompt-text-fill: #808080;");
        Label stockErrorLabel = new Label();
        stockErrorLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-size: 11px;");
        stockErrorLabel.setManaged(false);
        stockErrorLabel.setVisible(false);
        stockBox.getChildren().addAll(stockLabel, stockField, stockErrorLabel);
        
        // Category combo
        VBox categoryBox = new VBox(5);
        Label categoryLabel = new Label("Category");
        categoryLabel.setStyle("-fx-text-fill: #808080;");
        JFXComboBox<String> categoryCombo = new JFXComboBox<>();
        categoryCombo.getItems().addAll("CLOTHING", "EQUIPMENT", "ACCESSORIES", "FOOD", "OTHER");
        categoryCombo.setValue(product.getCategory());
        categoryCombo.setPromptText("Select category");
        categoryCombo.setStyle("-fx-text-fill: white; -fx-prompt-text-fill: #808080;");
        Label categoryErrorLabel = new Label();
        categoryErrorLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-size: 11px;");
        categoryErrorLabel.setManaged(false);
        categoryErrorLabel.setVisible(false);
        categoryBox.getChildren().addAll(categoryLabel, categoryCombo, categoryErrorLabel);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        JFXButton cancelButton = new JFXButton("Cancel");
        cancelButton.setStyle("-fx-text-fill: #808080;");
        JFXButton saveButton = new JFXButton("Save");
        saveButton.setStyle("-fx-background-color: #FF6B00; -fx-text-fill: white;");
        buttonBox.getChildren().addAll(cancelButton, saveButton);
        
        content.getChildren().addAll(
            titleLabel,
            nameBox,
            descBox,
            priceBox,
            stockBox,
            categoryBox,
            buttonBox
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Hide the default buttons
        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setVisible(false);
        Node cancelButtonDefault = dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButtonDefault.setVisible(false);
        
        // Handle custom button actions
        cancelButton.setOnAction(e -> dialog.close());
        saveButton.setOnAction(e -> {
            if (validateProductForm(nameField, descriptionField, priceField, stockField, categoryCombo,
                                  nameErrorLabel, descErrorLabel, priceErrorLabel, stockErrorLabel, categoryErrorLabel)) {
                try {
                    product.setName(nameField.getText().trim());
                    product.setDescription(descriptionField.getText().trim());
                    product.setPrice(Float.parseFloat(priceField.getText().trim()));
                    product.setStock(Integer.parseInt(stockField.getText().trim()));
                    product.setCategory(categoryCombo.getValue());
                    
                    produitService.modifier(product);
                    loadProducts();
                    showInfo("Product updated successfully!");
                    dialog.close();
                } catch (SQLException ex) {
                    showError("Error updating product: " + ex.getMessage());
                }
            }
        });
        
        dialog.showAndWait();
    }

    private void handleDeleteProduct(Produit product) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Product");
        alert.setHeaderText("Delete Product");
        alert.setContentText("Are you sure you want to delete this product?");
        alert.getDialogPane().getStyleClass().add("custom-dialog");
        alert.getDialogPane().setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 5; -fx-border-radius: 5;");
        
        // Style the buttons
        ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setStyle("-fx-background-color: #FF6B00; -fx-text-fill: white;");
        ((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setStyle("-fx-text-fill: #808080;");
        
        // Style the content
        alert.getDialogPane().getChildren().forEach(node -> {
            if (node instanceof Label) {
                ((Label) node).setStyle("-fx-text-fill: white;");
            }
        });

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
        dialog.getDialogPane().setStyle("-fx-background-color: #262626; -fx-border-color: transparent;");
        
        VBox content = new VBox(15);
        content.setStyle("-fx-padding: 20; -fx-background-color: #262626;");
        content.setPrefWidth(400);
        
        Label titleLabel = new Label("Order Details");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        
        // Order ID
        VBox orderIdBox = new VBox(5);
        Label orderIdLabel = new Label("Order Number");
        orderIdLabel.setStyle("-fx-text-fill: #808080;");
        Label orderIdValue = new Label("#" + order.getId());
        orderIdValue.setStyle("-fx-text-fill: white;");
        orderIdBox.getChildren().addAll(orderIdLabel, orderIdValue);
        
        // Date
        VBox dateBox = new VBox(5);
        Label dateLabel = new Label("Order Date");
        dateLabel.setStyle("-fx-text-fill: #808080;");
        Label dateValue = new Label(order.getOrderDate().format(DATE_FORMATTER));
        dateValue.setStyle("-fx-text-fill: white;");
        dateBox.getChildren().addAll(dateLabel, dateValue);
        
        // Status
        VBox statusBox = new VBox(5);
        Label statusLabel = new Label("Status");
        statusLabel.setStyle("-fx-text-fill: #808080;");
        Label statusValue = new Label(order.getStatus());
        statusValue.setStyle("-fx-text-fill: white;");
        statusBox.getChildren().addAll(statusLabel, statusValue);
        
        // Payment Method
        VBox paymentBox = new VBox(5);
        Label paymentLabel = new Label("Payment Method");
        paymentLabel.setStyle("-fx-text-fill: #808080;");
        Label paymentValue = new Label(order.getPaymentMethod());
        paymentValue.setStyle("-fx-text-fill: white;");
        paymentBox.getChildren().addAll(paymentLabel, paymentValue);
        
        // Items
        VBox itemsBox = new VBox(10);
        Label itemsLabel = new Label("Items");
        itemsLabel.setStyle("-fx-text-fill: #808080;");
        VBox itemsList = new VBox(5);
        itemsList.setStyle("-fx-background-color: #363636; -fx-padding: 10;");
        
        try {
            for (Commande.CommandeItem item : order.getItems()) {
                Produit product = produitService.getOne(item.getProductId());
                HBox itemRow = new HBox(10);
                itemRow.setAlignment(Pos.CENTER_LEFT);
                
                Label nameLabel = new Label(product.getName());
                nameLabel.setStyle("-fx-text-fill: white;");
                Label quantityLabel = new Label("x" + item.getQuantity());
                quantityLabel.setStyle("-fx-text-fill: #808080;");
                Label priceLabel = new Label(String.format("%.2f TND", item.getSubtotal()));
                priceLabel.setStyle("-fx-text-fill: #FF6B00;");
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                itemRow.getChildren().addAll(nameLabel, quantityLabel, spacer, priceLabel);
                itemsList.getChildren().add(itemRow);
            }
        } catch (SQLException e) {
            showError("Error loading order details: " + e.getMessage());
        }
        
        itemsBox.getChildren().addAll(itemsLabel, itemsList);
        
        // Total
        HBox totalBox = new HBox(10);
        totalBox.setAlignment(Pos.CENTER_RIGHT);
        Label totalLabel = new Label("Total: ");
        totalLabel.setStyle("-fx-text-fill: #808080;");
        Label totalValueLabel = new Label(String.format("%.2f TND", order.getTotal()));
        totalValueLabel.setStyle("-fx-text-fill: #FF6B00; -fx-font-size: 16px; -fx-font-weight: bold;");
        totalBox.getChildren().addAll(totalLabel, totalValueLabel);
        
        // Close button
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        JFXButton closeButton = new JFXButton("Close");
        closeButton.setStyle("-fx-text-fill: #808080;");
        buttonBox.getChildren().add(closeButton);
        
        content.getChildren().addAll(
            titleLabel,
            orderIdBox,
            dateBox,
            statusBox,
            paymentBox,
            itemsBox,
            totalBox,
            buttonBox
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        // Hide the default button
        Node closeButtonDefault = dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        closeButtonDefault.setVisible(false);
        
        // Handle close button action
        closeButton.setOnAction(e -> dialog.close());
        
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

