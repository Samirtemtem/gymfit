package admindashboard;

import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import views.LoginController;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {
    @FXML private StackPane contentArea;
    @FXML private JFXButton usersBtn;
    @FXML private JFXButton sessionsBtn;
    @FXML private JFXButton exercisesBtn;
    @FXML private JFXButton productsBtn;
    @FXML private JFXButton ordersBtn;
    @FXML private JFXButton posBtn;
    
    private JFXButton currentButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Show users management by default
        showUsers();
    }
    
    private void setActiveButton(JFXButton button) {
        if (currentButton != null) {
            currentButton.getStyleClass().remove("active");
        }
        button.getStyleClass().add("active");
        currentButton = button;
    }

    @FXML
    private void showUsers() {
        loadView("UserManagement.fxml");
        setActiveButton(usersBtn);
    }

    @FXML
    private void showSessions() {
        loadView("SessionManagement.fxml");
        setActiveButton(sessionsBtn);
    }

    @FXML
    public void showExercises() {
        loadView("ExerciceManagement.fxml");
        setActiveButton(exercisesBtn);
    }

    @FXML
    private void showPointOfSale() {
        loadView("PointOfSaleManagement.fxml");
        setActiveButton(posBtn);
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admindashboard/Login.fxml"));
            Parent loginView = loader.load();

            // Create new scene
            Scene scene = new Scene(loginView);
            
            // Get the current stage
            Stage stage = (Stage) usersBtn.getScene().getWindow();
            
            // Set the new scene
            stage.setScene(scene);

            
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxml) {
        try {
            Node view = FXMLLoader.load(getClass().getResource("/admindashboard/" + fxml));
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading " + fxml + ": " + e.getMessage());
        }
    }
}
