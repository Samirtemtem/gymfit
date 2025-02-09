package services;

import entities.Nutrition;
import Utils.MyDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NutritionService implements IService<Nutrition> {
    private Connection conx = MyDB.getConn();

    @Override
    public void ajouter(Nutrition nutrition) throws SQLException {
        String req = "INSERT INTO nutrition (id_user, objectif, date_debut, date_fin) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setInt(1, nutrition.getId_user());
        ps.setString(2, nutrition.getObjectif());
        ps.setString(3, nutrition.getDate_debut());
        ps.setString(4, nutrition.getDate_fin());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Nutrition nutrition) throws SQLException {
        String req = "UPDATE nutrition SET id_user=?, objectif=?, date_debut=?, date_fin=? WHERE id=?";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setInt(1, nutrition.getId_user());
        ps.setString(2, nutrition.getObjectif());
        ps.setString(3, nutrition.getDate_debut());
        ps.setString(4, nutrition.getDate_fin());
        ps.setInt(5, nutrition.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM nutrition WHERE id=?";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Nutrition> afficher() throws SQLException {
        List<Nutrition> nutritions = new ArrayList<>();
        String req = "SELECT * FROM nutrition";
        Statement st = conx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Nutrition nutrition = new Nutrition();
            nutrition.setId(rs.getInt("id"));
            nutrition.setId_user(rs.getInt("id_user"));
            nutrition.setObjectif(rs.getString("objectif"));
            nutrition.setDate_debut(rs.getString("date_debut"));
            nutrition.setDate_fin(rs.getString("date_fin"));
            nutritions.add(nutrition);
        }
        return nutritions;
    }

    @Override
    public Nutrition getOne(int id) throws SQLException {
        String req = "SELECT * FROM nutrition WHERE id=?";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Nutrition nutrition = new Nutrition();
            nutrition.setId(rs.getInt("id"));
            nutrition.setId_user(rs.getInt("id_user"));
            nutrition.setObjectif(rs.getString("objectif"));
            nutrition.setDate_debut(rs.getString("date_debut"));
            nutrition.setDate_fin(rs.getString("date_fin"));
            return nutrition;
        }
        return null;
    }
}
