package com.thu;

import io.indico.Indico;
import io.indico.api.results.IndicoResult;
import io.indico.api.utils.IndicoException;

import java.io.IOException;
import java.sql.*;
import java.util.*;
/**
 * Created by jagesh on 06/03/2016.
 */
public class SentimentScoring implements MyConstants
{
    public static void main(String[] args) throws Exception
    {
        SentimentScoring ss = new SentimentScoring();
        Map<Integer,Double> movie_sentimentscore = new HashMap<>();

        Map<Integer, List<String>> output = ss.getReviews();
        Iterator it = output.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry entry = (Map.Entry) it.next();
            double sentimentScore = ss.getSentimentScore(entry.getValue().toString());
            System.out.println(sentimentScore);
            movie_sentimentscore.put((Integer) entry.getKey(),sentimentScore);
        }
        ss.storeSentimentsonTable(movie_sentimentscore);
    }

    public Map<Integer,List<String>> getReviews() throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException
    {
        Map<Integer, List<String>>  results = new HashMap<>();

        String sql = "SELECT * FROM review WHERE movieid=?";
        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);)
        {
            for (int i=401;i<= 500;i++)
            {
                List<String> reviews = new ArrayList<>();
                ps.setInt(1,i);
                ResultSet rs = ps.executeQuery();
                while (rs.next())
                {
                    reviews.add(rs.getString("comments"));
                }
                results.put(i,reviews);
            }
        }
        return results;
    }

    public double getSentimentScore(String stringFullAll) throws IOException, IndicoException
    {
        Indico indico = new Indico(MY_API_KEY);
        String stringFull = stringFullAll.replaceAll("[^a-zA-Z0-9]"," ");

        double sentimentScore=0;
        int lines = 0;
        while ((stringFull.length() > STRING_LEN) || (stringFull.length() != 0))
        {
            String strShort = null;
            if (stringFull.length() > STRING_LEN)
            {
                strShort = (stringFull.substring(0,STRING_LEN));
                IndicoResult res = indico.sentimentHQ.predict(strShort.replaceAll("[^a-zA-Z0-9]"," "));
                Double val = res.getSentimentHQ();
                sentimentScore += val;
                lines++;
                stringFull =  stringFull.substring(STRING_LEN,stringFull.length());
            }
            else if (stringFull.length() < STRING_LEN)
            {
                strShort = (stringFull.substring(0,stringFull.length()));
                IndicoResult res = indico.sentimentHQ.predict(strShort.replaceAll("[^a-zA-Z0-9]"," "));
                Double val = res.getSentimentHQ();
                sentimentScore += val;
                lines++;
                stringFull = "";
            }
        }
        return (sentimentScore/lines);
    }

    public void storeSentimentsonTable(Map<Integer, Double> movie_sentimentscore) throws SQLException, ClassNotFoundException
    {
        String query = "INSERT INTO sentiments(movieID, score) VALUES (?,?)";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(query);)
        {
            conn.setAutoCommit(false);

            for (Map.Entry<Integer, Double> entry : movie_sentimentscore.entrySet())
            {
                ps.setInt(1, entry.getKey());
                ps.setDouble(2, entry.getValue());
                ps.addBatch();
            }

            ps.executeBatch();
            conn.commit();
        }
    }
}
