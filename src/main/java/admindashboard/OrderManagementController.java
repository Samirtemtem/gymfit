package admindashboard;

import com.jfoenix.controls.*;
import entities.Commande;
import entities.Produit;
import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import services.CommandeService;
import services.ProduitService;
import services.UtilisateurService;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.collections.FXCollections;

public class OrderManagementController implements Initializable {
    @FXML private FlowPane orderCardsContainer;
    @FXML private JFXTextField searchField;
    @FXML private JFXComboBox<String> statusFilter;
    @FXML private DatePicker dateFilter;
    @FXML private JFXDialog orderDialog;
    @FXML private JFXComboBox<Utilisateur> userComboBox;
    @FXML private JFXComboBox<Produit> productComboBox;
    @FXML private JFXTextField quantityField;
    @FXML private JFXTextField totalField;
    @FXML private JFXComboBox<String> statusComboBox;
    @FXML private StackPane rootPane;

    private CommandeService commandeService;
    private ProduitService produitService;
    private UtilisateurService utilisateurService;
    private Commande currentOrder;
    private boolean isEditing = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        commandeService = new CommandeService();
        produitService = new ProduitService();
        utilisateurService = new UtilisateurService();
        
        // Set dialog container
        orderDialog.setDialogContainer(rootPane);
        
        // Initialize filters
        statusFilter.setItems(FXCollections.observableArrayList(
            "All", "Pending", "Processing", "Shipped", "Delivered", "Cancelled"
        ));
        
        // Load users and products for combo boxes
        try {
            userComboBox.setItems(FXCollections.observableArrayList(
                utilisateurService.afficher()
            ));
            
            productComboBox.setItems(FXCollections.observableArrayList(
                produitService.afficher()
            ));
        } catch (SQLException e) {
      //      showError("Error loading data: " + e.getMessage());
        }
        
        // Initialize status combo box
        statusComboBox.setItems(FXCollections.observableArrayList(
            "Pending", "Processing", "Shipped", "Delivered", "Cancelled"
        ));
        
        // Add search listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
     //       refreshOrderList();
        });
        
        // Add filter listeners
      //  statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> refreshOrderList());
       // dateFilter.valueProperty().addListener((obs, oldVal, newVal) -> refreshOrderList());
        
        // Add quantity and product change listeners to update total
       // quantityField.textProperty().addListener((obs, oldVal, newVal) -> updateTotal());
        //productComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateTotal());
        
     //   refreshOrderList();
    }
