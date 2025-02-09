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

public class LoginController {
    @FXML private JFXTextField usernameField;
    @FXML private JFXPasswordField passwordField;
    @FXML private JFXCheckBox rememberMeCheckbox;
    @FXML private JFXButton loginButton;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Hyperlink signUpLink;
    @FXML private Label usernameErrorLabel;
    @FXML private Label passwordErrorLabel;

    private UtilisateurService utilisateurService;
    private static Utilisateur loggedInUser;

    public static Utilisateur getLoggedInUser() {
        return loggedInUser;
    }

    @FXML
    public void initialize() {
        utilisateurService = new UtilisateurService();
        
        // Add real-time validation
        usernameField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.trim().isEmpty()) {
                usernameErrorLabel.setText("Username is required");
                usernameErrorLabel.setVisible(true);
            } else {
                usernameErrorLabel.setVisible(false);
            }
        });
        
        passwordField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.trim().isEmpty()) {
                passwordErrorLabel.setText("Password is required");
                passwordErrorLabel.setVisible(true);
            } else {
                passwordErrorLabel.setVisible(false);
            }
        });
    }

    @FXML
    private void handleLogin() {
        // Clear previous errors
        usernameErrorLabel.setVisible(false);
        passwordErrorLabel.setVisible(false);
        
        // Validate fields
        boolean isValid = true;
        
        if (usernameField.getText().trim().isEmpty()) {
            usernameErrorLabel.setText("Username is required");
            usernameErrorLabel.setVisible(true);
            isValid = false;
        }
        
        if (passwordField.getText().trim().isEmpty()) {
            passwordErrorLabel.setText("Password is required");
            passwordErrorLabel.setVisible(true);
            isValid = false;
        }
        
        if (!isValid) {
            return;
        }
        
        try {
            // Attempt login
            Utilisateur user = utilisateurService.login(
                usernameField.getText().trim(),
                passwordField.getText().trim()
            );
            
            if (user != null) {
                // Store logged in user
                loggedInUser = user;
                
                // Store user session if remember me is checked
                if (rememberMeCheckbox.isSelected()) {
                    // TODO: Implement session storage
                }
                
                // Navigate based on user role
                if (user.getRole() == Utilisateur.Role.admin) {
                    navigateToAdminDashboard();
                } else {
                    navigateToClientDashboard();
                }
            } else {
                passwordErrorLabel.setText("Invalid username or password");
                passwordErrorLabel.setVisible(true);
            }
        } catch (SQLException e) {
            passwordErrorLabel.setText("Error during login: " + e.getMessage());
            passwordErrorLabel.setVisible(true);
        }
    }

    private void navigateToAdminDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admindashboard/PointOfSaleManagement.fxml"));
            Parent dashboardView = loader.load();
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(dashboardView);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            passwordErrorLabel.setText("Error loading admin dashboard: " + e.getMessage());
            passwordErrorLabel.setVisible(true);
        }
    }

    private void navigateToClientDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admindashboard/ClientDashboard.fxml"));
            Parent dashboardView = loader.load();
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(dashboardView);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            passwordErrorLabel.setText("Error loading client dashboard: " + e.getMessage());
            passwordErrorLabel.setVisible(true);
        }
    }

    @FXML
    private void handleForgotPassword() {
        // TODO: Implement forgot password functionality
    }

    @FXML
    private void handleSignUp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admindashboard/SignUp.fxml"));
            Parent signUpView = loader.load();
            
            Stage stage = (Stage) signUpLink.getScene().getWindow();
            Scene scene = new Scene(signUpView);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            passwordErrorLabel.setText("Error loading sign up view: " + e.getMessage());
            passwordErrorLabel.setVisible(true);
        }
    }
}