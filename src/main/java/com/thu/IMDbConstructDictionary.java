package com.thu;

import java.sql.*;

/**
 * Created by jagesh on 05/27/2016.
 */
public class IMDbConstructDictionary
{
    public static void main(String[] args) throws Exception
    {
        double t1 = System.currentTimeMillis();
        Connection conn;
        String  url = "jdbc:mysql://localhost:3306/";
        String dbName = "movieanalysis";
        String driver = "com.mysql.jdbc.Driver";
        String userName = "user";
        String password = "admin";
        try
        {
            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(url + dbName, userName, password);

            String q_posting = "select distinct term, sum(frequency) as totalfreq, count( distinct movieID) as docs FROM movieanalysis.postingtbl group by term";
            PreparedStatement ps_posting = conn.prepareStatement(q_posting);
            ResultSet rs = ps_posting.executeQuery();
            String q_dict = "INSERT INTO dictionarytbl(term,frequency,docs) VALUES (?,?,?)";
            conn.setAutoCommit(false);
            PreparedStatement ps_dict = conn.prepareStatement(q_dict);
            while (rs.next())
            {
                ps_dict.setString(1,rs.getString("term"));
                ps_dict.setInt(2,rs.getInt("totalfreq"));
                ps_dict.setInt(3,rs.getInt("docs"));
                ps_dict.addBatch();
            }
            ps_dict.executeBatch();
            conn.commit();
            ps_dict.close();
            ps_posting.close();

            conn.close();
        }
        catch (SQLException e)
        {
            System.out.println(e);
        }

        System.out.println(System.currentTimeMillis()- t1);
    }
}
