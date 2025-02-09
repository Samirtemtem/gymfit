package services;

import entities.Reclamation;
import Utils.MyDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReclamationService implements IService<Reclamation> {
    private Connection conx = MyDB.getConn();

    @Override
    public void ajouter(Reclamation reclamation) throws SQLException {
        String req = "INSERT INTO reclamation (id_blog, id_user, contenu_r, date_r) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setInt(1, reclamation.getId_blog());
        ps.setInt(2, reclamation.getId_user());
        ps.setString(3, reclamation.getContenu_r());
        ps.setString(4, reclamation.getDate_r());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Reclamation reclamation) throws SQLException {
        String req = "UPDATE reclamation SET id_blog=?, id_user=?, contenu_r=?, date_r=? WHERE id=?";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setInt(1, reclamation.getId_blog());
        ps.setInt(2, reclamation.getId_user());
        ps.setString(3, reclamation.getContenu_r());
        ps.setString(4, reclamation.getDate_r());
        ps.setInt(5, reclamation.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM reclamation WHERE id=?";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Reclamation> afficher() throws SQLException {
        List<Reclamation> reclamations = new ArrayList<>();
        String req = "SELECT * FROM reclamation";
        Statement st = conx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Reclamation reclamation = new Reclamation();
            reclamation.setId(rs.getInt("id"));
            reclamation.setId_blog(rs.getInt("id_blog"));
            reclamation.setId_user(rs.getInt("id_user"));
            reclamation.setContenu_r(rs.getString("contenu_r"));
            reclamation.setDate_r(rs.getString("date_r"));
            reclamations.add(reclamation);
        }
        return reclamations;
    }

    @Override
    public Reclamation getOne(int id) throws SQLException {
        String req = "SELECT * FROM reclamation WHERE id=?";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Reclamation reclamation = new Reclamation();
            reclamation.setId(rs.getInt("id"));
            reclamation.setId_blog(rs.getInt("id_blog"));
            reclamation.setId_user(rs.getInt("id_user"));
            reclamation.setContenu_r(rs.getString("contenu_r"));
            reclamation.setDate_r(rs.getString("date_r"));
            return reclamation;
        }
        return null;
    }
}
