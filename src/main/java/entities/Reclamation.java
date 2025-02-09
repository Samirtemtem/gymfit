package entities;

public class Reclamation {
    private int id;
    private int id_blog;
    private int id_user;
    private String contenu_r;
    private String date_r;

    public Reclamation() {
    }

    public Reclamation(int id, int id_blog, int id_user, String contenu_r, String date_r) {
        this.id = id;
        this.id_blog = id_blog;
        this.id_user = id_user;
        this.contenu_r = contenu_r;
        this.date_r = date_r;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_blog() {
        return id_blog;
    }

    public void setId_blog(int id_blog) {
        this.id_blog = id_blog;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    public String getContenu_r() {
        return contenu_r;
    }

    public void setContenu_r(String contenu_r) {
        this.contenu_r = contenu_r;
    }

    public String getDate_r() {
        return date_r;
    }

    public void setDate_r(String date_r) {
        this.date_r = date_r;
    }

    @Override
    public String toString() {
        return "Reclamation{" +
                "id=" + id +
                ", id_blog=" + id_blog +
                ", id_user=" + id_user +
                ", contenu_r='" + contenu_r + '\'' +
                ", date_r='" + date_r + '\'' +
                '}';
    }
}
