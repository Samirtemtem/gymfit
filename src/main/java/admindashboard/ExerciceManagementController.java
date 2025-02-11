package admindashboard;

import com.jfoenix.controls.*;
import entities.Exercice;
import entities.Seance;
import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import services.ExerciceService;
import services.SeanceService;
import services.UtilisateurService;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.util.StringConverter;

public class ExerciceManagementController implements Initializable {
    @FXML private StackPane rootPane;
    @FXML private FlowPane exerciseCardsContainer;
    @FXML private JFXTextField searchField;
    @FXML private ComboBox<Seance> sessionFilter;
    @FXML private JFXDialog exerciseDialog;
    @FXML private ComboBox<Seance> sessionComboBox;
    @FXML private JFXTextField nameField;
    @FXML private JFXTextArea descriptionField;
    @FXML private JFXTextField durationField;
    @FXML private Label sessionErrorLabel;
    @FXML private Label nameErrorLabel;
    @FXML private Label descriptionErrorLabel;
    @FXML private Label durationErrorLabel;

    private ExerciceService exerciceService;
    private SeanceService seanceService;
    private UtilisateurService utilisateurService;
    private Exercice currentExercice;
    private boolean isEditing = false;
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter DB_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        exerciceService = new ExerciceService();
        seanceService = new SeanceService();
        utilisateurService = new UtilisateurService();
        
        // Set dialog container
        exerciseDialog.setDialogContainer(rootPane);
        
