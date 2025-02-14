package views;

import com.jfoenix.controls.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.UtilisateurService;
import entities.Utilisateur;
import entities.Utilisateur.Role;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML private VBox loginForm;
    @FXML private VBox signupForm;
    @FXML private JFXTextField loginUsername;
    @FXML private JFXPasswordField loginPassword;
    @FXML private Label loginErrorLabel;
    
    @FXML private JFXTextField signupNom;
    @FXML private JFXTextField signupPrenom;
    @FXML private JFXTextField signupEmail;
    @FXML private JFXTextField signupTel;
    @FXML private JFXTextField signupAdresse;
    @FXML private JFXTextField signupUsername;
    @FXML private JFXPasswordField signupPassword;
    @FXML private JFXPasswordField signupConfirmPassword;
    @FXML private Label signupErrorLabel;
    
    private UtilisateurService utilisateurService;
    @FXML
    private VBox root;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        utilisateurService = new UtilisateurService();
        showLogin(); // Show login form by default
    }
    public void initStyle() {
        if (root.getScene() != null) {
            root.getScene().getStylesheets().add(getClass().getResource("/views/Login.css").toExternalForm());
        }
    }
    @FXML
    private void handleLogin() throws SQLException {
        String username = loginUsername.getText().trim();
        String password = loginPassword.getText().trim();
        
        // Validate inputs
        if (username.isEmpty() || password.isEmpty()) {
            showLoginError("Please fill in all fields");
            return;
        }
        
        // Attempt login
        Utilisateur user = utilisateurService.login(username, password);
        System.out.println(user);
        if (user != null) {
            // Login successful
            try {
                // Load appropriate dashboard based on user role
                String dashboardPath = user.getRole().equals("admin") 
                    ? "/admindashboard/AdminDashboard.fxml"
                    : "/userdashboard/UserDashboard.fxml";
                    
                Parent dashboard = FXMLLoader.load(getClass().getResource(dashboardPath));
                Scene scene = new Scene(dashboard);
                Stage stage = (Stage) loginForm.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                showLoginError("Error loading dashboard");
                e.printStackTrace();
            }
        } else {
            showLoginError("Invalid username or password");
        }
    }
    
    @FXML
    private void handleSignup() {
        // Get all field values
        String nom = signupNom.getText().trim();
        String prenom = signupPrenom.getText().trim();
        String email = signupEmail.getText().trim();
        String tel = signupTel.getText().trim();
        String adresse = signupAdresse.getText().trim();
        String username = signupUsername.getText().trim();
        String password = signupPassword.getText().trim();
        String confirmPassword = signupConfirmPassword.getText().trim();
        
        // Validate inputs
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || tel.isEmpty() || 
            adresse.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showSignupError("Please fill in all fields");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showSignupError("Passwords do not match");
            return;
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showSignupError("Invalid email format");
            return;
        }
        
        if (tel.length() != 8 || !tel.matches("\\d+")) {
            showSignupError("Phone number must be 8 digits");
            return;
        }
        
        // Create new user
        Utilisateur newUser = new Utilisateur();
        newUser.setNom(nom);
        newUser.setPrenom(prenom);
        newUser.setEmail(email);
        newUser.setTel(Integer.parseInt(signupTel.getText().trim()));
        newUser.setAdresse(adresse);
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setRole(Role.valueOf("Client")); // Default role
        
        // Attempt to register
        try {
            utilisateurService.ajouter(newUser);
            // Show success message and switch to login
            showSignupError("Registration successful! Please login.");
            showLogin();
        } catch (Exception e) {
            showSignupError("Error registering user: " + e.getMessage());
        }
    }
    
    @FXML
    private void showSignup() {
        loginForm.setVisible(false);
        loginForm.setManaged(false);
        signupForm.setVisible(true);
        signupForm.setManaged(true);
        clearErrors();
    }
    
    @FXML
    private void showLogin() {
        signupForm.setVisible(false);
        signupForm.setManaged(false);
        loginForm.setVisible(true);
        loginForm.setManaged(true);
        clearErrors();
    }
    
    private void showLoginError(String message) {
        loginErrorLabel.setText(message);
        loginErrorLabel.setVisible(true);
    }
    
    private void showSignupError(String message) {
        signupErrorLabel.setText(message);
        signupErrorLabel.setVisible(true);
    }
    
    private void clearErrors() {
        loginErrorLabel.setVisible(false);
        signupErrorLabel.setVisible(false);
    }
}
