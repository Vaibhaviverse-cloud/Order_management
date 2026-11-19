package com.example.library;
import java.sql.*;
import java.util.Scanner;

public class OrderManagement {

    static final String URL =
        "jdbc:mysql://localhost:3306/ecommerce_db";
    static final String USER = "root";
    static final String PASS = "20079";

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        try (Connection con =
             DriverManager.getConnection(URL, USER, PASS)) {

            System.out.println("Database Connected!");

            while (true) {
                System.out.println("\n1. Insert Orders");
                System.out.println("2. Search Orders");
                System.out.println("3. Update Quantity");
                System.out.println("4. Update Status");
                System.out.println("5. Total Order Value");
                System.out.println("6. Delete Cancelled Order");
                System.out.println("0. Exit");

                System.out.print("Enter choice: ");
                int ch = sc.nextInt();

                switch (ch) {
                    case 1: insertOrders(con); break;
                    case 2: searchOrders(con); break;
                    case 3: updateQuantity(con); break;
                    case 4: updateStatus(con); break;
                    case 5: totalValue(con); break;
                    case 6: deleteOrder(con); break;
                    case 0:
                        System.out.println("Thank You!");
                        return;
                    default:
                        System.out.println("Invalid choice!");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // A: Insert three orders
    static void insertOrders(Connection con)
            throws SQLException {

        String sql = "INSERT INTO Orders VALUES(?,?,?,?,?,?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 1; i <= 3; i++) {
                System.out.print("Order ID: ");
                ps.setInt(1, sc.nextInt());
                sc.nextLine();

                System.out.print("Customer Name: ");
                ps.setString(2, sc.nextLine());

                System.out.print("Product Name: ");
                ps.setString(3, sc.nextLine());

                System.out.print("Quantity: ");
                ps.setInt(4, sc.nextInt());

                System.out.print("Unit Price: ");
                ps.setDouble(5, sc.nextDouble());
                sc.nextLine();

                System.out.print("Status: ");
                ps.setString(6, sc.nextLine());

                try {
                    ps.executeUpdate();
                    System.out.println("Order inserted!");
                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println("Duplicate Order ID!");
                }
            }
        }
    }

    // B: Search by status and quantity > 2
    static void searchOrders(Connection con)
            throws SQLException {

        System.out.print("Enter status: ");
        sc.nextLine();
        String status = sc.nextLine();

        String sql = "SELECT * FROM Orders " +
            "WHERE status=? AND quantity>2 " +
            "ORDER BY unit_price DESC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                System.out.println(
                    rs.getInt("order_id") + " " +
                    rs.getString("customer_name") + " " +
                    rs.getString("product_name") + " " +
                    rs.getInt("quantity") + " " +
                    rs.getDouble("unit_price") + " " +
                    rs.getString("status")
                );
            }
        }
    }

    // C: Update quantity
    static void updateQuantity(Connection con)
            throws SQLException {

        System.out.print("Order ID: ");
        int id = sc.nextInt();

        System.out.print("Additional Quantity: ");
        int qty = sc.nextInt();

        if (qty <= 0) {
            System.out.println("Invalid quantity!");
            return;
        }

        String sql = "UPDATE Orders SET quantity=quantity+? WHERE order_id=?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            System.out.println(rows > 0 ?
                "Quantity Updated!" : "Order Not Found!");
        }
    }

    // D: Update valid status
    static void updateStatus(Connection con)
            throws SQLException {

        System.out.print("Order ID: ");
        int id = sc.nextInt();
        sc.nextLine();

        System.out.print("New Status: ");
        String status = sc.nextLine();

        if (!status.equalsIgnoreCase("Pending") &&
            !status.equalsIgnoreCase("Shipped") &&
            !status.equalsIgnoreCase("Delivered")) {

            System.out.println("Invalid Status!");
            return;
        }

        String sql = "UPDATE Orders SET status=? WHERE order_id=?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Status Updated!");
                String query = "SELECT * FROM Orders WHERE order_id=?";

                try (PreparedStatement p =
                     con.prepareStatement(query)) {
                    p.setInt(1, id);
                    ResultSet rs = p.executeQuery();

                    if (rs.next()) {
                        System.out.println(
                            rs.getInt("order_id") + " " +
                            rs.getString("customer_name") + " " +
                            rs.getString("status"));
                    }
                }
            } else {
                System.out.println("Order Not Found!");
            }
        }
    }

    // E: Total order value
    static void totalValue(Connection con)
            throws SQLException {

        String sql = "SELECT SUM(quantity * unit_price) AS total FROM Orders";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                System.out.println("Total Value: " +
                    rs.getDouble("total"));
            }
        }
    }

    // E: Delete only cancelled order
    static void deleteOrder(Connection con)
            throws SQLException {

        System.out.print("Enter Order ID: ");
        int id = sc.nextInt();

        String sql =
            "DELETE FROM Orders WHERE order_id=? AND status='Cancelled'";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            System.out.println(rows > 0 ?
                "Cancelled Order Deleted!" :
                "Order not found or not Cancelled!");
        }
    }
}
