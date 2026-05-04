package bankingsystem.dao;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    
    private static final String URL = "jdbc:mysql://localhost:3306/BankingSystemDB";
    private static final String USER = "root"; 
    private static final String PASS = "Karlnicoli123"; //password nako

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}