        // Initialize search field listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            refreshExerciseList(newValue);
        });
        
        // Setup session ComboBoxes
        setupSessionComboBoxes();
        
        // Initialize session filter listener
        sessionFilter.getSelectionModel().selectedItemProperty().addListener((obs, old, newValue) -> {
            refreshExerciseList(searchField.getText());
        });
        
        // Initial exercise list load
        refreshExerciseList("");
    }

    private void setupSessionComboBoxes() {
        StringConverter<Seance> sessionConverter = new StringConverter<>() {
            @Override
            public String toString(Seance seance) {
                if (seance == null) return "All Sessions";
                try {
                    Utilisateur user = utilisateurService.getOne(seance.getId_user());
                    Utilisateur coach = utilisateurService.getOne(seance.getId_coach());
                    String userName = user != null ? user.getPrenom() + " " + user.getNom() : "Unknown User";
                    String coachName = coach != null ? coach.getPrenom() + " " + coach.getNom() : "Unknown Coach";
                    LocalDateTime sessionDate = LocalDateTime.parse(seance.getDate_s(), DB_FORMATTER);
                    return String.format("%s - %s - %s - %s", 
                        userName,
                        coachName,
                        seance.getType_s(),
                        sessionDate.format(DISPLAY_FORMATTER));
                } catch (SQLException e) {
                    e.printStackTrace();
                    return "Error loading session";
                }
            }

            @Override
            public Seance fromString(String string) {
                return null; // Not needed for this use case
            }
        };

        sessionFilter.setConverter(sessionConverter);
        sessionComboBox.setConverter(sessionConverter);
        
        loadSessionFilters();
    }

    private void loadSessionFilters() {
        try {
            // Get sessions from SeanceService
            var sessions = seanceService.afficher();
            
            sessionFilter.getItems().clear();
            sessionFilter.getItems().add(null); // For "All Sessions" option
            sessionFilter.getItems().addAll(sessions);
            sessionFilter.setValue(null);
            
            // Also populate the session combo box for the dialog
            sessionComboBox.getItems().clear();
            sessionComboBox.getItems().addAll(sessions);
        } catch (SQLException e) {
            showError("Error loading sessions: " + e.getMessage());
        }
    }

    private void refreshExerciseList(String searchTerm) {
        exerciseCardsContainer.getChildren().clear();
        
        try {
            exerciceService.afficher().stream()
                .filter(exercise -> matchesSearch(exercise, searchTerm))
                .filter(this::matchesSessionFilter)
                .forEach(this::createExerciseCard);
        } catch (SQLException e) {
            showError("Error loading exercises: " + e.getMessage());
        }
    }

    private boolean matchesSearch(Exercice exercise, String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) return true;
        
        String searchLower = searchTerm.toLowerCase();
        return exercise.getNom_e().toLowerCase().contains(searchLower) ||
               exercise.getDescription_e().toLowerCase().contains(searchLower);
    }

    private boolean matchesSessionFilter(Exercice exercise) {
        Seance selectedSession = sessionFilter.getValue();
        if (selectedSession == null) return true;
        
        return exercise.getId_seance() == selectedSession.getId();
    }

    private void createExerciseCard(Exercice exercise) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #262626; -fx-padding: 15; -fx-background-radius: 5;");
        card.setPrefWidth(280);
        
        Label nameLabel = new Label(exercise.getNom_e());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        
        Label descriptionLabel = new Label(exercise.getDescription_e());
        descriptionLabel.setStyle("-fx-text-fill: #808080; -fx-wrap-text: true;");
        
        Label durationLabel = new Label(exercise.getDuree_e() + " minutes");
        durationLabel.setStyle("-fx-text-fill: #FF6B00;");
        
        // Get and display session information
        Label sessionLabel = new Label();
        try {
            Seance session = seanceService.getOne(exercise.getId_seance());
            if (session != null) {
                Utilisateur user = utilisateurService.getOne(session.getId_user());
                Utilisateur coach = utilisateurService.getOne(session.getId_coach());
                String userName = user != null ? user.getPrenom() + " " + user.getNom() : "Unknown User";
                String coachName = coach != null ? coach.getPrenom() + " " + coach.getNom() : "Unknown Coach";
                LocalDateTime sessionDate = LocalDateTime.parse(session.getDate_s(), DB_FORMATTER);
                String sessionInfo = String.format("Session: %s - %s - %s - %s",
                    userName, coachName, session.getType_s(), sessionDate.format(DISPLAY_FORMATTER));
                sessionLabel.setText(sessionInfo);
            } else {
                sessionLabel.setText("Session not found");
            }
        } catch (SQLException e) {
            sessionLabel.setText("Error loading session details");
            e.printStackTrace();
        }
        sessionLabel.setStyle("-fx-text-fill: #808080;");
        
        HBox actions = new HBox(10);
        JFXButton editBtn = new JFXButton("Edit");
        editBtn.setStyle("-fx-background-color: #FF6B00; -fx-text-fill: white;");
        editBtn.setOnAction(e -> handleEdit(exercise));
        
        JFXButton deleteBtn = new JFXButton("Delete");
        deleteBtn.setStyle("-fx-background-color: #4d4d4d; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> handleDelete(exercise));
        
        actions.getChildren().addAll(editBtn, deleteBtn);
        
        card.getChildren().addAll(nameLabel, descriptionLabel, durationLabel, sessionLabel, actions);
        exerciseCardsContainer.getChildren().add(card);
    }

    @FXML
    private void handleAddExercise() {
        isEditing = false;
        currentExercice = null;
        clearFields();
        clearErrors();
        exerciseDialog.show();
    }

    private void handleEdit(Exercice exercise) {
        isEditing = true;
        currentExercice = exercise;
        populateFields(exercise);
        clearErrors();
        exerciseDialog.show();
    }

    private void handleDelete(Exercice exercise) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Delete Exercise");
        alert.setHeaderText("Delete Exercise");
        alert.setContentText("Are you sure you want to delete this exercise?");
        
        alert.showAndWait().ifPresent(result -> {
            if (result == javafx.scene.control.ButtonType.OK) {
                try {
                    exerciceService.supprimer(exercise.getId());
                    refreshExerciseList("");
                    showInfo("Exercise deleted successfully!");
                } catch (SQLException e) {
                    showError("Error deleting exercise: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleSave() {
        if (!validateFields()) {
            return;
        }

        try {
            Exercice exercise = new Exercice();
            if (isEditing) {
                exercise.setId(currentExercice.getId());
            }
            
            Seance selectedSession = sessionComboBox.getValue();
            if (selectedSession != null) {
                exercise.setId_seance(selectedSession.getId());
            } else {
                showError("Please select a session");
                return;
            }
            
            exercise.setNom_e(nameField.getText().trim());
            exercise.setDescription_e(descriptionField.getText().trim());
            exercise.setDuree_e(Integer.parseInt(durationField.getText().trim()));
            
            if (isEditing) {
                exerciceService.modifier(exercise);
                showInfo("Exercise updated successfully!");
            } else {
                exerciceService.ajouter(exercise);
                showInfo("Exercise added successfully!");
            }
            
            exerciseDialog.close();
            refreshExerciseList("");
            loadSessionFilters();
            
        } catch (SQLException e) {
            showError("Error saving exercise: " + e.getMessage());
        } catch (NumberFormatException e) {
            durationErrorLabel.setText("Please enter a valid duration");
            durationErrorLabel.setVisible(true);
        }
    }

    @FXML
    private void handleCancel() {
        exerciseDialog.close();
    }

    private boolean validateFields() {
        boolean isValid = true;
        clearErrors();

        if (sessionComboBox.getValue() == null) {
            showFieldError(sessionErrorLabel, "Please select a session");
            isValid = false;
        }

        if (nameField.getText().trim().isEmpty()) {
            showFieldError(nameErrorLabel, "Please enter exercise name");
            isValid = false;
        }

        if (descriptionField.getText().trim().isEmpty()) {
            showFieldError(descriptionErrorLabel, "Please enter exercise description");
            isValid = false;
        }

        String duration = durationField.getText().trim();
        if (duration.isEmpty()) {
            showFieldError(durationErrorLabel, "Please enter exercise duration");
            isValid = false;
        } else {
            try {
                int durationValue = Integer.parseInt(duration);
                if (durationValue <= 0) {
                    showFieldError(durationErrorLabel, "Duration must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                showFieldError(durationErrorLabel, "Please enter a valid number");
                isValid = false;
            }
        }

        return isValid;
    }

    private void showFieldError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearErrors() {
        sessionErrorLabel.setVisible(false);
        sessionErrorLabel.setManaged(false);
        nameErrorLabel.setVisible(false);
        nameErrorLabel.setManaged(false);
        descriptionErrorLabel.setVisible(false);
        descriptionErrorLabel.setManaged(false);
        durationErrorLabel.setVisible(false);
        durationErrorLabel.setManaged(false);
    }

    private void clearFields() {
        sessionComboBox.setValue(null);
        nameField.clear();
        descriptionField.clear();
        durationField.clear();
    }

    private void populateFields(Exercice exercise) {
        try {
            Seance session = seanceService.getOne(exercise.getId_seance());
            sessionComboBox.setValue(session);
            nameField.setText(exercise.getNom_e());
            descriptionField.setText(exercise.getDescription_e());
            durationField.setText(String.valueOf(exercise.getDuree_e()));
        } catch (SQLException e) {
            showError("Error loading session details: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 