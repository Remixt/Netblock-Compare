package remixt;

import java.sql.*;

public class HiveJdbcClient {
    private static String driverName = "org.apache.hive.jdbc.HiveDriver";
    private Connection con;
    private Statement stmt;
    private ResultSet res;

    public HiveJdbcClient(String user, String pass) throws SQLException{
        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
        con = DriverManager.getConnection("jdbc:hive2://tbm1:10000", user, pass);
        stmt = con.createStatement();
        stmt.execute("USE sta");
        stmt.execute("add jar  /home/cbrant/intzsta-1.0-SNAPSHOT.jar");
    }

    public ResultSet getQueryResult(String query){
        try {
             res = stmt.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

}