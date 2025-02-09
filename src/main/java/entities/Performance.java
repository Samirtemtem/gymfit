package entities;

import java.sql.Date;

public class Performance {
    private int id;
    private int id_seance;
    private int id_user;
    private String notes;          // Coach's notes and recommendations
    private int niveau_effort;     // 1-10 scale
    private int progression;       // Percentage improvement
    private Date date;
    private String commentaires;   // User's feedback
    
    public Performance() {}
    
    public Performance(int id, int id_seance, int id_user, String notes, 
                      int niveau_effort, int progression, Date date, String commentaires) {
        this.id = id;
        this.id_seance = id_seance;
        this.id_user = id_user;
        this.notes = notes;
        this.niveau_effort = niveau_effort;
        this.progression = progression;
        this.date = date;
        this.commentaires = commentaires;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getId_seance() { return id_seance; }
    public void setId_seance(int id_seance) { this.id_seance = id_seance; }
    
    public int getId_user() { return id_user; }
    public void setId_user(int id_user) { this.id_user = id_user; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public int getNiveau_effort() { return niveau_effort; }
    public void setNiveau_effort(int niveau_effort) { this.niveau_effort = niveau_effort; }
    
    public int getProgression() { return progression; }
    public void setProgression(int progression) { this.progression = progression; }
    
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    
    public String getCommentaires() { return commentaires; }
    public void setCommentaires(String commentaires) { this.commentaires = commentaires; }
}
