package services;

import entities.Utilisateur;
import Utils.MyDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurService implements IService<Utilisateur> {
    private Connection conx = MyDB.getConn();

    @Override
    public void ajouter(Utilisateur utilisateur) throws SQLException {
        String req = "INSERT INTO utilisateur (nom, prenom, email, tel, adresse, username, password, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setString(1, utilisateur.getNom());
        ps.setString(2, utilisateur.getPrenom());
        ps.setString(3, utilisateur.getEmail());
        ps.setInt(4, utilisateur.getTel());
        ps.setString(5, utilisateur.getAdresse());
        ps.setString(6, utilisateur.getUsername());
        ps.setString(7, utilisateur.getPassword());
        ps.setString(8, utilisateur.getRole().toString());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Utilisateur utilisateur) throws SQLException {
        String req = "UPDATE utilisateur SET nom=?, prenom=?, email=?, tel=?, adresse=?, username=?, password=?, role=? WHERE id=?";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setString(1, utilisateur.getNom());
        ps.setString(2, utilisateur.getPrenom());
        ps.setString(3, utilisateur.getEmail());
        ps.setInt(4, utilisateur.getTel());
        ps.setString(5, utilisateur.getAdresse());
        ps.setString(6, utilisateur.getUsername());
        ps.setString(7, utilisateur.getPassword());
        ps.setString(8, utilisateur.getRole().toString());
        ps.setInt(9, utilisateur.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM utilisateur WHERE id=?";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Utilisateur> afficher() throws SQLException {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String req = "SELECT * FROM utilisateur";
        Statement st = conx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setId(rs.getInt("id"));
            utilisateur.setNom(rs.getString("nom"));
            utilisateur.setPrenom(rs.getString("prenom"));
            utilisateur.setEmail(rs.getString("email"));
            utilisateur.setTel(rs.getInt("tel"));
            utilisateur.setAdresse(rs.getString("adresse"));
            utilisateur.setUsername(rs.getString("username"));
            utilisateur.setPassword(rs.getString("password"));
            utilisateur.setRole(Utilisateur.Role.valueOf(rs.getString("role")));
            utilisateurs.add(utilisateur);
        }
        return utilisateurs;
    }

    @Override
    public Utilisateur getOne(int id) throws SQLException {
        String req = "SELECT * FROM utilisateur WHERE id=?";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setId(rs.getInt("id"));
            utilisateur.setNom(rs.getString("nom"));
            utilisateur.setPrenom(rs.getString("prenom"));
            utilisateur.setEmail(rs.getString("email"));
            utilisateur.setTel(rs.getInt("tel"));
            utilisateur.setAdresse(rs.getString("adresse"));
            utilisateur.setUsername(rs.getString("username"));
            utilisateur.setPassword(rs.getString("password"));
            utilisateur.setRole(Utilisateur.Role.valueOf(rs.getString("role")));
            return utilisateur;
        }
        return null;
    }

    // Additional methods specific to Utilisateur
    public Utilisateur getByUsername(String username) throws SQLException {
        String req = "SELECT * FROM utilisateur WHERE username=?";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setId(rs.getInt("id"));
            utilisateur.setNom(rs.getString("nom"));
            utilisateur.setPrenom(rs.getString("prenom"));
            utilisateur.setEmail(rs.getString("email"));
            utilisateur.setTel(rs.getInt("tel"));
            utilisateur.setAdresse(rs.getString("adresse"));
            utilisateur.setUsername(rs.getString("username"));
            utilisateur.setPassword(rs.getString("password"));
            utilisateur.setRole(Utilisateur.Role.valueOf(rs.getString("role")));
            return utilisateur;
        }
        return null;
    }

    public boolean authenticate(String username, String password) throws SQLException {
        String req = "SELECT * FROM utilisateur WHERE username=? AND password=?";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setString(1, username);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }
    public Utilisateur login(String username, String password) throws SQLException {
        String req = "SELECT * FROM utilisateur WHERE username=? AND password=?";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setString(1, username);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setId(rs.getInt("id"));
            utilisateur.setNom(rs.getString("nom"));
            utilisateur.setPrenom(rs.getString("prenom"));
            utilisateur.setEmail(rs.getString("email"));
            utilisateur.setTel(rs.getInt("tel"));
            utilisateur.setAdresse(rs.getString("adresse"));
            utilisateur.setUsername(rs.getString("username"));
            utilisateur.setPassword(rs.getString("password"));
            utilisateur.setRole(Utilisateur.Role.valueOf(rs.getString("role")));
            return utilisateur;
        }
        return null;
    }
}
