package services;

import entities.Produit;
import Utils.MyDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitService implements IService<Produit> {
    private Connection conx = MyDB.getConn();

    @Override
    public void ajouter(Produit produit) throws SQLException {
        String req = "INSERT INTO produit (name, description, price, stock, category, image) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, produit.getName());
            ps.setString(2, produit.getDescription());
            ps.setFloat(3, produit.getPrice());
            ps.setInt(4, produit.getStock());
            ps.setString(5, produit.getCategory());
            ps.setBytes(6, produit.getImage());
            ps.executeUpdate();
            
            // Get the generated ID
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    produit.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void modifier(Produit produit) throws SQLException {
        String req = "UPDATE produit SET name=?, description=?, price=?, stock=?, category=?, image=? WHERE id=?";
        try (PreparedStatement ps = conx.prepareStatement(req)) {
            ps.setString(1, produit.getName());
            ps.setString(2, produit.getDescription());
            ps.setFloat(3, produit.getPrice());
            ps.setInt(4, produit.getStock());
            ps.setString(5, produit.getCategory());
            ps.setBytes(6, produit.getImage());
            ps.setInt(7, produit.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM produit WHERE id=?";
        try (PreparedStatement ps = conx.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Produit> afficher() throws SQLException {
        List<Produit> produits = new ArrayList<>();
        String req = "SELECT * FROM produit";
        try (Statement st = conx.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                Produit p = new Produit(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getFloat("price"),
                    rs.getInt("stock"),
                    rs.getString("category"),
                    rs.getBytes("image")
                );
                produits.add(p);
            }
        }
        return produits;
    }

    @Override
    public Produit getOne(int id) throws SQLException {
        String req = "SELECT * FROM produit WHERE id=?";
        try (PreparedStatement ps = conx.prepareStatement(req)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Produit produit = new Produit(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getFloat("price"),
                        rs.getInt("stock"),
                        rs.getString("category"),
                        rs.getBytes("image")
                    );
                    return produit;
                }
            }
        }
        return null;
    }

    // Additional methods for stock management
    public boolean updateStock(int productId, int quantity) throws SQLException {
        String req = "UPDATE produit SET stock = stock - ? WHERE id = ? AND stock >= ?";
        try (PreparedStatement ps = conx.prepareStatement(req)) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            return ps.executeUpdate() > 0;
        }
    }

    public Produit findById(int id) throws SQLException {
        String req = "SELECT * FROM produit WHERE id = ?";
        try (PreparedStatement ps = conx.prepareStatement(req)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Produit(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getFloat("price"),
                        rs.getInt("stock"),
                        rs.getString("category"),
                        rs.getBytes("image")
                    );
                }
            }
        }
        return null;
    }

    public List<Produit> findByCategory(String category) throws SQLException {
        List<Produit> produits = new ArrayList<>();
        String req = "SELECT * FROM produit WHERE category = ?";
        try (PreparedStatement ps = conx.prepareStatement(req)) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Produit p = new Produit(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getFloat("price"),
                        rs.getInt("stock"),
                        rs.getString("category"),
                        rs.getBytes("image")
                    );
                    produits.add(p);
                }
            }
        }
        return produits;
    }
}
