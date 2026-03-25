package com.example.mavenproject3;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BalanceDAO {

    // Option 2: Register a new account with 1000 Rs
    public boolean createNewAccount(int userId, String type, double initialDeposit) {
        String sql = "INSERT INTO balance (user_id, account_type, a_balance) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, type);
            pstmt.setDouble(3, initialDeposit);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (Exception ex) {
            System.getLogger(BalanceDAO.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        return false;
    }

    public List<String> getUserAccounts(int userId) throws Exception {
        List<String> accounts = new ArrayList<>();
        String sql = "SELECT account_id, account_type FROM balance WHERE user_id = ?";
    
         try (Connection conn = DatabaseConnection.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
                pstmt.setInt(1, userId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    accounts.add(rs.getString("account_id") + " (" + rs.getString("account_type") + ")");
                }
              } catch (SQLException e) {
                    e.printStackTrace();
                }
        return accounts;
    }
    
    // Pass BOTH account IDs directly instead of just the userId
    public String executeSelfTransfer(String fromAccId, String toAccId, double amount) throws Exception {
        String withdrawSql = "UPDATE balance SET a_balance = a_balance - ? WHERE account_id = ?";
        String depositSql = "UPDATE balance SET a_balance = a_balance + ? WHERE account_id = ?";
    
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // ACID property

            try (PreparedStatement psW = conn.prepareStatement(withdrawSql);
                 PreparedStatement psD = conn.prepareStatement(depositSql)) {
            
            // 1. Deduct from selected Source
                psW.setDouble(1, amount);
                psW.setLong(2, Long.parseLong(fromAccId));
                psW.executeUpdate();

            // 2. Add to selected Destination
                psD.setDouble(1, amount);
                psD.setLong(2, Long.parseLong(toAccId));
                psD.executeUpdate();

            // 3. Log it (Using transaction_type to avoid your previous error)
                String log = "INSERT INTO transactions (sender_account_id, receiver_account_id, transaction_type, amount) VALUES (?, ?, 'TRANSFER', ?)";
                try (PreparedStatement ps = conn.prepareStatement(log)) {
                   ps.setLong(1, Long.parseLong(fromAccId)); 
                   ps.setLong(2, Long.parseLong(toAccId));
                   ps.setDouble(3, amount);
                   ps.executeUpdate();
                }

            conn.commit();
            return "Self-Transfer Successful!";
        } catch (Exception e) {
            conn.rollback();
            return "Error: " + e.getMessage();
        }
    } catch (SQLException e) {
        return "Database Connection Error.";
    }
}


    // Option 3: Transfer Money with Validations
    public String executeTransfer(int senderUserId, String receiverAccNum, double amount) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start Transaction (ACID)

            // 1. Get Sender's Account ID and Balance
            long senderAccId = -1;
            double senderBalance = 0;
            String senderQuery = "SELECT account_id, a_balance FROM balance WHERE user_id = ? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(senderQuery)) {
                ps.setInt(1, senderUserId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    senderAccId = rs.getLong("account_id");
                    senderBalance = rs.getDouble("a_balance");
                }
            }

            // Validation: Enough money?
            if (senderBalance < amount) {
                return "Error: Not enough balance (Current: ₹" + senderBalance + ")";
            }

            // 2. Validation: Does Receiver exist?
            long receiverAccId = -1;
            String receiverQuery = "SELECT account_id FROM balance WHERE account_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(receiverQuery)) {
                ps.setString(1, receiverAccNum);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    receiverAccId = rs.getLong("account_id");
                } else {
                    return "Error: Receiver account number does not exist.";
                }
            }

            // 3. Subtract from Sender
            String withdraw = "UPDATE balance SET a_balance = a_balance - ? WHERE account_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(withdraw)) {
                ps.setDouble(1, amount);
                ps.setLong(2, senderAccId);
                ps.executeUpdate();
            }

            // 4. Add to Receiver
            String deposit = "UPDATE balance SET a_balance = a_balance + ? WHERE account_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deposit)) {
                ps.setDouble(1, amount);
                ps.setLong(2, receiverAccId);
                ps.executeUpdate();
            }

            // 5. Log the Transaction (For Option 1)
            String log = "INSERT INTO transactions (sender_account_id, receiver_account_id, transaction_type, amount) VALUES (?, ?, 'TRANSFER', ?)";
            try (PreparedStatement ps = conn.prepareStatement(log)) {
                ps.setLong(1, senderAccId);
                ps.setLong(2, receiverAccId);
                ps.setDouble(3, amount);
                ps.executeUpdate();
            }

            conn.commit();
            return "Transaction Successful!";

       } catch (Exception e) {
            e.printStackTrace(); // This prints the EXACT error to the NetBeans/Eclipse console
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
                return "Transaction Failed: " + e.getMessage(); 
        }
    }

    public String getLastFiveTransactions(int userId) throws Exception {
    StringBuilder history = new StringBuilder("--- Last 5 Transactions ---\n\n");
    
    // SQL using a JOIN to find transactions belonging to this user's account
    String sql = "SELECT t.transaction_type, t.amount, t.timestamp, t.receiver_account_id " +
                 "FROM transactions t " +
                 "JOIN balance b ON (t.sender_account_id = b.account_id OR t.receiver_account_id = b.account_id) " +
                 "WHERE b.user_id = ? " +
                 "ORDER BY t.timestamp DESC LIMIT 5";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setInt(1, userId);
        ResultSet rs = pstmt.executeQuery();

        boolean hasData = false;
        while (rs.next()) {
            hasData = true;
            String type = rs.getString("transaction_type");
            double amt = rs.getDouble("amount");
            String time = rs.getTimestamp("timestamp").toString();
            long target = rs.getLong("receiver_account_id");

            history.append(String.format("[%s] %s: ₹%.2f", time.substring(0, 16), type, amt));
            if (type.equals("TRANSFER")) {
                history.append(" (To Acc: ").append(target).append(")");
            }
            history.append("\n");
        }

        if (!hasData) {
            return "No transaction history found.";
        }

    } catch (SQLException e) {
        e.printStackTrace();
        return "Error fetching history.";
    }
    
    return history.toString();
}
    public String getAllAccountDetails(int userId) throws Exception {
        StringBuilder details = new StringBuilder("--- Your Account Details ---\n\n");
    // SQL selects all columns from balance for the specific user
        String sql = "SELECT account_id, account_type, a_balance FROM balance WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
                pstmt.setInt(1, userId);
                ResultSet rs = pstmt.executeQuery();

                boolean found = false;
                while (rs.next()) {
                    found = true;
                    long accId = rs.getLong("account_id");
                    String type = rs.getString("account_type");
                    double bal = rs.getDouble("a_balance");

                    details.append("Account Number: ").append(accId).append("\n");
                    details.append("Type: ").append(type).append("\n");
                    details.append("Current Balance: ₹").append(String.format("%.2f", bal)).append("\n");
                    details.append("----------------------------\n");
                }   

                if (!found) return "No accounts associated with this user.";

            } catch (SQLException e) {
                    e.printStackTrace();
                    return "Error retrieving account details.";
                    }
        return details.toString();
    }
}