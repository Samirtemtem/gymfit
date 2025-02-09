package Utils;

import java.sql.*;
public class MyDB {
    private static Connection conn = null;
    private static String url = "jdbc:mysql://localhost:3306/esprit";
    private static String user="root";
    private static String pwd="";
    private static MyDB D;
    private MyDB()
    {
        try {
            conn = DriverManager.getConnection(url,user,pwd);
            System.out.println("connexion etablie");

        } catch(SQLException e) {
            System.out.println(e);
        }
    }
    public static Connection getConn()
    {
        if(conn==null)
            new MyDB();
        return conn;

    }
}