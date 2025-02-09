package entities;

public class Repas {
    private int id;
    private int id_nutrition;
    private String nom_r;
    private String type_r;
    private String description_r;
    private int calories_r;

    public Repas() {
    }

    public Repas(int id, int id_nutrition, String nom_r, String type_r, String description_r, int calories_r) {
        this.id = id;
        this.id_nutrition = id_nutrition;
        this.nom_r = nom_r;
        this.type_r = type_r;
        this.description_r = description_r;
        this.calories_r = calories_r;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_nutrition() {
        return id_nutrition;
    }

    public void setId_nutrition(int id_nutrition) {
        this.id_nutrition = id_nutrition;
    }

    public String getNom_r() {
        return nom_r;
    }

    public void setNom_r(String nom_r) {
        this.nom_r = nom_r;
    }

    public String getType_r() {
        return type_r;
    }

    public void setType_r(String type_r) {
        this.type_r = type_r;
    }

    public String getDescription_r() {
        return description_r;
    }

    public void setDescription_r(String description_r) {
        this.description_r = description_r;
    }

    public int getCalories_r() {
        return calories_r;
    }

    public void setCalories_r(int calories_r) {
        this.calories_r = calories_r;
    }

    @Override
    public String toString() {
        return "Repas{" +
                "id=" + id +
                ", id_nutrition=" + id_nutrition +
                ", nom_r='" + nom_r + '\'' +
                ", type_r='" + type_r + '\'' +
                ", description_r='" + description_r + '\'' +
                ", calories_r=" + calories_r +
                '}';
    }
}
