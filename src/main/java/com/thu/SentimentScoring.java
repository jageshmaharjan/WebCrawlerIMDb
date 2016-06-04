package com.thu;

import io.indico.Indico;
import io.indico.api.results.IndicoResult;
import io.indico.api.utils.IndicoException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Created by jagesh on 06/03/2016.
 * Finding positiveness or negativeness -> Currently not in use, using different module
 */
public class SentimentScoring implements MyConstants
{
    public static void main(String[] args) throws Exception
    {
        SentimentScoring ss = new SentimentScoring();

        Map<Integer,Double> movie_sentimentscore = new HashMap<>();

        Map<Integer, List<String>> output = ss.getReviews();
        for (Map.Entry<Integer, List<String>> entry : output.entrySet() )
        {
            System.out.println(entry.getValue());
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
            for (int i=1;i<= 5;i++)
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
        Map params = new HashMap();
        params.put("threshold",0.25);

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
//                Map<Emotion, Double> results = indico.emotion.predict(strShort,params).getEmotion();
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
