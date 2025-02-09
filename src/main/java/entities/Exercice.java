package entities;

public class Exercice {
    private int id;
    private int id_seance;
    private String nom_e;
    private String description_e;
    private int duree_e;

    public Exercice() {
    }

    public Exercice(int id, int id_seance, String nom_e, String description_e, int duree_e) {
        this.id = id;
        this.id_seance = id_seance;
        this.nom_e = nom_e;
        this.description_e = description_e;
        this.duree_e = duree_e;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_seance() {
        return id_seance;
    }

    public void setId_seance(int id_seance) {
        this.id_seance = id_seance;
    }

    public String getNom_e() {
        return nom_e;
    }

    public void setNom_e(String nom_e) {
        this.nom_e = nom_e;
    }

    public String getDescription_e() {
        return description_e;
    }

    public void setDescription_e(String description_e) {
        this.description_e = description_e;
    }

    public int getDuree_e() {
        return duree_e;
    }

    public void setDuree_e(int duree_e) {
        this.duree_e = duree_e;
    }

    @Override
    public String toString() {
        return "Exercice{" +
                "id=" + id +
                ", id_seance=" + id_seance +
                ", nom_e='" + nom_e + '\'' +
                ", description_e='" + description_e + '\'' +
                ", duree_e=" + duree_e +
                '}';
    }
}
