package bankingsystem.dao;

import java.sql.*;

public class TransactionDAO {

    // deposit og withdraw
    public boolean processTransaction(int accountId, double amount, String type) throws Exception {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction for data integrity

            // 1. check balance if withdrawing
            if (type.equalsIgnoreCase("Withdraw")) {
                String checkSql = "SELECT balance FROM Account WHERE account_id = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setInt(1, accountId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getDouble("balance") < amount) {
                    return false; // insufficient funds 
                }
            }

            // 2. update balance
            String updateSql = type.equalsIgnoreCase("Deposit")
                    ? "UPDATE Account SET balance = balance + ? WHERE account_id = ?"
                    : "UPDATE Account SET balance = balance - ? WHERE account_id = ?";

            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setDouble(1, amount);
            updateStmt.setInt(2, accountId);
            updateStmt.executeUpdate();

            // 3. log transaction
            String logSql = "INSERT INTO Transaction (account_id, transaction_type, amount) VALUES (?, ?, ?)";
            PreparedStatement logStmt = conn.prepareStatement(logSql);
            logStmt.setInt(1, accountId);
            logStmt.setString(2, type);
            logStmt.setDouble(3, amount);
            logStmt.executeUpdate();

            conn.commit(); // save changees
            return true;
        }

    }

    public ResultSet getFilteredTransactions(String criteria, String keyword) throws Exception {
        Connection conn = DBConnection.getConnection();
        String sql = "";

        if (criteria.equals("Account ID")) {
            sql = "SELECT * FROM Transaction WHERE account_id = ?";
        } else if (criteria.equals("Transaction Type")) {
            sql = "SELECT * FROM Transaction WHERE transaction_type LIKE ?";
        } else {
            sql = "SELECT * FROM Transaction"; // Default: Show all
        }

        PreparedStatement pstmt = conn.prepareStatement(sql);

        if (criteria.equals("Account ID")) {
            pstmt.setInt(1, Integer.parseInt(keyword));
        } else if (criteria.equals("Transaction Type")) {
            pstmt.setString(1, "%" + keyword + "%");
        }

        return pstmt.executeQuery();
    }

    public javax.swing.table.DefaultTableModel getTransactionHistory() throws Exception {
        java.sql.Connection conn = DBConnection.getConnection();

        String sql = "SELECT transaction_id, account_id, transaction_type, amount, transaction_date FROM transaction ORDER BY transaction_date DESC";
        java.sql.PreparedStatement ps = conn.prepareStatement(sql);
        java.sql.ResultSet rs = ps.executeQuery();

        String[] columnNames = {"Transaction ID", "Account ID", "Type", "Amount", "Date & Time"};
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(columnNames, 0);

        while (rs.next()) {
            Object[] row = {
                rs.getInt("transaction_id"),
                rs.getInt("account_id"),
                rs.getString("transaction_type"),
                "$" + rs.getDouble("amount"),
                rs.getTimestamp("transaction_date")
            };
            model.addRow(row);
        }

        conn.close();
        return model;
    }
}
