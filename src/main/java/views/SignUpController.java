package views;

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

/**
 * Controller for the sign-up view that handles user registration functionality.
 * Manages form validation, user creation, and navigation between views.
 */
public class SignUpController {
    // FXML injected fields for form controls
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
    
    // FXML injected fields for error labels
    @FXML private Label firstNameErrorLabel;
    @FXML private Label lastNameErrorLabel;
    @FXML private Label usernameErrorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label addressErrorLabel;
    @FXML private Label passwordErrorLabel;
    @FXML private Label confirmPasswordErrorLabel;
    @FXML private Label termsErrorLabel;

    private UtilisateurService utilisateurService;

    /**
     * Initializes the controller.
     * Sets up the user service and configures real-time validation for all form fields.
     */
    @FXML
    public void initialize() {
        utilisateurService = new UtilisateurService();
        setupValidation();
    }

    /**
     * Sets up real-time validation for all form fields.
     * Each field has its own validation rules and error messages.
     */
    private void setupValidation() {
        // First Name validation
        firstNameField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.trim().isEmpty()) {
                firstNameErrorLabel.setText("First name is required");
                firstNameErrorLabel.setVisible(true);
            } else {
                firstNameErrorLabel.setVisible(false);
            }
        });
        
        // Last Name validation
        lastNameField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.trim().isEmpty()) {
                lastNameErrorLabel.setText("Last name is required");
                lastNameErrorLabel.setVisible(true);
            } else {
                lastNameErrorLabel.setVisible(false);
            }
        });
        
        // Username validation
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
        
        // Email validation
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
        
        // Address validation
        addressField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.trim().isEmpty()) {
                addressErrorLabel.setText("Address is required");
                addressErrorLabel.setVisible(true);
            } else {
                addressErrorLabel.setVisible(false);
            }
        });
        
        // Password validation
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
            validatePasswordMatch();
        });
        
        // Confirm Password validation
        confirmPasswordField.textProperty().addListener((obs, old, newValue) -> {
            validatePasswordMatch();
        });
        
        // Terms checkbox validation
        termsCheckbox.selectedProperty().addListener((obs, old, newValue) -> {
            termsErrorLabel.setVisible(!newValue);
        });
    }

    /**
     * Validates that the password and confirm password fields match.
     * Shows error message if they don't match.
     */
    private void validatePasswordMatch() {
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        if (!confirmPassword.isEmpty() && !confirmPassword.equals(password)) {
            confirmPasswordErrorLabel.setText("Passwords do not match");
            confirmPasswordErrorLabel.setVisible(true);
        } else {
            confirmPasswordErrorLabel.setVisible(false);
        }
    }

    /**
     * Handles the sign-up button click.
     * Validates all fields, creates a new user, and registers them in the system.
     * Shows appropriate error messages if validation fails or if there are database errors.
     */
    @FXML
    private void handleSignUp() {
        clearErrors();
        
        if (!validateFields()) {
            return;
        }
        
        try {
            // Create and register new user
            Utilisateur user = createUser();
            utilisateurService.ajouter(user);
            showSuccessAndNavigateToLogin();
        } catch (SQLException e) {
            handleRegistrationError(e);
        }
    }

    /**
     * Creates a new User object from the form data.
     * @return Utilisateur object with the form data
     */
    private Utilisateur createUser() {
        Utilisateur user = new Utilisateur();
        user.setNom(lastNameField.getText().trim());
        user.setPrenom(firstNameField.getText().trim());
        user.setUsername(usernameField.getText().trim());
        user.setEmail(emailField.getText().trim());
        user.setAdresse(addressField.getText().trim());
        user.setPassword(passwordField.getText());
        user.setRole(Utilisateur.Role.client);
        return user;
    }

    /**
     * Handles database errors during registration.
     * Shows appropriate error messages for duplicate username/email.
     * @param e The SQLException that occurred
     */
    private void handleRegistrationError(SQLException e) {
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
            emailErrorLabel.setVisible(false);
        }
    }

    /**
     * Validates all form fields.
     * Shows appropriate error messages for invalid fields.
     * @return true if all fields are valid, false otherwise
     */
    private boolean validateFields() {
        boolean isValid = true;
        
        // Validate First Name
        if (firstNameField.getText().trim().isEmpty()) {
            firstNameErrorLabel.setText("First name is required");
            firstNameErrorLabel.setVisible(true);
            isValid = false;
        }
        
        // Validate Last Name
        if (lastNameField.getText().trim().isEmpty()) {
            lastNameErrorLabel.setText("Last name is required");
            lastNameErrorLabel.setVisible(true);
            isValid = false;
        }
        
        // Validate Username
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
        
        // Validate Email
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
        
        // Validate Address
        if (addressField.getText().trim().isEmpty()) {
            addressErrorLabel.setText("Address is required");
            addressErrorLabel.setVisible(true);
            isValid = false;
        }
        
        // Validate Password
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
        
        // Validate Confirm Password
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
        
        // Validate Terms
        if (!termsCheckbox.isSelected()) {
            termsErrorLabel.setText("You must agree to the Terms and Conditions");
            termsErrorLabel.setVisible(true);
            isValid = false;
        }
        
        return isValid;
    }

    /**
     * Clears all error messages from the form.
     */
    private void clearErrors() {
        firstNameErrorLabel.setVisible(false);
        lastNameErrorLabel.setVisible(false);
        usernameErrorLabel.setVisible(false);
        emailErrorLabel.setVisible(false);
        addressErrorLabel.setVisible(false);
        passwordErrorLabel.setVisible(false);
        confirmPasswordErrorLabel.setVisible(false);
        termsErrorLabel.setVisible(false);
    }

    /**
     * Handles navigation to the login view.
     * Called when user clicks the login link or after successful registration.
     */
    @FXML
    private void handleLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admindashboard/Login.fxml"));
            Parent loginView = loader.load();
            
            Stage stage = (Stage) loginLink.getScene().getWindow();
            Scene scene = new Scene(loginView);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            emailErrorLabel.setText("Error loading login view: " + e.getMessage());
            emailErrorLabel.setVisible(true);
        }
    }

    /**
     * Shows success message and navigates to login view after successful registration.
     */
    private void showSuccessAndNavigateToLogin() {
        // TODO: Show success message
        handleLogin();
    }

    /**
     * Validates email format using a simple regex pattern.
     * @param email The email to validate
     * @return true if email format is valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
}
