package com.thu;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by jagesh on 06/03/2016.
 */
public class DBConnection
{
    public static Connection getConnection() throws ClassNotFoundException, SQLException
    {
        String driver = "com.mysql.jdbc.Driver";
        String connection = "jdbc:mysql://localhost:3306/movieanalysis";
        String user = "jagesh";
        String password = "jagesh007";

        Class.forName(driver);
        Connection con = DriverManager.getConnection(connection, user, password);
        if (con.isClosed())
        {
            con.close();
        }
        return con;
    }
}
