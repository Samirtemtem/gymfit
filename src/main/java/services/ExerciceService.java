package services;

import entities.Exercice;
import Utils.MyDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExerciceService implements IService<Exercice> {
    private Connection conx = MyDB.getConn();

    @Override
    public void ajouter(Exercice exercice) throws SQLException {
        String req = "INSERT INTO exercice (id_seance, nom_e, description_e, duree_e) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setInt(1, exercice.getId_seance());
        ps.setString(2, exercice.getNom_e());
        ps.setString(3, exercice.getDescription_e());
        ps.setInt(4, exercice.getDuree_e());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Exercice exercice) throws SQLException {
        String req = "UPDATE exercice SET id_seance=?, nom_e=?, description_e=?, duree_e=? WHERE id=?";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setInt(1, exercice.getId_seance());
        ps.setString(2, exercice.getNom_e());
        ps.setString(3, exercice.getDescription_e());
        ps.setInt(4, exercice.getDuree_e());
        ps.setInt(5, exercice.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM exercice WHERE id=?";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Exercice> afficher() throws SQLException {
        List<Exercice> exercices = new ArrayList<>();
        String req = "SELECT * FROM exercice";
        Statement st = conx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Exercice exercice = new Exercice();
            exercice.setId(rs.getInt("id"));
            exercice.setId_seance(rs.getInt("id_seance"));
            exercice.setNom_e(rs.getString("nom_e"));
            exercice.setDescription_e(rs.getString("description_e"));
            exercice.setDuree_e(rs.getInt("duree_e"));
            exercices.add(exercice);
        }
        return exercices;
    }

    @Override
    public Exercice getOne(int id) throws SQLException {
        String req = "SELECT * FROM exercice WHERE id=?";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Exercice exercice = new Exercice();
            exercice.setId(rs.getInt("id"));
            exercice.setId_seance(rs.getInt("id_seance"));
            exercice.setNom_e(rs.getString("nom_e"));
            exercice.setDescription_e(rs.getString("description_e"));
            exercice.setDuree_e(rs.getInt("duree_e"));
            return exercice;
        }
        return null;
    }
}
