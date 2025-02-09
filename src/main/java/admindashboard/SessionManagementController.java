package admindashboard;

import com.jfoenix.controls.*;
import entities.Seance;
import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Alert.AlertType;
import javafx.collections.FXCollections;
import javafx.util.StringConverter;
import javafx.util.Callback;
import services.SeanceService;
import services.UtilisateurService;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ResourceBundle;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

public class SessionManagementController implements Initializable {
    @FXML private FlowPane sessionCardsContainer;
    @FXML private JFXTextField searchField;
    @FXML private JFXComboBox<String> statusFilter;
    @FXML private DatePicker dateFilter;
    @FXML private JFXDialog sessionDialog;
    @FXML private JFXComboBox<Utilisateur> userComboBox;
    @FXML private JFXComboBox<Utilisateur> coachComboBox;
    @FXML private DatePicker dateField;
    @FXML private Spinner<Integer> hourSpinner;
    @FXML private Spinner<Integer> minuteSpinner;
    @FXML private JFXTextField typeField;
    @FXML private JFXTextField dureeField;
    @FXML private JFXComboBox<String> statutComboBox;
    @FXML private JFXComboBox<String> intensiteComboBox;
    @FXML private JFXTextArea objectifsArea;
    @FXML private JFXTextArea recommandationsArea;
    @FXML private StackPane rootPane;
    @FXML private Label userErrorLabel;
    @FXML private Label coachErrorLabel;
    @FXML private Label dateErrorLabel;
    @FXML private Label typeErrorLabel;
    @FXML private Label dureeErrorLabel;
    @FXML private Label intensiteErrorLabel;
    @FXML private Label objectifsErrorLabel;
    @FXML private Label recommandationsErrorLabel;
    @FXML private Label statutErrorLabel;

    private SeanceService seanceService;
    private UtilisateurService utilisateurService;
    private Seance currentSession;
    private boolean isEditing = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        seanceService = new SeanceService();
        utilisateurService = new UtilisateurService();
        
        // Set dialog container
        sessionDialog.setDialogContainer(rootPane);
        
