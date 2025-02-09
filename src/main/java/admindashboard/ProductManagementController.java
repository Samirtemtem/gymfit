package admindashboard;

import com.jfoenix.controls.*;
import entities.Produit;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import services.ProduitService;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.collections.FXCollections;

public class ProductManagementController implements Initializable {
    @FXML private FlowPane productCardsContainer;
    @FXML private JFXTextField searchField;
    @FXML private JFXComboBox<String> categoryFilter;
    @FXML private JFXComboBox<String> stockFilter;
    @FXML private JFXDialog productDialog;
    @FXML private JFXTextField nomField;
    @FXML private JFXTextArea descriptionField;
    @FXML private JFXTextField prixField;
    @FXML private JFXTextField stockField;
    @FXML private JFXComboBox<String> categorieField;
    @FXML private StackPane rootPane;
    @FXML private Label nomErrorLabel;
    @FXML private Label descriptionErrorLabel;
    @FXML private Label prixErrorLabel;
    @FXML private Label stockErrorLabel;
    @FXML private Label categorieErrorLabel;

    private ProduitService produitService;
    private Produit currentProduct;
    private boolean isEditing = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        produitService = new ProduitService();
   /*
        // Set dialog container
        productDialog.setDialogContainer(rootPane);
        
        // Initialize category options
        categorieField.setItems(FXCollections.observableArrayList(
            "Nutrition", "Equipment", "Accessories"
        ));
        
        // Initialize filters
        categoryFilter.setItems(FXCollections.observableArrayList(
            "All", "Nutrition", "Equipment", "Accessories"
        ));
        
        stockFilter.setItems(FXCollections.observableArrayList(
            "All", "In Stock", "Low Stock", "Out of Stock"
        ));
        
        // Add search listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            refreshProductList();
        });
        
        // Add filter listeners
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> refreshProductList());
        stockFilter.valueProperty().addListener((obs, oldVal, newVal) -> refreshProductList());
        
        refreshProductList();
    }

    private void refreshProductList() {
        productCardsContainer.getChildren().clear();
        
        try {
            produitService.afficher().stream()
                .filter(this::matchesFilters)
                .forEach(this::createProductCard);
        } catch (SQLException e) {
            showError("Error loading products: " + e.getMessage());
        }
    }

    private boolean matchesFilters(Produit product) {
        // Search text filter
        String searchTerm = searchField.getText();
        if (searchTerm != null && !searchTerm.isEmpty()) {
            if (!product.getNom_p().toLowerCase().contains(searchTerm.toLowerCase()) &&
                !product.getDescription_p().toLowerCase().contains(searchTerm.toLowerCase())) {
                return false;
            }
        }
        
        // Category filter
        String category = categoryFilter.getValue();
        if (category != null && !category.equals("All")) {
            if (!product.getCategorie_p().equals(category)) {
                return false;
            }
        }
        
        // Stock filter
        String stock = stockFilter.getValue();
        if (stock != null && !stock.equals("All")) {
            switch (stock) {
                case "Out of Stock":
                    if (product.getStock_p() > 0) return false;
                    break;
                case "Low Stock":
                    if (product.getStock_p() > 10) return false;
                    break;
                case "In Stock":
                    if (product.getStock_p() <= 0) return false;
                    break;
            }
        }
        
        return true;
    }

    private void createProductCard(Produit product) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPrefWidth(280);
        
        Label nameLabel = new Label(product.getNom_p());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        
        Label priceLabel = new Label(String.format("Price: $%.2f", product.getPrix_p()));
        priceLabel.setStyle("-fx-text-fill: #FF6B00;");
        
        Label stockLabel = new Label("Stock: " + product.getStock_p());
        stockLabel.setStyle("-fx-text-fill: #808080;");
        
        Label categoryLabel = new Label("Category: " + product.getCategorie_p());
        categoryLabel.setStyle("-fx-text-fill: #808080;");
        
        Label descriptionLabel = new Label(product.getDescription_p());
        descriptionLabel.setStyle("-fx-text-fill: #808080; -fx-wrap-text: true;");
        
        HBox actions = new HBox(10);
        JFXButton editBtn = new JFXButton("Edit");
        editBtn.getStyleClass().addAll("action-button");
        editBtn.setOnAction(e -> handleEdit(product));
        
        JFXButton deleteBtn = new JFXButton("Delete");
        deleteBtn.getStyleClass().addAll("action-button", "delete-button");
        deleteBtn.setOnAction(e -> handleDelete(product));
        
        actions.getChildren().addAll(editBtn, deleteBtn);
        
        card.getChildren().addAll(nameLabel, priceLabel, stockLabel, categoryLabel, 
                                descriptionLabel, actions);
        productCardsContainer.getChildren().add(card);
    }

    @FXML
    private void handleAddProduct() {
        isEditing = false;
        currentProduct = null;
        clearFields();
        clearErrors();
        productDialog.show();
    }

    private void handleEdit(Produit product) {
        isEditing = true;
        currentProduct = product;
        populateFields(product);
        productDialog.show();
    }

    private void handleDelete(Produit product) {
        try {
            produitService.supprimer(product.getId());
            refreshProductList();
        } catch (SQLException e) {
            showError("Error deleting product: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        try {
            clearErrors();
            boolean hasErrors = false;

            String nom = nomField.getText();
            if (nom == null || nom.trim().isEmpty()) {
                showError(nomErrorLabel, "Please enter product name");
                hasErrors = true;
            }

            String description = descriptionField.getText();
            if (description == null || description.trim().isEmpty()) {
                showError(descriptionErrorLabel, "Please enter product description");
                hasErrors = true;
            }

            String prix = prixField.getText();
            if (prix == null || prix.trim().isEmpty()) {
                showError(prixErrorLabel, "Please enter price");
                hasErrors = true;
            } else {
                try {
                    double price = Double.parseDouble(prix);
                    if (price <= 0) {
                        showError(prixErrorLabel, "Price must be greater than 0");
                        hasErrors = true;
                    }
                } catch (NumberFormatException e) {
                    showError(prixErrorLabel, "Please enter a valid price");
                    hasErrors = true;
                }
            }

            String stock = stockField.getText();
            if (stock == null || stock.trim().isEmpty()) {
                showError(stockErrorLabel, "Please enter stock quantity");
                hasErrors = true;
            } else {
                try {
                    int stockQty = Integer.parseInt(stock);
                    if (stockQty < 0) {
                        showError(stockErrorLabel, "Stock cannot be negative");
                        hasErrors = true;
                    }
                } catch (NumberFormatException e) {
                    showError(stockErrorLabel, "Please enter a valid number");
                    hasErrors = true;
                }
            }

            String categorie = categorieField.getValue();
            if (categorie == null) {
                showError(categorieErrorLabel, "Please select a category");
                hasErrors = true;
            }

            if (hasErrors) {
                return;
            }

            Produit produit = new Produit();
            produit.setNom_p(nom);
            produit.setDescription_p(description);
            produit.setPrix_p((float) Double.parseDouble(prix));
            produit.setStock_p(Integer.parseInt(stock));
            produit.setCategorie_p(categorie);
            
            if (isEditing && currentProduct != null) {
                produit.setId(currentProduct.getId());
                produitService.modifier(produit);
            } else {
                produitService.ajouter(produit);
            }
            
            productDialog.close();
            refreshProductList();
            
        } catch (SQLException e) {
            showError(categorieErrorLabel, "Database error: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        productDialog.close();
    }

    private void clearFields() {
        nomField.clear();
        descriptionField.clear();
        prixField.clear();
        stockField.clear();
        categorieField.getSelectionModel().clearSelection();
    }

    private void clearErrors() {
        nomErrorLabel.setVisible(false);
        descriptionErrorLabel.setVisible(false);
        prixErrorLabel.setVisible(false);
        stockErrorLabel.setVisible(false);
        categorieErrorLabel.setVisible(false);
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void populateFields(Produit product) {
        nomField.setText(product.getNom_p());
        descriptionField.setText(product.getDescription_p());
        prixField.setText(String.valueOf(product.getPrix_p()));
        stockField.setText(String.valueOf(product.getStock_p()));
        categorieField.getSelectionModel().select(product.getCategorie_p());
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        */

    }
}
