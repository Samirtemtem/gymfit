package services;

import entities.Seance;
import Utils.MyDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeanceService implements IService<Seance> {
    private Connection conx = MyDB.getConn();

    @Override
    public void ajouter(Seance seance) throws SQLException {
        String req = "INSERT INTO seance (id_user, id_coach, date_s, type_s, duree_s, statut_s, recommandations, intensite, objectifs) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setInt(1, seance.getId_user());
        ps.setInt(2, seance.getId_coach());
        ps.setString(3, seance.getDate_s());
        ps.setString(4, seance.getType_s());
        ps.setInt(5, seance.getDuree_s());
        ps.setString(6, seance.getStatut_s());
        ps.setString(7, seance.getRecommandations());
        ps.setString(8, seance.getIntensite());
        ps.setString(9, seance.getObjectifs());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Seance seance) throws SQLException {
        String req = "UPDATE seance SET id_user=?, id_coach=?, date_s=?, type_s=?, duree_s=?, statut_s=?, " +
                    "recommandations=?, intensite=?, objectifs=? WHERE id=?";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setInt(1, seance.getId_user());
        ps.setInt(2, seance.getId_coach());
        ps.setString(3, seance.getDate_s());
        ps.setString(4, seance.getType_s());
        ps.setInt(5, seance.getDuree_s());
        ps.setString(6, seance.getStatut_s());
        ps.setString(7, seance.getRecommandations());
        ps.setString(8, seance.getIntensite());
        ps.setString(9, seance.getObjectifs());
        ps.setInt(10, seance.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM seance WHERE id=?";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Seance> afficher() throws SQLException {
        List<Seance> seances = new ArrayList<>();
        String req = "SELECT * FROM seance";
        Statement st = conx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Seance seance = new Seance();
            seance.setId(rs.getInt("id"));
            seance.setId_user(rs.getInt("id_user"));
            seance.setId_coach(rs.getInt("id_coach"));
            seance.setDate_s(rs.getString("date_s"));
            seance.setType_s(rs.getString("type_s"));
            seance.setDuree_s(rs.getInt("duree_s"));
            seance.setStatut_s(rs.getString("statut_s"));
            seance.setRecommandations(rs.getString("recommandations"));
            seance.setIntensite(rs.getString("intensite"));
            seance.setObjectifs(rs.getString("objectifs"));
            seances.add(seance);
        }
        return seances;
    }

    @Override
    public Seance getOne(int id) throws SQLException {
        String req = "SELECT * FROM seance WHERE id=?";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Seance seance = new Seance();
            seance.setId(rs.getInt("id"));
            seance.setId_user(rs.getInt("id_user"));
            seance.setId_coach(rs.getInt("id_coach"));
            seance.setDate_s(rs.getString("date_s"));
            seance.setType_s(rs.getString("type_s"));
            seance.setDuree_s(rs.getInt("duree_s"));
            seance.setStatut_s(rs.getString("statut_s"));
            seance.setRecommandations(rs.getString("recommandations"));
            seance.setIntensite(rs.getString("intensite"));
            seance.setObjectifs(rs.getString("objectifs"));
            return seance;
        }
        return null;
    }
}