        // Initialize spinners
        SpinnerValueFactory<Integer> hourFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 12);
        SpinnerValueFactory<Integer> minuteFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);
        hourSpinner.setValueFactory(hourFactory);
        minuteSpinner.setValueFactory(minuteFactory);
        
        // Add leading zeros to spinners
        StringConverter<Integer> timeConverter = new StringConverter<Integer>() {
            @Override
            public String toString(Integer value) {
                return String.format("%02d", value);
            }
            
            @Override
            public Integer fromString(String string) {
                return Integer.parseInt(string);
            }
        };
        hourSpinner.getValueFactory().setConverter(timeConverter);
        minuteSpinner.getValueFactory().setConverter(timeConverter);
        
        // Initialize combo boxes
        try {
            // Load clients (users with role 'client')
            List<Utilisateur> clients = utilisateurService.afficher().stream()
                .filter(u -> u.getRole() == Utilisateur.Role.client)
                .collect(Collectors.toList());
            userComboBox.setItems(FXCollections.observableArrayList(clients));
            
            // Load coaches (users with role 'coach')
            List<Utilisateur> coaches = utilisateurService.afficher().stream()
                .filter(u -> u.getRole() == Utilisateur.Role.coach)
                .collect(Collectors.toList());
            coachComboBox.setItems(FXCollections.observableArrayList(coaches));
            
            // Set cell factories to display user names
            Callback<ListView<Utilisateur>, ListCell<Utilisateur>> cellFactory = lv -> new ListCell<Utilisateur>() {
                @Override
                protected void updateItem(Utilisateur user, boolean empty) {
                    super.updateItem(user, empty);
                    if (empty || user == null) {
                        setText(null);
                    } else {
                        setText(user.getPrenom() + " " + user.getNom());
                    }
                }
            };
            
            userComboBox.setCellFactory(cellFactory);
            userComboBox.setButtonCell(cellFactory.call(null));
            coachComboBox.setCellFactory(cellFactory);
            coachComboBox.setButtonCell(cellFactory.call(null));
            
        } catch (SQLException e) {
            showError("Error loading users: " + e.getMessage());
        }
        
        // Initialize intensity options
        intensiteComboBox.setItems(FXCollections.observableArrayList(
            "Low", "Medium", "High"
        ));
        
        // Initialize status options
        statutComboBox.setItems(FXCollections.observableArrayList(
            "Scheduled", "In Progress", "Completed", "Cancelled"
        ));
        statusFilter.setItems(FXCollections.observableArrayList(
            "Scheduled", "In Progress", "Completed", "Cancelled"
        ));; 
        
        // Add search listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            refreshSessionList();
        });
        
        // Add filter listeners
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> refreshSessionList());
        dateFilter.valueProperty().addListener((obs, oldVal, newVal) -> refreshSessionList());
        
        refreshSessionList();
    }

    private void refreshSessionList() {
        sessionCardsContainer.getChildren().clear();
        
        try {
            seanceService.afficher().stream()
                .filter(this::matchesFilters)
                .forEach(this::createSessionCard);
        } catch (SQLException e) {
            showError("Error loading sessions: " + e.getMessage());
        }
    }

    private boolean matchesFilters(Seance session) {
        // Search text filter
        String searchTerm = searchField.getText();
        if (searchTerm != null && !searchTerm.isEmpty()) {
            if (!session.getType_s().toLowerCase().contains(searchTerm.toLowerCase())) {
                return false;
            }
        }
        
        // Status filter
        String status = statusFilter.getValue();
        if (status != null && !status.equals("All")) {
            if (!session.getStatut_s().equals(status)) {
                return false;
            }
        }
        
        // Date filter
        LocalDate filterDate = dateFilter.getValue();
        if (filterDate != null) {
            if (!session.getDate_s().contains(filterDate.toString())) {
                return false;
            }
        }
        
        return true;
    }

    private void createSessionCard(Seance session) {
        try {
            VBox card = new VBox(10);
            card.setStyle("-fx-background-color: #262626; -fx-padding: 15; -fx-background-radius: 5;");
            card.setPrefWidth(350);
            
            // Get user and coach info
            Utilisateur user = utilisateurService.getOne(session.getId_user());
            Utilisateur coach = utilisateurService.getOne(session.getId_coach());
            
            // Date and time
            Label dateLabel = new Label(session.getDate_s());
            dateLabel.setStyle("-fx-text-fill: #FF6B00; -fx-font-size: 16px;");
            
            // Client name
            String clientName = user != null ? user.getPrenom() + " " + user.getNom() : "Unknown Client";
            Label clientLabel = new Label("Client: " + clientName);
            clientLabel.setStyle("-fx-text-fill: white;");
            
            // Coach name
            String coachName = coach != null ? coach.getPrenom() + " " + coach.getNom() : "Unknown Coach";
            Label coachLabel = new Label("Coach: " + coachName);
            coachLabel.setStyle("-fx-text-fill: white;");
            
            // Type and intensity
            HBox typeBox = new HBox(10);
            Label typeLabel = new Label("Type: " + session.getType_s());
            typeLabel.setStyle("-fx-text-fill: white;");
            Label intensityLabel = new Label("Intensity: " + session.getIntensite());
            intensityLabel.setStyle("-fx-text-fill: white;");
            typeBox.getChildren().addAll(typeLabel, intensityLabel);
            
            // Duration
            Label durationLabel = new Label("Duration: " + session.getDuree_s() + " minutes");
            durationLabel.setStyle("-fx-text-fill: white;");
            
            // Status with color coding
            Label statusLabel = new Label("Status: " + session.getStatut_s());
            String statusColor = switch(session.getStatut_s().toLowerCase()) {
                case "scheduled" -> "#FFB74D";  // Orange
                case "in progress" -> "#4CAF50";  // Green
                case "completed" -> "#2196F3";   // Blue
                case "cancelled" -> "#F44336";   // Red
                default -> "white";
            };
            statusLabel.setStyle("-fx-text-fill: " + statusColor + ";");
            
            // Action buttons
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            
            JFXButton editButton = new JFXButton("Edit");
            editButton.setStyle("-fx-text-fill: #FF6B00;");
            editButton.setOnAction(e -> handleEdit(session));
            
            JFXButton deleteButton = new JFXButton("Delete");
            deleteButton.setStyle("-fx-text-fill: #F44336;");
            deleteButton.setOnAction(e -> handleDelete(session));
            
            buttonBox.getChildren().addAll(editButton, deleteButton);
            
            // Add all elements to card
            card.getChildren().addAll(
                dateLabel,
                clientLabel,
                coachLabel,
                typeBox,
                durationLabel,
                statusLabel,
                buttonBox
            );
            
            sessionCardsContainer.getChildren().add(card);
            
        } catch (SQLException e) {
            showError("Error creating session card: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddSession() {
        isEditing = false;
        currentSession = null;
        clearFields();
        sessionDialog.show();
    }

    private void handleEdit(Seance session) {
        isEditing = true;
        currentSession = session;
        populateFields(session);
        sessionDialog.show();
    }

    private void handleDelete(Seance session) {
        try {
            seanceService.supprimer(session.getId());
            refreshSessionList();
        } catch (SQLException e) {
            showError("Error deleting session: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        try {
            clearErrors();
            boolean hasErrors = false;

            Utilisateur selectedUser = userComboBox.getValue();
            if (selectedUser == null) {
                showError(userErrorLabel, "Please select a client");
                hasErrors = true;
            }

            Utilisateur selectedCoach = coachComboBox.getValue();
            if (selectedCoach == null) {
                showError(coachErrorLabel, "Please select a coach");
                hasErrors = true;
            }

            LocalDate date = dateField.getValue();
            if (date == null) {
                showError(dateErrorLabel, "Please select a date");
                hasErrors = true;
            } else {
                LocalDateTime sessionDateTime = LocalDateTime.of(
                    date,
                    LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue())
                );
                LocalDateTime now = LocalDateTime.now();
                if (sessionDateTime.isBefore(now)) {
                    showError(dateErrorLabel, "Session date must be in the future");
                    hasErrors = true;
                } else if (sessionDateTime.isAfter(now.plusMonths(6))) {
                    showError(dateErrorLabel, "Cannot schedule sessions more than 6 months in advance");
                    hasErrors = true;
                }
            }

            String type = typeField.getText();
            if (type == null || type.trim().isEmpty()) {
                showError(typeErrorLabel, "Please enter session type");
                hasErrors = true;
            }

            String duree = dureeField.getText();
            if (duree == null || duree.trim().isEmpty()) {
                showError(dureeErrorLabel, "Please enter duration");
                hasErrors = true;
            } else {
                try {
                    int duration = Integer.parseInt(duree);
                    if (duration <= 0) {
                        showError(dureeErrorLabel, "Duration must be greater than 0");
                        hasErrors = true;
                    }
                } catch (NumberFormatException e) {
                    showError(dureeErrorLabel, "Please enter a valid number");
                    hasErrors = true;
                }
            }

            String statut = statutComboBox.getValue();
            if (statut == null) {
                showError(statutErrorLabel, "Please select a status");
                hasErrors = true;
            }

            String intensite = intensiteComboBox.getValue();
            if (intensite == null) {
                showError(intensiteErrorLabel, "Please select intensity level");
                hasErrors = true;
            }

            if (hasErrors) {
                return;
            }

            // Create timestamp with date and time
            LocalDateTime dateTime = LocalDateTime.of(
                date,
                LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue())
            );
            
            Seance seance = new Seance();
            seance.setId_user(selectedUser.getId());
            seance.setId_coach(selectedCoach.getId());
            seance.setDate_s(String.valueOf(java.sql.Timestamp.valueOf(dateTime)));
            seance.setType_s(type);
            seance.setDuree_s(Integer.parseInt(duree));
            seance.setStatut_s(statut);
            seance.setIntensite(intensite);
            seance.setObjectifs(objectifsArea.getText());
            seance.setRecommandations(recommandationsArea.getText());
            
            if (isEditing && currentSession != null) {
                seance.setId(currentSession.getId());
                seanceService.modifier(seance);
            } else {
                seanceService.ajouter(seance);
            }
            
            sessionDialog.close();
            refreshSessionList();
            
        } catch (SQLException e) {
            showError(statutErrorLabel, "Database error: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancel() {
        sessionDialog.close();
    }

    private void clearFields() {
        userComboBox.getSelectionModel().clearSelection();
        coachComboBox.getSelectionModel().clearSelection();
        dateField.setValue(null);
        hourSpinner.getValueFactory().setValue(12);
        minuteSpinner.getValueFactory().setValue(0);
        typeField.clear();
        dureeField.clear();
        intensiteComboBox.getSelectionModel().clearSelection();
        objectifsArea.clear();
        recommandationsArea.clear();
        statutComboBox.getSelectionModel().clearSelection();
    }
    
    private void populateFields(Seance session) {
        try {
            userComboBox.setValue(utilisateurService.getOne(session.getId_user()));
            coachComboBox.setValue(utilisateurService.getOne(session.getId_coach()));
            
            // Parse date and time
            String[] dateTime = session.getDate_s().split(" ");
            dateField.setValue(LocalDate.parse(dateTime[0]));
            String[] time = dateTime[1].split(":");
            hourSpinner.getValueFactory().setValue(Integer.parseInt(time[0]));
            minuteSpinner.getValueFactory().setValue(Integer.parseInt(time[1]));
            
            typeField.setText(session.getType_s());
            dureeField.setText(String.valueOf(session.getDuree_s()));
            intensiteComboBox.setValue(session.getIntensite());
            objectifsArea.setText(session.getObjectifs());
            recommandationsArea.setText(session.getRecommandations());
            statutComboBox.setValue(session.getStatut_s());
            
        } catch (SQLException e) {
            showError("Error loading session details: " + e.getMessage());
        }
    }

    private void clearErrors() {
        userErrorLabel.setVisible(false);
        coachErrorLabel.setVisible(false);
        dateErrorLabel.setVisible(false);
        typeErrorLabel.setVisible(false);
        dureeErrorLabel.setVisible(false);
        intensiteErrorLabel.setVisible(false);
        objectifsErrorLabel.setVisible(false);
        recommandationsErrorLabel.setVisible(false);
        statutErrorLabel.setVisible(false);
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
