package entities;

public class Seance {
    private int id;
    private int id_user;      // Client
    private int id_coach;     // Coach assigned to the session
    private String date_s;    // Date and time of the session
    private String type_s;    // Type of session
    private int duree_s;      // Duration in minutes
    private String statut_s;  // Status (Scheduled, Completed, Cancelled)
    private String recommandations; // Coach's recommendations
    private String intensite;      // Intensity level (Low, Medium, High)
    private String objectifs;      // Session objectives
    private String feedback;       // User's feedback after session
    
    public Seance() {}
    
    public Seance(int id, int id_user, int id_coach, String date_s, String type_s, 
                 int duree_s, String statut_s, String recommandations, 
                 String intensite, String objectifs, String feedback) {
        this.id = id;
        this.id_user = id_user;
        this.id_coach = id_coach;
        this.date_s = date_s;
        this.type_s = type_s;
        this.duree_s = duree_s;
        this.statut_s = statut_s;
        this.recommandations = recommandations;
        this.intensite = intensite;
        this.objectifs = objectifs;
        this.feedback = feedback;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getId_user() { return id_user; }
    public void setId_user(int id_user) { this.id_user = id_user; }
    
    public int getId_coach() { return id_coach; }
    public void setId_coach(int id_coach) { this.id_coach = id_coach; }
    
    public String getDate_s() { return date_s; }
    public void setDate_s(String date_s) { this.date_s = date_s; }
    
    public String getType_s() { return type_s; }
    public void setType_s(String type_s) { this.type_s = type_s; }
    
    public int getDuree_s() { return duree_s; }
    public void setDuree_s(int duree_s) { this.duree_s = duree_s; }
    
    public String getStatut_s() { return statut_s; }
    public void setStatut_s(String statut_s) { this.statut_s = statut_s; }
    
    public String getRecommandations() { return recommandations; }
    public void setRecommandations(String recommandations) { this.recommandations = recommandations; }
    
    public String getIntensite() { return intensite; }
    public void setIntensite(String intensite) { this.intensite = intensite; }
    
    public String getObjectifs() { return objectifs; }
    public void setObjectifs(String objectifs) { this.objectifs = objectifs; }
    
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    
    @Override
    public String toString() {
        return "Seance{" +
                "id=" + id +
                ", id_user=" + id_user +
                ", id_coach=" + id_coach +
                ", date_s='" + date_s + '\'' +
                ", type_s='" + type_s + '\'' +
                ", duree_s=" + duree_s +
                ", statut_s='" + statut_s + '\'' +
                ", recommandations='" + recommandations + '\'' +
                ", intensite='" + intensite + '\'' +
                ", objectifs='" + objectifs + '\'' +
                ", feedback='" + feedback + '\'' +
                '}';
    }
}
