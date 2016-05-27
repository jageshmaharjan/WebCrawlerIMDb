package com.thu;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jagesh on 05/27/2016.
 */
public class RankingModelVSM
{
    public static void main(String[] args) //queryHandler()
    {
        String user_query = "movies related to action";

        String output = Stopwords.removeStemmedStopWords(user_query.toLowerCase());
        String[] normalized = output.split(" |\\.|\\//|\\:|\\?|\\/|\\,|\\'|\\(|\\|\\!|\\)|\\&|\\;|\\@|\\[|\\{|\\}|\\''|\\]|\\-|\\*");

        Connection conn;
        String  url = "jdbc:mysql://localhost:3306/";
        String dbName = "movieanalysis";
        String driver = "com.mysql.jdbc.Driver";
        String userName = "jagesh";
        String password = "jagesh007";
        try
        {
            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(url + dbName, userName, password);

            Set<String> alldocsId = new HashSet<>();
            for (int i=0;i<normalized.length;i++)
            {
                String sql = "SELECT * FROM postingtbl WHERE term=?";
                PreparedStatement prepstm = conn.prepareStatement(sql);
                prepstm.setString(1,normalized[i]);
                ResultSet rs = prepstm.executeQuery();
                while (rs.next())
                {
                       alldocsId.add(rs.getString("MovieID"));
                }
            }

            System.out.println(alldocsId);


        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }
}
