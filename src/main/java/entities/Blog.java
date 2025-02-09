package entities;

public class Blog {
    private int id;
    private int id_user;
    private String titre_b;
    private String contenu_b;
    private String date_b;

    public Blog() {
    }

    public Blog(int id, int id_user, String titre_b, String contenu_b, String date_b) {
        this.id = id;
        this.id_user = id_user;
        this.titre_b = titre_b;
        this.contenu_b = contenu_b;
        this.date_b = date_b;
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

    public String getTitre_b() {
        return titre_b;
    }

    public void setTitre_b(String titre_b) {
        this.titre_b = titre_b;
    }

    public String getContenu_b() {
        return contenu_b;
    }

    public void setContenu_b(String contenu_b) {
        this.contenu_b = contenu_b;
    }

    public String getDate_b() {
        return date_b;
    }

    public void setDate_b(String date_b) {
        this.date_b = date_b;
    }

    @Override
    public String toString() {
        return "Blog{" +
                "id=" + id +
                ", id_user=" + id_user +
                ", titre_b='" + titre_b + '\'' +
                ", contenu_b='" + contenu_b + '\'' +
                ", date_b='" + date_b + '\'' +
                '}';
    }
}
