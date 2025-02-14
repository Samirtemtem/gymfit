package views;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;

import admindashboard.LoginController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import services.UtilisateurService;
import entities.Utilisateur;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Controller for the client dashboard view.
 * Handles user profile management and display.
 * Provides functionality for viewing and updating user information.
 */
public class ClientDashboardController {
    // FXML injected fields for UI controls
    @FXML private Label welcomeLabel;
    @FXML private JFXTextField firstNameField;
    @FXML private JFXTextField lastNameField;
    @FXML private JFXTextField emailField;
    @FXML private JFXTextField addressField;
    @FXML private JFXTextField phoneField;
    @FXML private JFXPasswordField currentPasswordField;
    @FXML private JFXPasswordField newPasswordField;
    @FXML private JFXPasswordField confirmPasswordField;
    
    // FXML injected fields for error labels
    @FXML private Label firstNameErrorLabel;
    @FXML private Label lastNameErrorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label addressErrorLabel;
    @FXML private Label phoneErrorLabel;
    @FXML private Label passwordErrorLabel;
    
    private UtilisateurService utilisateurService;
    private Utilisateur currentUser;

    /**
     * Initializes the controller.
     * Sets up the user service, loads user data, and configures window properties.
     * Also sets up real-time validation for form fields.
     */
    @FXML
    public void initialize() {
        utilisateurService = new UtilisateurService();
        currentUser = LoginController.getLoggedInUser();
        
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getPrenom() + " " + currentUser.getNom());
            loadUserData();
        }
        
        // Make window fullscreen after the scene is fully loaded
        Platform.runLater(() -> {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            if (stage != null) {
                stage.setMaximized(true);
            }
        });
        
        setupValidation();
    }

    /**
     * Loads the current user's data into the form fields.
     * Populates all fields with the user's current information.
     */
    private void loadUserData() {
        firstNameField.setText(currentUser.getPrenom());
        lastNameField.setText(currentUser.getNom());
        emailField.setText(currentUser.getEmail());
        addressField.setText(currentUser.getAdresse());
        phoneField.setText(String.valueOf(currentUser.getTel()));
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
        
        // Phone validation
        phoneField.textProperty().addListener((obs, old, newValue) -> {
            if (!newValue.trim().isEmpty()) {
                try {
                    Integer.parseInt(newValue.trim());
                    phoneErrorLabel.setVisible(false);
                } catch (NumberFormatException e) {
                    phoneErrorLabel.setText("Please enter a valid phone number");
                    phoneErrorLabel.setVisible(true);
                }
            }
        });
        
        // New Password validation
        newPasswordField.textProperty().addListener((obs, old, newValue) -> {
            validateNewPassword(newValue);
        });
        
        // Confirm Password validation
        confirmPasswordField.textProperty().addListener((obs, old, newValue) -> {
            validateConfirmPassword(newValue);
        });
    }

    /**
     * Validates the new password field.
     * Checks password length and match with confirm password.
     * @param newValue The new password value
     */
    private void validateNewPassword(String newValue) {
        if (!newValue.isEmpty() && newValue.length() < 6) {
            passwordErrorLabel.setText("Password must be at least 6 characters");
            passwordErrorLabel.setVisible(true);
        } else {
            passwordErrorLabel.setVisible(false);
        }
        
        if (!confirmPasswordField.getText().isEmpty() && 
            !confirmPasswordField.getText().equals(newValue)) {
            passwordErrorLabel.setText("Passwords do not match");
            passwordErrorLabel.setVisible(true);
        }
    }

    /**
     * Validates the confirm password field.
     * Ensures it matches the new password.
     * @param newValue The confirm password value
     */
    private void validateConfirmPassword(String newValue) {
        if (!newValue.isEmpty() && !newValue.equals(newPasswordField.getText())) {
            passwordErrorLabel.setText("Passwords do not match");
            passwordErrorLabel.setVisible(true);
        } else {
            passwordErrorLabel.setVisible(false);
        }
    }

    /**
     * Handles the save button click.
     * Validates all fields and updates the user's information.
     * Shows appropriate error messages if validation fails.
     */
    @FXML
    private void handleSave() {
        clearErrors();
        
        if (!validateFields()) {
            return;
        }
        
        updateUserData();
        
        try {
            utilisateurService.modifier(currentUser);
            showSuccessMessage("Profile updated successfully!");
            loadUserData();
        } catch (SQLException e) {
            handleUpdateError(e);
        }
    }

    /**
     * Updates the current user's data from form fields.
     * Includes handling of optional password change.
     */
    private void updateUserData() {
        currentUser.setPrenom(firstNameField.getText().trim());
        currentUser.setNom(lastNameField.getText().trim());
        currentUser.setEmail(emailField.getText().trim());
        currentUser.setAdresse(addressField.getText().trim());
        
        String phone = phoneField.getText().trim();
        if (!phone.isEmpty()) {
            try {
                currentUser.setTel(Integer.parseInt(phone));
            } catch (NumberFormatException e) {
                phoneErrorLabel.setText("Please enter a valid phone number");
                phoneErrorLabel.setVisible(true);
            }
        }
        
        if (!newPasswordField.getText().isEmpty()) {
            if (validatePasswordChange()) {
                currentUser.setPassword(newPasswordField.getText());
            }
        }
    }

    /**
     * Validates all form fields.
     * Shows appropriate error messages for invalid fields.
     * @return true if all fields are valid, false otherwise
     */
    private boolean validateFields() {
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
        
        return isValid;
    }

    /**
     * Validates password change fields if a password change is requested.
     * @return true if password change is valid, false otherwise
     */
    private boolean validatePasswordChange() {
        if (currentPasswordField.getText().isEmpty()) {
            passwordErrorLabel.setText("Please enter your current password");
            passwordErrorLabel.setVisible(true);
            return false;
        }
        
        if (!currentPasswordField.getText().equals(currentUser.getPassword())) {
            passwordErrorLabel.setText("Current password is incorrect");
            passwordErrorLabel.setVisible(true);
            return false;
        }
        
        if (newPasswordField.getText().length() < 6) {
            passwordErrorLabel.setText("New password must be at least 6 characters");
            passwordErrorLabel.setVisible(true);
            return false;
        }
        
        if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
            passwordErrorLabel.setText("Passwords do not match");
            passwordErrorLabel.setVisible(true);
            return false;
        }
        
        return true;
    }

    /**
     * Handles database errors during user update.
     * Shows appropriate error messages.
     * @param e The SQLException that occurred
     */
    private void handleUpdateError(SQLException e) {
        if (e.getMessage().contains("duplicate")) {
            if (e.getMessage().contains("email")) {
                emailErrorLabel.setText("Email already registered");
                emailErrorLabel.setVisible(true);
            }
        } else {
            showErrorMessage("Error updating profile: " + e.getMessage());
        }
    }

    /**
     * Clears all error messages from the form.
     */
    private void clearErrors() {
        firstNameErrorLabel.setVisible(false);
        lastNameErrorLabel.setVisible(false);
        emailErrorLabel.setVisible(false);
        addressErrorLabel.setVisible(false);
        phoneErrorLabel.setVisible(false);
        passwordErrorLabel.setVisible(false);
    }

    /**
     * Handles the logout button click.
     * Navigates back to the login view.
     */
    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admindashboard/Login.fxml"));
            Parent loginView = loader.load();
            
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            Scene scene = new Scene(loginView);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showErrorMessage("Error loading login view: " + e.getMessage());
        }
    }

    /**
     * Shows a success message to the user.
     * @param message The success message to display
     */
    private void showSuccessMessage(String message) {
        System.out.println("Success: " + message);
    }

    /**
     * Shows an error message to the user.
     * @param message The error message to display
     */
    private void showErrorMessage(String message) {
        System.out.println("Error: " + message);
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