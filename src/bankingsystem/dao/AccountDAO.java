package bankingsystem.dao;

import bankingsystem.model.Account;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {

    public void addAccount(Account account) throws Exception {
        String sql = "INSERT INTO Account (customer_id, account_type, balance) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, account.getCustomerId());
            pstmt.setString(2, account.getAccountType());
            pstmt.setDouble(3, account.getBalance());
            pstmt.executeUpdate();
        }
    }

    public ResultSet searchAccounts(String keyword) throws Exception {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT a.account_id, c.first_name, c.last_name, a.account_type, a.balance "
                + "FROM Account a JOIN Customer c ON a.customer_id = c.customer_id "
                + "WHERE c.first_name LIKE ? OR a.account_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, "%" + keyword + "%");

        try {
            pstmt.setInt(2, Integer.parseInt(keyword));
        } catch (NumberFormatException e) {
            pstmt.setInt(2, -1);
        }
        return pstmt.executeQuery();
    }

    public void addAccount(String fname, String lname, String email, String phone, String type, double balance) throws Exception {
        Connection conn = DBConnection.getConnection();
        conn.setAutoCommit(false);

        try {
            // insert customer
            String sqlCust = "INSERT INTO customer (first_name, last_name, email, phone_number) VALUES (?, ?, ?, ?)";
            PreparedStatement psCust = conn.prepareStatement(sqlCust, java.sql.Statement.RETURN_GENERATED_KEYS);
            psCust.setString(1, fname);
            psCust.setString(2, lname);
            psCust.setString(3, email);
            psCust.setString(4, phone);
            psCust.executeUpdate();

            java.sql.ResultSet rs = psCust.getGeneratedKeys();
            int customerId = 0;
            if (rs.next()) {
                customerId = rs.getInt(1);
            }

            // insert account
            String sqlAcc = "INSERT INTO account (customer_id, account_type, balance) VALUES (?, ?, ?)";
            PreparedStatement psAcc = conn.prepareStatement(sqlAcc);
            psAcc.setInt(1, customerId);
            psAcc.setString(2, type);
            psAcc.setDouble(3, balance);
            psAcc.executeUpdate();

            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.close();
        }
    }

    public void deleteAccount(int accountId) throws Exception {
        java.sql.Connection conn = bankingsystem.dao.DBConnection.getConnection();

        try {

            conn.setAutoCommit(false);

            String findCustomerSql = "SELECT customer_id FROM account WHERE account_id = ?";
            java.sql.PreparedStatement psFind = conn.prepareStatement(findCustomerSql);
            psFind.setInt(1, accountId);
            java.sql.ResultSet rs = psFind.executeQuery();

            int customerId = -1;
            if (rs.next()) {
                customerId = rs.getInt("customer_id");
            }

            String deleteTransactionsSql = "DELETE FROM transaction WHERE account_id = ?";
            java.sql.PreparedStatement psTrans = conn.prepareStatement(deleteTransactionsSql);
            psTrans.setInt(1, accountId);
            psTrans.executeUpdate();

            String deleteAccountSql = "DELETE FROM account WHERE account_id = ?";
            java.sql.PreparedStatement psAcc = conn.prepareStatement(deleteAccountSql);
            psAcc.setInt(1, accountId);
            psAcc.executeUpdate();

            if (customerId != -1) {
                String deleteCustomerSql = "DELETE FROM customer WHERE customer_id = ?";
                java.sql.PreparedStatement psCust = conn.prepareStatement(deleteCustomerSql);
                psCust.setInt(1, customerId);
                psCust.executeUpdate();
            }

            conn.commit();

        } catch (Exception e) {

            conn.rollback();
            throw new Exception("Error during deletion: " + e.getMessage());
        } finally {

            conn.setAutoCommit(true);
            conn.close();
        }
    }

    public void updateCustomerAndAccount(int accountId, String fname, String lname, String email, String phone, String type) throws Exception {
        java.sql.Connection conn = bankingsystem.dao.DBConnection.getConnection();
        conn.setAutoCommit(false);

        try {

            String findCustSql = "SELECT customer_id FROM account WHERE account_id = ?";
            java.sql.PreparedStatement psFind = conn.prepareStatement(findCustSql);
            psFind.setInt(1, accountId);
            java.sql.ResultSet rs = psFind.executeQuery();

            int customerId = 0;
            if (rs.next()) {
                customerId = rs.getInt("customer_id");
            } else {
                throw new Exception("Account not found in database!");
            }

            String updateCustSql = "UPDATE customer SET first_name = ?, last_name = ?, email = ?, phone_number = ? WHERE customer_id = ?";
            java.sql.PreparedStatement psCust = conn.prepareStatement(updateCustSql);
            psCust.setString(1, fname);
            psCust.setString(2, lname);
            psCust.setString(3, email);
            psCust.setString(4, phone);
            psCust.setInt(5, customerId);
            psCust.executeUpdate();

            String updateAccSql = "UPDATE account SET account_type = ? WHERE account_id = ?";
            java.sql.PreparedStatement psAcc = conn.prepareStatement(updateAccSql);
            psAcc.setString(1, type);
            psAcc.setInt(2, accountId);
            psAcc.executeUpdate();

            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.close();
        }
    }

    public javax.swing.table.DefaultTableModel getAccountsTableModel() throws Exception {

        java.sql.Connection conn = bankingsystem.dao.DBConnection.getConnection();

        String sql = "SELECT a.account_id, c.first_name, c.last_name, c.email, c.phone_number, a.account_type, a.balance "
                + "FROM account a JOIN customer c ON a.customer_id = c.customer_id";

        java.sql.PreparedStatement ps = conn.prepareStatement(sql);
        java.sql.ResultSet rs = ps.executeQuery();

        String[] columnNames = {"ID", "First Name", "Last Name", "Email", "Phone", "Type", "Balance"};
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(null, columnNames);

        while (rs.next()) {
            Object[] row = new Object[7];
            row[0] = rs.getInt("account_id");
            row[1] = rs.getString("first_name");
            row[2] = rs.getString("last_name");
            row[3] = rs.getString("email");
            row[4] = rs.getString("phone_number");
            row[5] = rs.getString("account_type");
            row[6] = rs.getDouble("balance");
            model.addRow(row);
        }

        conn.close();
        return model;
    }

    public double getAccountBalance(int accountId) throws Exception {
        java.sql.Connection conn = bankingsystem.dao.DBConnection.getConnection();
        String sql = "SELECT balance FROM account WHERE account_id = ?";
        java.sql.PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, accountId);

        java.sql.ResultSet rs = ps.executeQuery();
        double balance = -1; // -1 will tell us the account doesn't exist

        if (rs.next()) {
            balance = rs.getDouble("balance");
        }
        conn.close();
        return balance;
    }

    public void withdrawFromAccount(int accountId, double amount) throws Exception {
        java.sql.Connection conn = bankingsystem.dao.DBConnection.getConnection();

        try {

            String updateSql = "UPDATE account SET balance = balance - ? WHERE account_id = ?";
            java.sql.PreparedStatement psUpdate = conn.prepareStatement(updateSql);
            psUpdate.setDouble(1, amount);
            psUpdate.setInt(2, accountId);
            psUpdate.executeUpdate();

            String insertSql = "INSERT INTO transaction (account_id, transaction_type, amount, transaction_date) VALUES (?, 'Withdraw', ?, NOW())";
            java.sql.PreparedStatement psInsert = conn.prepareStatement(insertSql);
            psInsert.setInt(1, accountId);
            psInsert.setDouble(2, amount);
            psInsert.executeUpdate();

        } finally {
            conn.close();
        }
    }

    public void depositToAccount(int accountId, double amount) throws Exception {
        java.sql.Connection conn = bankingsystem.dao.DBConnection.getConnection();

        try {

            String updateSql = "UPDATE account SET balance = balance + ? WHERE account_id = ?";
            java.sql.PreparedStatement psUpdate = conn.prepareStatement(updateSql);
            psUpdate.setDouble(1, amount);
            psUpdate.setInt(2, accountId);
            psUpdate.executeUpdate();

            String insertSql = "INSERT INTO transaction (account_id, transaction_type, amount, transaction_date) VALUES (?, 'Deposit', ?, NOW())";
            java.sql.PreparedStatement psInsert = conn.prepareStatement(insertSql);
            psInsert.setInt(1, accountId);
            psInsert.setDouble(2, amount);
            psInsert.executeUpdate();

        } finally {
            conn.close();
        }
    }
    
    
}