/*
    private void updateTotal() {
        try {
            Produit selectedProduct = productComboBox.getValue();
            if (selectedProduct != null && !quantityField.getText().isEmpty()) {
                int quantity = Integer.parseInt(quantityField.getText());
                double total = selectedProduct.getPrice() * quantity;
                totalField.setText(String.format("%.2f", total));
            }
        } catch (NumberFormatException e) {
            totalField.clear();
        }
    }

    private void refreshOrderList() {
        orderCardsContainer.getChildren().clear();
        
        try {
            commandeService.afficher().stream()
                .filter(this::matchesFilters)
                .forEach(this::createOrderCard);
        } catch (SQLException e) {
            showError("Error loading orders: " + e.getMessage());
        }
    }

    private boolean matchesFilters(Commande order) {
        // Search text filter
        String searchTerm = searchField.getText();
        if (searchTerm != null && !searchTerm.isEmpty()) {
            if (!String.valueOf(order.getId()).contains(searchTerm)) {
                return false;
            }
        }
        
        // Status filter
        String status = statusFilter.getValue();
        if (status != null && !status.equals("All")) {
            if (!order.getStatus().equals(status)) {
                return false;
            }
        }
        
        // Date filter
        LocalDate filterDate = dateFilter.getValue();
        if (filterDate != null) {
            if (!order.getDate_c().contains(filterDate.toString())) {
                return false;
            }
        }
        
        return true;
    }

    private void createOrderCard(Commande order) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPrefWidth(280);
        
        Label orderIdLabel = new Label("Order #" + order.getId());
        orderIdLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        
        Label dateLabel = new Label("Date: " + order.getDate_c());
        dateLabel.setStyle("-fx-text-fill: #808080;");
        
        Label totalLabel = new Label(String.format("Total: $%.2f", order.getTotal_c()));
        totalLabel.setStyle("-fx-text-fill: #FF6B00;");
        
        Label statusLabel = new Label("Status: " + order.getStatut_c());
        statusLabel.setStyle("-fx-text-fill: #808080;");
        
        HBox actions = new HBox(10);
        JFXButton editBtn = new JFXButton("Edit");
        editBtn.getStyleClass().addAll("action-button");
        editBtn.setOnAction(e -> handleEdit(order));
        
        JFXButton deleteBtn = new JFXButton("Delete");
        deleteBtn.getStyleClass().addAll("action-button", "delete-button");
        deleteBtn.setOnAction(e -> handleDelete(order));
        
        actions.getChildren().addAll(editBtn, deleteBtn);
        
        card.getChildren().addAll(orderIdLabel, dateLabel, totalLabel, statusLabel, actions);
        orderCardsContainer.getChildren().add(card);
    }

    @FXML
    private void handleAddOrder() {
        isEditing = false;
        currentOrder = null;
        clearFields();
        orderDialog.show();
    }

    private void handleEdit(Commande order) {
        isEditing = true;
        currentOrder = order;
        populateFields(order);
        orderDialog.show();
    }

    private void handleDelete(Commande order) {
        try {
            commandeService.supprimer(order.getId());
            refreshOrderList();
        } catch (SQLException e) {
            showError("Error deleting order: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        try {
            Utilisateur selectedUser = userComboBox.getValue();
            Produit selectedProduct = productComboBox.getValue();
            String status = statusComboBox.getValue();
            
            if (selectedUser == null || selectedProduct == null || 
                quantityField.getText().isEmpty() || status == null) {
                showError("Please fill in all required fields");
                return;
            }
            
            int quantity = Integer.parseInt(quantityField.getText());
            double total = Double.parseDouble(totalField.getText());
            
            Commande commande = new Commande();
            commande.setId_user(selectedUser.getId());
            //commande.setProduit(selectedProduct);
            //commande.setQuantite_c(quantity);
            commande.setTotal_c((float) total);
            commande.setStatut_c(status);
            if (!isEditing) {
                // Set current date for new orders
                commande.setDate_c(String.valueOf(java.sql.Date.valueOf(LocalDate.now())));
            } else if (currentOrder != null) {
                commande.setId(currentOrder.getId());
                commande.setDate_c(currentOrder.getDate_c());
            }
            
            if (isEditing) {
                commandeService.modifier(commande);
            } else {
                commandeService.ajouter(commande);
            }
            
            orderDialog.close();
            refreshOrderList();
            
        } catch (NumberFormatException e) {
            showError("Please enter valid numbers");
        } catch (SQLException e) {
            showError("Error saving order: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        orderDialog.close();
    }

    private void clearFields() {
        userComboBox.getSelectionModel().clearSelection();
        productComboBox.getSelectionModel().clearSelection();
        quantityField.clear();
        totalField.clear();
        statusComboBox.getSelectionModel().clearSelection();
    }

    private void populateFields(Commande order) {
        try {
            Utilisateur user = utilisateurService.getOne(order.getId_user());
            Produit product = produitService.getOne(order.getId());
            
            userComboBox.setValue(user);
            productComboBox.setValue(product);
            //  quantityField.setText(String.valueOf(order.getQuantite()));
            // totalField.setText(String.format("%.2f", order.getTotal()));
            // statusComboBox.setValue(order.getStatut());
        } catch (SQLException e) {
            showError("Error loading order details: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }*/
}
