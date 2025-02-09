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
        conx.setAutoCommit(false);
        try {
            // Insert the order
            String orderReq = "INSERT INTO commande (user_id, order_date, status, total, payment_method) VALUES (?, ?, ?, ?, ?)";
            int orderId;
            try (PreparedStatement ps = conx.prepareStatement(orderReq, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, commande.getUserId());
                ps.setTimestamp(2, Timestamp.valueOf(commande.getOrderDate()));
                ps.setString(3, commande.getStatus());
                ps.setFloat(4, commande.getTotal());
                ps.setString(5, commande.getPaymentMethod());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        orderId = rs.getInt(1);
                        commande.setId(orderId);
                    } else {
                        throw new SQLException("Failed to get order ID");
                    }
                }
            }

            // Insert order items
            String itemReq = "INSERT INTO commande_items (commande_id, product_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conx.prepareStatement(itemReq)) {
                for (Commande.CommandeItem item : commande.getItems()) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, item.getProductId());
                    ps.setInt(3, item.getQuantity());
                    ps.setFloat(4, item.getUnitPrice());
                    ps.setFloat(5, item.getSubtotal());
                    ps.executeUpdate();

                    // Update product stock
                    if (!produitService.updateStock(item.getProductId(), item.getQuantity())) {
                        throw new SQLException("Failed to update stock for product " + item.getProductId());
                    }
                }
            }

            conx.commit();
        } catch (SQLException e) {
            conx.rollback();
            throw e;
        } finally {
            conx.setAutoCommit(true);
        }
    }

    @Override
    public void modifier(Commande commande) throws SQLException {
        String req = "UPDATE commande SET status=?, payment_method=? WHERE id=?";
        try (PreparedStatement ps = conx.prepareStatement(req)) {
            ps.setString(1, commande.getStatus());
            ps.setString(2, commande.getPaymentMethod());
            ps.setInt(3, commande.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        // Note: Due to ON DELETE CASCADE, this will also delete order items
        String req = "DELETE FROM commande WHERE id=?";
        try (PreparedStatement ps = conx.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Commande> afficher() throws SQLException {
        List<Commande> orders = new ArrayList<>();
        String req = "SELECT * FROM commande ORDER BY order_date DESC";
        try (Statement st = conx.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                Commande order = mapResultSetToCommande(rs);
                loadOrderItems(order);
                orders.add(order);
            }
        }
        return orders;
    }

    @Override
    public Commande getOne(int id) throws SQLException {
        return null;
    }

    public List<Commande> getUserOrders(int userId) throws SQLException {
        List<Commande> orders = new ArrayList<>();
        String req = "SELECT * FROM commande WHERE user_id = ? ORDER BY order_date DESC";
        try (PreparedStatement ps = conx.prepareStatement(req)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Commande order = mapResultSetToCommande(rs);
                    loadOrderItems(order);
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    public Commande createOrderFromCart(int userId, String paymentMethod) throws SQLException {
        List<Panier> cartItems = panierService.getUserCart(userId);
        if (cartItems.isEmpty()) {
            throw new SQLException("Cart is empty");
        }

        Commande order = new Commande(0, userId, paymentMethod);
        for (Panier item : cartItems) {
            order.addItem(item);
        }

        ajouter(order);
        panierService.clearUserCart(userId);
        return order;
    }

    private Commande mapResultSetToCommande(ResultSet rs) throws SQLException {
        Commande order = new Commande(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getString("payment_method")
        );
        order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
        order.setStatus(rs.getString("status"));
        return order;
    }

    private void loadOrderItems(Commande order) throws SQLException {
        String req = "SELECT * FROM commande_items WHERE commande_id = ?";
        try (PreparedStatement ps = conx.prepareStatement(req)) {
            ps.setInt(1, order.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    order.addItem(new Panier(
                        0, // Cart item ID not needed
                        order.getUserId(),
                        rs.getInt("product_id"),
                        rs.getInt("quantity"),
                        rs.getFloat("unit_price")
                    ));
                }
            }
        }
    }
}
