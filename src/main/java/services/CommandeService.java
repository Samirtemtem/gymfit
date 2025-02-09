package services;

import entities.Commande;
import entities.Panier;
import entities.Produit;
import Utils.MyDB;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommandeService implements IService<Commande> {
    private Connection conx = MyDB.getConn();
    private ProduitService produitService = new ProduitService();
    private PanierService panierService = new PanierService();

    @Override
    public void ajouter(Commande commande) throws SQLException {
        // Insert the order
        String orderReq = "INSERT INTO commande (user_id, order_date, status, total, payment_method) VALUES (?, CURRENT_TIMESTAMP, ?, ?, ?)";
        try (PreparedStatement ps = conx.prepareStatement(orderReq, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, commande.getUserId());
            ps.setString(2, commande.getStatus());
            ps.setFloat(3, commande.getTotal());
            ps.setString(4, commande.getPaymentMethod());
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    commande.setId(rs.getInt(1));
                } else {
                    throw new SQLException("Failed to get order ID");
                }
            }
        }
    }

    @Override
    public void modifier(Commande commande) throws SQLException {
        String req = "UPDATE commande SET status = ?, total = ?, payment_method = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement ps = conx.prepareStatement(req)) {
            ps.setString(1, commande.getStatus());
            ps.setFloat(2, commande.getTotal());
            ps.setString(3, commande.getPaymentMethod());
            ps.setInt(4, commande.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM commande WHERE id = ?";
        try (PreparedStatement ps = conx.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Commande> afficher() throws SQLException {
        List<Commande> orders = new ArrayList<>();
        String req = "SELECT * FROM commande ORDER BY created_at DESC";
        try (Statement st = conx.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                orders.add(mapResultSetToCommande(rs));
            }
        }
        return orders;
    }

    @Override
    public Commande getOne(int id) throws SQLException {
        String req = "SELECT * FROM commande WHERE id = ?";
        try (PreparedStatement ps = conx.prepareStatement(req)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCommande(rs);
                }
            }
        }
        return null;
    }

    public List<Commande> getUserOrders(int userId) throws SQLException {
        List<Commande> orders = new ArrayList<>();
        String req = "SELECT * FROM commande WHERE user_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = conx.prepareStatement(req)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToCommande(rs));
                }
            }
        }
        return orders;
    }

    public Commande createOrderFromCart(int userId, String paymentMethod) throws SQLException {
        conx.setAutoCommit(false);
        try {
            // Get cart items
            List<Panier> cartItems = panierService.getUserCart(userId);
            if (cartItems.isEmpty()) {
                throw new SQLException("Cart is empty");
            }

            // Calculate total
            float total = 0;
            for (Panier item : cartItems) {
                total += item.getSubtotal();
            }

            // Create order
            Commande order = new Commande(0, userId, paymentMethod);
            order.setStatus("PENDING");
            order.total = total;

            // Insert order
            ajouter(order);

            // Update product stock
            for (Panier item : cartItems) {
                if (!produitService.updateStock(item.getProductId(), item.getQuantity())) {
                    throw new SQLException("Failed to update stock for product " + item.getProductId());
                }
            }

            // Clear cart
            panierService.clearUserCart(userId);

            conx.commit();
            return order;

        } catch (SQLException e) {
            conx.rollback();
            throw e;
        } finally {
            conx.setAutoCommit(true);
        }
    }

    private Commande mapResultSetToCommande(ResultSet rs) throws SQLException {
        Commande order = new Commande(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getString("payment_method")
        );
        order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
        order.setStatus(rs.getString("status"));
        order.total = (rs.getFloat("total"));
        return order;
    }
}
