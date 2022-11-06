package org.furniture.services;

import org.furniture.enums.OrderStatus;
import org.furniture.models.Customer;
import org.furniture.models.Furniture;
import org.furniture.models.Material;
import org.furniture.models.Order;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DBConnect {
    private static Connection conn;

    public static void loadDriver() {
        conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/furniture-selling-system?" + "user=root");
            System.out.println("Connection Is Successful");
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }

    public static void closeDriver() throws SQLException {
        conn.close();
        System.out.println("Connection Is Closed");
    }

    private static void printResults(ResultSet rs) {
        try {
            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();

            //print head
            for (int i = 0; i < cols; i++) {
                String name = md.getColumnLabel(i + 1);
                System.out.print(name + "\t");
            }
            System.out.println();

            //print data
            while (rs.next()) {
                for (int i = 0; i < cols; i++) {
                    String value = rs.getString(i + 1);
                    System.out.print(value + "\t");
                }
                System.out.println();
            }
        } catch (SQLException e) {
            System.out.println("Error printing results: " + e.getMessage());
        }

    }

    public static ResultSet query(String codeSQL) {
        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(codeSQL);

        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        return rs;
    }

    public static void queryUpdate(String codeSQL) {
        Statement stmt = null;

        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(codeSQL);

        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }

    public static HashMap<Material, Integer> getAmountMaterialNeedAddToStock() {
        HashMap<Material, Integer> materialIntegerHashMap = new HashMap<>();
        try {
            ResultSet rs = null;
            List<String> idMaterialFirstTable = new ArrayList<>();
            rs = query("SELECT m_id,minimum - (quantity - sum_spend) need  FROM (SELECT m.id m_id,SUM(bom.spend) sum_spend,m.quantity,m.minimum FROM sale_order_list AS ol\n" +
                    "                           INNER JOIN sale_order AS so\n" +
                    "                               ON so.id = ol.fk_sale_order_id\n" +
                    "                           INNER JOIN furniture AS f\n" +
                    "                               ON f.id = ol.fk_furniture_id\n" +
                    "                           INNER JOIN bill_of_material AS bom\n" +
                    "                               ON bom.fk_furniture_id = f.id\n" +
                    "                           INNER JOIN material AS m\n" +
                    "                               ON m.id = bom.fk_material_id\n" +
                    "                          WHERE so.furniture_status = 0\n" +
                    "                          GROUP BY m.id) product_spend_table\n" +
                    "                          WHERE minimum - (quantity - sum_spend) > 0 ");
            while (rs.next()) {
                materialIntegerHashMap.put(getMaterialByID(rs.getString("m_id")),
                        rs.getInt("need"));
                idMaterialFirstTable.add(rs.getString("m_id"));
            }

            rs = query("SELECT m.id,m.minimum - m.quantity need\n" +
                    "FROM material m\n" +
                    "WHERE m.minimum > m.quantity");

            while (rs.next()) {
                if (! idMaterialFirstTable.contains(rs.getString("id")))
                    materialIntegerHashMap.put(getMaterialByID(rs.getString("id")), rs.getInt("need"));
            }
            return materialIntegerHashMap;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getIDMaterailNeedToAddToStock() {
        List<String> listID = new ArrayList<>();
        try {
            ResultSet rs = null;
            rs = query("SELECT m_id  FROM (SELECT m.id m_id,SUM(bom.spend) sum_spend,m.quantity,m.minimum FROM sale_order_list AS ol\n" +
                    "                           INNER JOIN sale_order AS so\n" +
                    "                               ON so.id = ol.fk_sale_order_id\n" +
                    "                           INNER JOIN furniture AS f\n" +
                    "                               ON f.id = ol.fk_furniture_id\n" +
                    "                           INNER JOIN bill_of_material AS bom\n" +
                    "                               ON bom.fk_furniture_id = f.id\n" +
                    "                           INNER JOIN material AS m\n" +
                    "                               ON m.id = bom.fk_material_id\n" +
                    "                          WHERE so.furniture_status = 0\n" +
                    "                          GROUP BY m.id) product_spend_table\n" +
                    "                          WHERE minimum - (quantity - sum_spend) > 0 \n" +
                    "                          UNION\n" +
                    "                          SELECT m.id\n" +
                    "                          FROM material m\n" +
                    "                          WHERE m.minimum > m.quantity");
            while (rs.next()) {
                listID.add(rs.getString("m_id"));
            }
            return listID;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Customer> createCustomersList(String query) {
        List<Customer> customerArrayList = new ArrayList<>();
        try {
            ResultSet rs = null;
            rs = query(query);
            while (rs.next()) {
                customerArrayList.add(new Customer(
                                rs.getString("id"),
                                rs.getString("name"),
                                rs.getString("address"),
                                rs.getString("phone")
                        )
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return customerArrayList;
    }

    private static Customer createCustomer(String query) {
        Customer customer = null;
        try {
            ResultSet rs = null;
            rs = query(query);
            while (rs.next()) {
                customer = new Customer(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("phone")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return customer;
    }

    public static List<Customer> getCustomers() {
        return createCustomersList("SELECT c.id,c.name,c.address,c.phone FROM customer c");
    }

    public static List<Customer> getCustomersByName(String name) {
        return createCustomersList("SELECT c.id,c.name,c.address,c.phone FROM customer c\n" +
                "WHERE c.name LIKE '%" + name + "%'");
    }

    public static Customer getCustomersByID(String id) {
        return createCustomer("SELECT c.id,c.name,c.address,c.phone FROM customer c\n" +
                "WHERE c.id=" + id);
    }

    private static List<Furniture> createFurnitureList(String query) {
        List<Furniture> furnitureArrayList = new ArrayList<>();
        try {
            ResultSet rs = null;

            rs = query(query);
            List<String> idFurnitureList = new ArrayList<>();

            while (rs.next())
                idFurnitureList.add(rs.getString("id"));

            for (String i : idFurnitureList) {
                furnitureArrayList.add(getFurnitureByID(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return furnitureArrayList;
    }

    private static Furniture createFurniture(String condition) {
        ResultSet rs = null;
        rs = query("SELECT f.id,f.cost,f.name,bom.spend,m.name m_name,m.id m_id FROM furniture f\n" +
                "INNER JOIN bill_of_material bom\n" +
                "ON bom.fk_furniture_id = f.id\n" +
                "INNER JOIN material m\n" +
                "ON m.id = bom.fk_material_id\n" +
                condition);
        Furniture furniture = null;
        try {
            while (rs.next()) {
                if (furniture == null) {
                    furniture = new Furniture(
                            rs.getString("id"),
                            rs.getString("name"),
                            Integer.parseInt(rs.getString("cost")));
                }
                furniture.addMaterial(getMaterialByID(rs.getString("m_id")),
                        rs.getInt("spend"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return furniture;
    }

    public static Furniture getFurnitureByID(String id) {
        return createFurniture("WHERE f.id =" + id);
    }

    public static List<Furniture> getFurnituresList() {
        return createFurnitureList("SELECT f.id FROM furniture f");
    }

    public static List<Furniture> getFurnituresListByName(String name) {
        return createFurnitureList("SELECT f.id FROM furniture f\n" +
                "WHERE f.name LIKE '%" + name + '%');
    }

    public static List<Furniture> getFurnitureListByOrderID(String orderID) {
        return createFurnitureList("SELECT f.id FROM furniture f\n" +
                "INNER JOIN sale_order_list sl\n" +
                "ON sl.fk_furniture_id = f.id\n" +
                "WHERE sl.fk_sale_order_id=" + orderID);
    }

    public static HashMap<Furniture, Integer> getFurnitureAmountByOrderID(String orderID) {
        HashMap<Furniture, Integer> furnitureIntegerHashMap = new HashMap<>();
        ResultSet rs = null;
        rs = query("SELECT f.id,sl.quantity FROM furniture f\n" +
                "INNER JOIN sale_order_list sl\n" +
                "ON sl.fk_furniture_id = f.id\n" +
                "WHERE sl.fk_sale_order_id=" + orderID);
        try {
            while (rs.next()) {
                furnitureIntegerHashMap.put(getFurnitureByID(rs.getString("id")),
                        rs.getInt("quantity"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return furnitureIntegerHashMap;
    }

    public static List<Material> createMaterialsList(String query) {
        List<Material> materialArrayList = new ArrayList<>();
        try {
            ResultSet rs = null;
            rs = query(query);
            while (rs.next()) {
                materialArrayList.add(new Material(
                                rs.getString("id"),
                                rs.getString("name"),
                                rs.getInt("quantity"),
                                rs.getInt("minimum")
                        )
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return materialArrayList;
    }

    public static Material createMaterial(String query) {
        Material material = null;

        try {
            ResultSet rs = null;
            rs = query(query);
            rs.next();
            material = new Material(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getInt("quantity"),
                    rs.getInt("minimum")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return material;
    }

    public static List<Material> getMaterialsList() {
        return createMaterialsList("SELECT m.id,m.name,m.quantity,m.minimum FROM material m");
    }

    public static List<Material> getMaterialsListByName(String name) {
        return createMaterialsList("SELECT m.id,m.name,m.quantity,m.minimum FROM material m\n" +
                "WHERE m.name LIKE '%" + name + "%'");
    }

    public static List<Material> getMaterialsByFurnitureID(String furnitureID) {
        return createMaterialsList("SELECT m.id,m.name,m.quantity,m.minimum FROM material m\n" +
                "INNER JOIN bill_of_material bom\n" +
                "ON bom.fk_material_id = m.id\n" +
                "WHERE bom.fk_furniture_id=" + furnitureID);
    }

    public static Material getMaterialByID(String id) {
        return createMaterial("SELECT m.id,m.name,m.quantity,m.minimum FROM material m\n" +
                "WHERE m.id=" + id);
    }

    public static List<Material> checkMaterialUnderMinimum() {
        return createMaterialsList("SELECT m.id,m.name,m.quantity,m.minimum FROM material m\n" +
                "WHERE m.quantity < m.minimum");

    }

    public static List<Order> getSaleOrdersByStatus(OrderStatus orderStatus) {
        ArrayList<Order> arrayList = new ArrayList<>();
        try {
            ResultSet rs = null;
            rs = query("SELECT so.id,so.fk_customer_id,so.c_name,so.c_address,so.cost_total,so.create_date FROM sale_order so\n" +
                    "WHERE so.furniture_status=" + orderStatus.getStatus());

            while (rs.next()) {
                arrayList.add(
                        new Order(rs.getString("id"),
                                rs.getString("c_name"),
                                rs.getInt("cost_total"),
                                rs.getString("c_address"),
                                rs.getDate("create_date"),
                                orderStatus,
                                getCustomersByID(rs.getString("ID"))
                        ));
            }
        } catch (Exception ignored) {

        }
        return arrayList;
    }

    private static List<Order> createSaleOrdersList(String query) {
        ArrayList<Order> orderArrayList = new ArrayList<>();
        try {
            ResultSet rs = null;
            rs = query(query);

            while (rs.next()) {
                orderArrayList.add(
                        new Order(rs.getString("id"),
                                rs.getString("c_name"),
                                rs.getInt("cost_total"),
                                rs.getString("c_address"),
                                rs.getDate("create_date"),
                                OrderStatus.findStatus(rs.getInt("furniture_status")),
                                getCustomersByID(rs.getString("ID"))
                        ));
            }
        } catch (Exception ignored) {

        }
        return orderArrayList;
    }

    public static List<Order> getSaleOrdersList() {
        return createSaleOrdersList("SELECT so.id,so.fk_customer_id,so.c_name,so.c_address," +
                "so.cost_total,so.create_date,so.furniture_status FROM sale_order so\n");
    }

    public static List<Order> getSaleOrdersByCName(String name) {
        return createSaleOrdersList("SELECT so.id,so.fk_customer_id,so.c_name,so.c_address,so.cost_total,so.create_date,so.furniture_status FROM sale_order so\n" +
                "WHERE c_name LIKE '%" + name + "'%");
    }

    public static boolean checkOrderCanBeConstruction(String id) {
        try {
            ResultSet rs = query("SELECT SUM(IF(quantity > sum_spend,0,-1)) check_status \n" +
                    "   FROM (SELECT m.id m_id,SUM(bom.spend) sum_spend,m.quantity FROM sale_order_list AS ol\n" +
                    "                           INNER JOIN sale_order AS so\n" +
                    "                               ON so.id = ol.fk_sale_order_id\n" +
                    "                           INNER JOIN furniture AS f\n" +
                    "                               ON f.id = ol.fk_furniture_id\n" +
                    "                           INNER JOIN bill_of_material AS bom\n" +
                    "                               ON bom.fk_furniture_id = f.id\n" +
                    "                           INNER JOIN material AS m\n" +
                    "                               ON m.id = bom.fk_material_id\n" +
                    "                          WHERE so.id = " + id + "\n" +
                    "                          GROUP BY m.id) statusTable");
            rs.next();
            return rs.getInt("check_status") >= 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void changeSaleOrderToConstruction(String orderID) {
        if (checkOrderCanBeConstruction(orderID)) {
            try {
                query("UPDATE sale_order\n" +
                        "SET furniture_status=" + OrderStatus.CONSTURCTING + "\n" +
                        "WHERE id=" + orderID);
                ResultSet rs = query("SELECT m.id m_id,SUM(bom.spend) sum_spend,m.quantity FROM sale_order_list AS ol\n" +
                        "                           INNER JOIN sale_order AS so\n" +
                        "                               ON so.id = ol.fk_sale_order_id\n" +
                        "                           INNER JOIN furniture AS f\n" +
                        "                               ON f.id = ol.fk_furniture_id\n" +
                        "                           INNER JOIN bill_of_material AS bom\n" +
                        "                               ON bom.fk_furniture_id = f.id\n" +
                        "                           INNER JOIN material AS m\n" +
                        "                               ON m.id = bom.fk_material_id\n" +
                        "                          WHERE so.id = " + orderID + "\n" +
                        "                          GROUP BY m.id"
                );

                while (rs.next()) {
                    query("UPDATE material\n" +
                            "SET quantity=" + (rs.getInt("quantity") - rs.getInt("sum_spend")) +
                            "WHERE id=" + rs.getInt("m_id"));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }
    }

    public static boolean updateOrder(String orderID, OrderStatus status) {
        try {
            queryUpdate("UPDATE sale_order\n" +
                    "SET furniture_status='" + status.getStatus() + "'\n" +
                    "WHERE id=" + orderID);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

//    public static List<Order> checkMaterialForOrderByOrderID(String id){
//        return crea
//    }

    public static void test() throws SQLException {
//        HashMap<Material,Integer> materialIntegerHashMap = getAmountMaterialNeedAddToStock();
//        for(Material m : materialIntegerHashMap.keySet()) {
//            System.out.println("M : " + m.toString());
//            System.out.println("Need : " + materialIntegerHashMap.get(m));
//        }
//        System.out.println(materialIntegerHashMap.size());
    }
}

