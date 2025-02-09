package services;

import entities.Panier;
import entities.Produit;
import Utils.MyDB;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PanierService implements IService<Panier> {
    private Connection conx = MyDB.getConn();
    private ProduitService produitService = new ProduitService();

    @Override
    public void ajouter(Panier panier) throws SQLException {
        // First check if product has enough stock
        Produit produit = produitService.findById(panier.getProductId());
        if (produit == null || !produit.hasEnoughStock(panier.getQuantity())) {
            throw new SQLException("Not enough stock available");
        }

        String req = "INSERT INTO panier (user_id, product_id, quantity, unit_price, subtotal, added_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, panier.getUserId());
            ps.setInt(2, panier.getProductId());
            ps.setInt(3, panier.getQuantity());
            ps.setFloat(4, panier.getUnitPrice());
            ps.setFloat(5, panier.getSubtotal());
            ps.setTimestamp(6, Timestamp.valueOf(panier.getAddedDate()));
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    panier.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void modifier(Panier panier) throws SQLException {
        // Check if new quantity is available
        Produit produit = produitService.findById(panier.getProductId());
        if (produit == null || !produit.hasEnoughStock(panier.getQuantity())) {
            throw new SQLException("Not enough stock available");
        }

        String req = "UPDATE panier SET quantity=?, unit_price=?, subtotal=? WHERE id=?";
        try (PreparedStatement ps = conx.prepareStatement(req)) {
            ps.setInt(1, panier.getQuantity());
            ps.setFloat(2, panier.getUnitPrice());
            ps.setFloat(3, panier.getSubtotal());
            ps.setInt(4, panier.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM panier WHERE id=?";
        try (PreparedStatement ps = conx.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Panier> afficher() throws SQLException {
        List<Panier> items = new ArrayList<>();
        String req = "SELECT * FROM panier";
        try (Statement st = conx.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                items.add(mapResultSetToPanier(rs));
            }
        }
        return items;
    }

    public List<Panier> getUserCart(int userId) throws SQLException {
        List<Panier> items = new ArrayList<>();
        String req = "SELECT * FROM panier WHERE user_id = ?";
        try (PreparedStatement ps = conx.prepareStatement(req)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToPanier(rs));
                }
            }
        }
        return items;
    }

    public float getCartTotal(int userId) throws SQLException {
        String req = "SELECT SUM(subtotal) as total FROM panier WHERE user_id = ?";
        try (PreparedStatement ps = conx.prepareStatement(req)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getFloat("total");
                }
            }
        }
        return 0;
    }

    public void clearUserCart(int userId) throws SQLException {
        String req = "DELETE FROM panier WHERE user_id = ?";
        try (PreparedStatement ps = conx.prepareStatement(req)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private Panier mapResultSetToPanier(ResultSet rs) throws SQLException {
        Panier item = new Panier(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getInt("product_id"),
            rs.getInt("quantity"),
            rs.getFloat("unit_price")
        );
        item.setAddedDate(rs.getTimestamp("added_date").toLocalDateTime());
        return item;
    }

    @Override
    public Panier getOne(int id) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getOne'");
    }
}
