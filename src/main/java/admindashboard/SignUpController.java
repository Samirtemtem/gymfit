package admindashboard;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import services.UtilisateurService;
import entities.Utilisateur;

import java.io.IOException;
import java.sql.SQLException;

public class SignUpController {
    @FXML private JFXTextField firstNameField;
    @FXML private JFXTextField lastNameField;
    @FXML private JFXTextField usernameField;
    @FXML private JFXTextField emailField;
    @FXML private JFXTextField addressField;
    @FXML private JFXPasswordField passwordField;
    @FXML private JFXPasswordField confirmPasswordField;
    @FXML private JFXCheckBox termsCheckbox;
    @FXML private JFXButton signUpButton;
    @FXML private Hyperlink loginLink;
    
    @FXML private Label firstNameErrorLabel;
    @FXML private Label lastNameErrorLabel;
    @FXML private Label usernameErrorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label addressErrorLabel;
    @FXML private Label passwordErrorLabel;
    @FXML private Label confirmPasswordErrorLabel;
    @FXML private Label termsErrorLabel;

    private UtilisateurService utilisateurService;

    @FXML
    public void initialize() {
        utilisateurService = new UtilisateurService();
        
        // Add real-time validation
        firstNameField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.trim().isEmpty()) {
                firstNameErrorLabel.setText("First name is required");
                firstNameErrorLabel.setVisible(true);
            } else {
                firstNameErrorLabel.setVisible(false);
            }
        });
        
        lastNameField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.trim().isEmpty()) {
                lastNameErrorLabel.setText("Last name is required");
                lastNameErrorLabel.setVisible(true);
            } else {
                lastNameErrorLabel.setVisible(false);
            }
        });
        
        usernameField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.trim().isEmpty()) {
                usernameErrorLabel.setText("Username is required");
                usernameErrorLabel.setVisible(true);
            } else if (newValue.length() < 3) {
                usernameErrorLabel.setText("Username must be at least 3 characters");
                usernameErrorLabel.setVisible(true);
            } else {
                usernameErrorLabel.setVisible(false);
            }
        });
        
        emailField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.trim().isEmpty()) {
                emailErrorLabel.setText("Email is required");
                emailErrorLabel.setVisible(true);
            } else if (!isValidEmail(newValue.trim())) {
                emailErrorLabel.setText("Please enter a valid email");
                emailErrorLabel.setVisible(true);
            } else {
                emailErrorLabel.setVisible(false);
            }
        });
        
        addressField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.trim().isEmpty()) {
                addressErrorLabel.setText("Address is required");
                addressErrorLabel.setVisible(true);
            } else {
                addressErrorLabel.setVisible(false);
            }
        });
        
        passwordField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.trim().isEmpty()) {
                passwordErrorLabel.setText("Password is required");
                passwordErrorLabel.setVisible(true);
            } else if (newValue.length() < 6) {
                passwordErrorLabel.setText("Password must be at least 6 characters");
                passwordErrorLabel.setVisible(true);
            } else {
                passwordErrorLabel.setVisible(false);
            }
            
            // Check confirm password match
            if (!confirmPasswordField.getText().isEmpty() && 
                !confirmPasswordField.getText().equals(newValue)) {
                confirmPasswordErrorLabel.setText("Passwords do not match");
                confirmPasswordErrorLabel.setVisible(true);
            } else {
                confirmPasswordErrorLabel.setVisible(false);
            }
        });
        
        confirmPasswordField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.trim().isEmpty()) {
                confirmPasswordErrorLabel.setText("Please confirm your password");
                confirmPasswordErrorLabel.setVisible(true);
            } else if (!newValue.equals(passwordField.getText())) {
                confirmPasswordErrorLabel.setText("Passwords do not match");
                confirmPasswordErrorLabel.setVisible(true);
            } else {
                confirmPasswordErrorLabel.setVisible(false);
            }
        });
        
        termsCheckbox.selectedProperty().addListener((obs, old, newValue) -> {
            termsErrorLabel.setVisible(!newValue);
        });
    }

    @FXML
    private void handleSignUp() {
        // Clear previous errors
        firstNameErrorLabel.setVisible(false);
        lastNameErrorLabel.setVisible(false);
        usernameErrorLabel.setVisible(false);
        emailErrorLabel.setVisible(false);
        addressErrorLabel.setVisible(false);
        passwordErrorLabel.setVisible(false);
        confirmPasswordErrorLabel.setVisible(false);
        termsErrorLabel.setVisible(false);
        
        // Validate fields
        boolean isValid = true;
        
        if (firstNameField.getText().trim().isEmpty()) {
            firstNameErrorLabel.setText("First name is required");
            firstNameErrorLabel.setVisible(true);
            isValid = false;
        }
        
        if (lastNameField.getText().trim().isEmpty()) {
            lastNameErrorLabel.setText("Last name is required");
            lastNameErrorLabel.setVisible(true);
            isValid = false;
        }
        
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            usernameErrorLabel.setText("Username is required");
            usernameErrorLabel.setVisible(true);
            isValid = false;
        } else if (username.length() < 3) {
            usernameErrorLabel.setText("Username must be at least 3 characters");
            usernameErrorLabel.setVisible(true);
            isValid = false;
        }
        
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            emailErrorLabel.setText("Email is required");
            emailErrorLabel.setVisible(true);
            isValid = false;
        } else if (!isValidEmail(email)) {
            emailErrorLabel.setText("Please enter a valid email");
            emailErrorLabel.setVisible(true);
            isValid = false;
        }
        
        String address = addressField.getText().trim();
        if (address.isEmpty()) {
            addressErrorLabel.setText("Address is required");
            addressErrorLabel.setVisible(true);
            isValid = false;
        }
        
        String password = passwordField.getText();
        if (password.isEmpty()) {
            passwordErrorLabel.setText("Password is required");
            passwordErrorLabel.setVisible(true);
            isValid = false;
        } else if (password.length() < 6) {
            passwordErrorLabel.setText("Password must be at least 6 characters");
            passwordErrorLabel.setVisible(true);
            isValid = false;
        }
        
        String confirmPassword = confirmPasswordField.getText();
        if (confirmPassword.isEmpty()) {
            confirmPasswordErrorLabel.setText("Please confirm your password");
            confirmPasswordErrorLabel.setVisible(true);
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            confirmPasswordErrorLabel.setText("Passwords do not match");
            confirmPasswordErrorLabel.setVisible(true);
            isValid = false;
        }
        
        if (!termsCheckbox.isSelected()) {
            termsErrorLabel.setText("You must agree to the Terms and Conditions");
            termsErrorLabel.setVisible(true);
            isValid = false;
        }
        
        if (!isValid) {
            return;
        }
        
        try {
            // Create new user
            Utilisateur user = new Utilisateur();
            user.setNom(lastNameField.getText().trim());
            user.setPrenom(firstNameField.getText().trim());
            user.setUsername(username);
            user.setEmail(email);
            user.setAdresse(address);
            user.setPassword(password);
            user.setRole(Utilisateur.Role.client);
            // Attempt to register
            utilisateurService.ajouter(user);
            
            // Show success message and navigate to login
            showSuccessAndNavigateToLogin();
        } catch (SQLException e) {
            // Check for duplicate username/email
            if (e.getMessage().contains("duplicate")) {
                if (e.getMessage().contains("username")) {
                    usernameErrorLabel.setText("Username already exists");
                    usernameErrorLabel.setVisible(true);
                } else if (e.getMessage().contains("email")) {
                    emailErrorLabel.setText("Email already registered");
                    emailErrorLabel.setVisible(true);
                }
            } else {
                emailErrorLabel.setText("Error during registration: " + e.getMessage());
                emailErrorLabel.setVisible(true);
            }
        }
    }

    @FXML
    private void handleLogin() {
        try {
            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admindashboard/Login.fxml"));
            Parent loginView = loader.load();
            
            // Get the current stage
            Stage stage = (Stage) loginLink.getScene().getWindow();
            
            // Set the new scene
            Scene scene = new Scene(loginView);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            emailErrorLabel.setText("Error loading login view: " + e.getMessage());
            emailErrorLabel.setVisible(true);
        }
    }

    private void showSuccessAndNavigateToLogin() {
        handleLogin();
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
}
