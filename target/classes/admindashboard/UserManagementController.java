package admindashboard;

import com.jfoenix.controls.*;
import entities.Utilisateur;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import services.UtilisateurService;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class UserManagementController implements Initializable {
    @FXML private StackPane rootPane;
    @FXML private FlowPane userCardsContainer;
    @FXML private JFXTextField searchField;
    @FXML private JFXDialog userDialog;
    @FXML private JFXTextField nomField;
    @FXML private JFXTextField prenomField;
    @FXML private JFXTextField emailField;
    @FXML private JFXTextField telField;
    @FXML private JFXTextField adresseField;
    @FXML private JFXTextField usernameField;
    @FXML private JFXPasswordField passwordField;
    @FXML private JFXComboBox<String> roleComboBox;
    @FXML private Label nomErrorLabel;
    @FXML private Label prenomErrorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label telErrorLabel;
    @FXML private Label adresseErrorLabel;
    @FXML private Label usernameErrorLabel;
    @FXML private Label passwordErrorLabel;
    @FXML private Label roleErrorLabel;

    private UtilisateurService utilisateurService;
    private Utilisateur currentUser;
    private boolean isEditing = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        utilisateurService = new UtilisateurService();
        
        // Initialize role combo box
        roleComboBox.setItems(FXCollections.observableArrayList(
            "client", "coach", "admin"
        ));
        
        // Set dialog container
        userDialog.setDialogContainer(rootPane);
        
        // Initialize search field listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            refreshUserList(newValue);
        });
        
        // Initial user list load
        refreshUserList("");
    }

    private void refreshUserList(String searchTerm) {
        userCardsContainer.getChildren().clear();
        
        try {
            utilisateurService.afficher().stream()
                .filter(user -> matchesSearch(user, searchTerm))
                .forEach(this::createUserCard);
        } catch (SQLException e) {
            showError("Error loading users: " + e.getMessage());
        }
    }

    private boolean matchesSearch(Utilisateur user, String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) return true;
        
        String searchLower = searchTerm.toLowerCase();
        return user.getNom().toLowerCase().contains(searchLower) ||
               user.getPrenom().toLowerCase().contains(searchLower) ||
               user.getEmail().toLowerCase().contains(searchLower) ||
               user.getUsername().toLowerCase().contains(searchLower);
    }

    private void createUserCard(Utilisateur user) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #262626; -fx-padding: 15; -fx-background-radius: 5;");
        card.setPrefWidth(280);
        
        Label nameLabel = new Label(user.getNom() + " " + user.getPrenom());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        
        Label emailLabel = new Label(user.getEmail());
        emailLabel.setStyle("-fx-text-fill: #808080;");
        
        Label roleLabel = new Label("Role: " + user.getRole());
        roleLabel.setStyle("-fx-text-fill: #FF6B00;");
        
        HBox actions = new HBox(10);
        JFXButton editBtn = new JFXButton("Edit");
        editBtn.setStyle("-fx-background-color: #FF6B00; -fx-text-fill: white;");
        editBtn.setOnAction(e -> handleEdit(user));
        
        JFXButton deleteBtn = new JFXButton("Delete");
        deleteBtn.setStyle("-fx-background-color: #4d4d4d; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> handleDelete(user));
        
        actions.getChildren().addAll(editBtn, deleteBtn);
        
        card.getChildren().addAll(nameLabel, emailLabel, roleLabel, actions);
        userCardsContainer.getChildren().add(card);
    }

    @FXML
    private void handleAddUser() {
        isEditing = false;
        currentUser = null;
        clearFields();
        clearErrors();
        userDialog.show();
    }

    private void handleEdit(Utilisateur user) {
        isEditing = true;
        currentUser = user;
        populateFields(user);
        clearErrors();
        userDialog.show();
    }

    private void handleDelete(Utilisateur user) {
        try {
            utilisateurService.supprimer(user.getId());
            refreshUserList("");
        } catch (SQLException e) {
            showError("Error deleting user: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        try {
            clearErrors();
            boolean hasErrors = false;

            String nom = nomField.getText();
            if (nom == null || nom.trim().isEmpty()) {
                showError(nomErrorLabel, "Please enter last name");
                hasErrors = true;
            }

            String prenom = prenomField.getText();
            if (prenom == null || prenom.trim().isEmpty()) {
                showError(prenomErrorLabel, "Please enter first name");
                hasErrors = true;
            }

            String email = emailField.getText();
            if (email == null || email.trim().isEmpty()) {
                showError(emailErrorLabel, "Please enter email");
                hasErrors = true;
            } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                showError(emailErrorLabel, "Please enter a valid email address");
                hasErrors = true;
            }

            String tel = telField.getText();
            if (tel == null || tel.trim().isEmpty()) {
                showError(telErrorLabel, "Please enter phone number");
                hasErrors = true;
            } else if (!tel.matches("^[0-9]{8}$")) {
                showError(telErrorLabel, "Please enter a valid 8-digit phone number");
                hasErrors = true;
            }

            String adresse = adresseField.getText();
            if (adresse == null || adresse.trim().isEmpty()) {
                showError(adresseErrorLabel, "Please enter address");
                hasErrors = true;
            }

            String username = usernameField.getText();
            if (username == null || username.trim().isEmpty()) {
                showError(usernameErrorLabel, "Please enter username");
                hasErrors = true;
            } else if (username.length() < 3) {
                showError(usernameErrorLabel, "Username must be at least 3 characters long");
                hasErrors = true;
            }

            String password = passwordField.getText();
            if (!isEditing && (password == null || password.trim().isEmpty())) {
                showError(passwordErrorLabel, "Please enter password");
                hasErrors = true;
            } else if (!isEditing && password.length() < 6) {
                showError(passwordErrorLabel, "Password must be at least 6 characters long");
                hasErrors = true;
            }

            String role = roleComboBox.getValue();
            if (role == null) {
                showError(roleErrorLabel, "Please select a role");
                hasErrors = true;
            }

            if (hasErrors) {
                return;
            }

            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setNom(nom);
            utilisateur.setPrenom(prenom);
            utilisateur.setEmail(email);
            utilisateur.setTel(Integer.parseInt(tel));
            utilisateur.setAdresse(adresse);
            utilisateur.setUsername(username);
            if (!isEditing || !password.isEmpty()) {
                utilisateur.setPassword(password);
            }
            utilisateur.setRole(Utilisateur.Role.valueOf(role));
            
            if (isEditing && currentUser != null) {
                utilisateur.setId(currentUser.getId());
                utilisateurService.modifier(utilisateur);
            } else {
                utilisateurService.ajouter(utilisateur);
            }
            
            userDialog.close();
            refreshUserList("");
            
        } catch (SQLException e) {
            showError(roleErrorLabel, "Database error: " + e.getMessage());
        } catch (NumberFormatException e) {
            showError(telErrorLabel, "Please enter a valid phone number");
        }
    }

    @FXML
    private void handleCancel() {
        userDialog.close();
    }

    private void clearFields() {
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        telField.clear();
        adresseField.clear();
        usernameField.clear();
        passwordField.clear();
        roleComboBox.getSelectionModel().clearSelection();
    }

    private void populateFields(Utilisateur user) {
        nomField.setText(user.getNom());
        prenomField.setText(user.getPrenom());
        emailField.setText(user.getEmail());
        telField.setText(String.valueOf(user.getTel()));
        adresseField.setText(user.getAdresse());
        usernameField.setText(user.getUsername());
        passwordField.setText(user.getPassword());
        roleComboBox.setValue(user.getRole().toString());
    }

    private void clearErrors() {
        nomErrorLabel.setVisible(false);
        prenomErrorLabel.setVisible(false);
        emailErrorLabel.setVisible(false);
        telErrorLabel.setVisible(false);
        adresseErrorLabel.setVisible(false);
        usernameErrorLabel.setVisible(false);
        passwordErrorLabel.setVisible(false);
        roleErrorLabel.setVisible(false);
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
