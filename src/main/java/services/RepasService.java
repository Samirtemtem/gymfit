package services;

import entities.Repas;
import Utils.MyDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RepasService implements IService<Repas> {
    private Connection conx = MyDB.getConn();

    @Override
    public void ajouter(Repas repas) throws SQLException {
        String req = "INSERT INTO repas (id_nutrition, nom_r, type_r, description_r, calories_r) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setInt(1, repas.getId_nutrition());
        ps.setString(2, repas.getNom_r());
        ps.setString(3, repas.getType_r());
        ps.setString(4, repas.getDescription_r());
        ps.setInt(5, repas.getCalories_r());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Repas repas) throws SQLException {
        String req = "UPDATE repas SET id_nutrition=?, nom_r=?, type_r=?, description_r=?, calories_r=? WHERE id=?";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setInt(1, repas.getId_nutrition());
        ps.setString(2, repas.getNom_r());
        ps.setString(3, repas.getType_r());
        ps.setString(4, repas.getDescription_r());
        ps.setInt(5, repas.getCalories_r());
        ps.setInt(6, repas.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM repas WHERE id=?";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Repas> afficher() throws SQLException {
        List<Repas> repas = new ArrayList<>();
        String req = "SELECT * FROM repas";
        Statement st = conx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Repas r = new Repas();
            r.setId(rs.getInt("id"));
            r.setId_nutrition(rs.getInt("id_nutrition"));
            r.setNom_r(rs.getString("nom_r"));
            r.setType_r(rs.getString("type_r"));
            r.setDescription_r(rs.getString("description_r"));
            r.setCalories_r(rs.getInt("calories_r"));
            repas.add(r);
        }
        return repas;
    }

    @Override
    public Repas getOne(int id) throws SQLException {
        String req = "SELECT * FROM repas WHERE id=?";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Repas repas = new Repas();
            repas.setId(rs.getInt("id"));
            repas.setId_nutrition(rs.getInt("id_nutrition"));
            repas.setNom_r(rs.getString("nom_r"));
            repas.setType_r(rs.getString("type_r"));
            repas.setDescription_r(rs.getString("description_r"));
            repas.setCalories_r(rs.getInt("calories_r"));
            return repas;
        }
        return null;
    }
}
