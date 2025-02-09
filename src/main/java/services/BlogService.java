package services;

import entities.Blog;
import Utils.MyDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BlogService implements IService<Blog> {
    private Connection conx = MyDB.getConn();

    @Override
    public void ajouter(Blog blog) throws SQLException {
        String req = "INSERT INTO blog (id_user, titre_b, contenu_b, date_b) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setInt(1, blog.getId_user());
        ps.setString(2, blog.getTitre_b());
        ps.setString(3, blog.getContenu_b());
        ps.setString(4, blog.getDate_b());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Blog blog) throws SQLException {
        String req = "UPDATE blog SET id_user=?, titre_b=?, contenu_b=?, date_b=? WHERE id=?";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setInt(1, blog.getId_user());
        ps.setString(2, blog.getTitre_b());
        ps.setString(3, blog.getContenu_b());
        ps.setString(4, blog.getDate_b());
        ps.setInt(5, blog.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM blog WHERE id=?";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Blog> afficher() throws SQLException {
        List<Blog> blogs = new ArrayList<>();
        String req = "SELECT * FROM blog";
        Statement st = conx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Blog blog = new Blog();
            blog.setId(rs.getInt("id"));
            blog.setId_user(rs.getInt("id_user"));
            blog.setTitre_b(rs.getString("titre_b"));
            blog.setContenu_b(rs.getString("contenu_b"));
            blog.setDate_b(rs.getString("date_b"));
            blogs.add(blog);
        }
        return blogs;
    }

    @Override
    public Blog getOne(int id) throws SQLException {
        String req = "SELECT * FROM blog WHERE id=?";
        PreparedStatement ps = conx.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Blog blog = new Blog();
            blog.setId(rs.getInt("id"));
            blog.setId_user(rs.getInt("id_user"));
            blog.setTitre_b(rs.getString("titre_b"));
            blog.setContenu_b(rs.getString("contenu_b"));
            blog.setDate_b(rs.getString("date_b"));
            return blog;
        }
        return null;
    }
}
