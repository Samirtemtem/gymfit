package entities;

public class Nutrition {
    private int id;
    private int id_user;
    private String objectif;
    private String date_debut;
    private String date_fin;

    public Nutrition() {
    }

    public Nutrition(int id, int id_user, String objectif, String date_debut, String date_fin) {
        this.id = id;
        this.id_user = id_user;
        this.objectif = objectif;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    public String getObjectif() {
        return objectif;
    }

    public void setObjectif(String objectif) {
        this.objectif = objectif;
    }

    public String getDate_debut() {
        return date_debut;
    }

    public void setDate_debut(String date_debut) {
        this.date_debut = date_debut;
    }

    public String getDate_fin() {
        return date_fin;
    }

    public void setDate_fin(String date_fin) {
        this.date_fin = date_fin;
    }

    @Override
    public String toString() {
        return "Nutrition{" +
                "id=" + id +
                ", id_user=" + id_user +
                ", objectif='" + objectif + '\'' +
                ", date_debut='" + date_debut + '\'' +
                ", date_fin='" + date_fin + '\'' +
                '}';
    }
}
