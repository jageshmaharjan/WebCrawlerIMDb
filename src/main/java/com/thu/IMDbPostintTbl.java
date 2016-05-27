package com.thu;

import com.uttesh.exude.stemming.Stemmer;

import java.sql.*;
import java.util.*;

/**
 * Created by jagesh on 05/27/2016.
 */
public class IMDbPostintTbl
{
    public static void normalization(String inLinks, String title, String year, String rating, String story, String[] gene, String[] director, String[] star, String[] lang, List<String> reviews) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        String document = inLinks+" ";
        document += title+" ";
        document += year+" ";
        document += rating+" ";
        document += story+" ";
        for (String g : gene)
        {
            document += g+" ";
        }
        for (String d : director)
        {
            document += d+" ";
        }
        for (String s : star)
        {
            document += s+" ";
        }
        for (String l : lang)
        {
            document += l+" ";
        }
        for (String r : reviews)
        {
            document += r+" ";
        }

        String output = Stopwords.removeStemmedStopWords(document.toLowerCase());
        String[] words = output.split(" |\\.|\\//|\\:|\\?|\\/|\\,|\\'|\\(|\\|\\!|\\)|\\&|\\;|\\@|\\[|\\{|\\}|\\''|\\]|\\-|\\*");
        List<String> txtlst = new ArrayList<>();
        Stemmer stemmer = new Stemmer();
        for (int i=0;i<words.length; i++)
        {
            String s1 = stemmer.stem(words[i]);
            txtlst.add(words[i]);
        }
        Map<String, Integer> map = new TreeMap<>();
        for (String str : txtlst)
        {
            Integer n = map.get(str);
            n = (n==null) ?1 : ++n;
            map.put(str,n);
        }
        createPostingTbl(map,title);

    }
    public static void createPostingTbl(Map<String, Integer> map, String title) throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
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

            String q_movie = "SELECT * FROM movie WHERE MovieName=?";
            PreparedStatement pstm = conn.prepareStatement(q_movie);
            pstm.setString(1,title);
            ResultSet rs = pstm.executeQuery();
            int mov_id = 0;
            while (rs.next())
            {
                mov_id = rs.getInt("MovieID");
            }

            String q_posting = "INSERT INTO postingtbl(movieID,term,frequency) VALUES (?,?,?)";
            PreparedStatement pstm_posting = conn.prepareStatement(q_posting);
            conn.setAutoCommit(false);
            Set mapSet = map.entrySet();
            Iterator mapIterator = mapSet.iterator();
            while (mapIterator.hasNext())
            {
                Map.Entry mapentry = (Map.Entry) mapIterator.next();

                pstm_posting.setInt(1,mov_id);
                pstm_posting.setString(2, String.valueOf(mapentry.getKey()));
                pstm_posting.setInt(3, (Integer) mapentry.getValue());
                pstm_posting.addBatch();
            }
            pstm_posting.executeBatch();
            conn.commit();
            pstm_posting.close();

            conn.close();
        }
        catch (SQLException sqle)
        {
            System.out.println(sqle);
        }
    }

}
