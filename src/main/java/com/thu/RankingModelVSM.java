package com.thu;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * Created by jagesh on 05/27/2016.
 */
public class RankingModelVSM
{
    public static void main(String[] args) //queryHandler()
    {
        String user_query = "comedy";

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

            int N = 0;
            Set<Integer> alldocsId = new HashSet<>();
            for (int i=0;i<normalized.length;i++)
            {
                String sql = "SELECT DISTINCT movieID FROM postingtbl WHERE term=?";
                PreparedStatement prepstm = conn.prepareStatement(sql);
                prepstm.setString(1,normalized[i]);
                ResultSet rs = prepstm.executeQuery();
                while (rs.next())
                {
                    alldocsId.add(rs.getInt("MovieID"));
                }
                prepstm.close();
            }
            N = alldocsId.size();
            System.out.println(N);

            double[] doclen = new double[N];
            //List<Double> doclen = new ArrayList<>();
            for (int i=0; i<normalized.length; i++)
            {
                String sql = "SELECT DISTINCT movieID, sum(frequency) as freq FROM postingtbl where term=? GROUP by movieID";
                PreparedStatement prepstm = conn.prepareStatement(sql);
                prepstm.setString(1,normalized[i]);
                ResultSet rs = prepstm.executeQuery();
                int k =0;
                while (rs.next())
                {
                    doclen[k] = (rs.getDouble("freq"));
                    k++;
                }
                prepstm.close();
            }

            int sum = 0;
            float avdl = 0;
            for (int k=0; k<doclen.length; k++)
            {
                sum += doclen[k];
            }
            avdl = (float) sum/N;

            int[][] termfreq = new int[normalized.length][N];
            float df = 0;
            for (int i=0; i<normalized.length; i++)
            {
                int m = 0;
                String sql = "SELECT DISTINCT movieID, sum(frequency) as freq FROM postingtbl WHERE term=? GROUP BY movieID";
                PreparedStatement prepstm = conn.prepareStatement(sql);
                prepstm.setString(1,normalized[i]);
                ResultSet rs = prepstm.executeQuery();
                while (rs.next())
                {
                    if (rs.getInt("freq") != 0)
                        df++;
                    termfreq[i][m] = rs.getInt("freq");
                    m++;
                }
            }

            float IDF = (float) Math.log((N+1)/(df/normalized.length));

            int[] tf = new int[N];
            for (int i=0;i<termfreq[0].length;i++)
            {
                for (int j=0;j<termfreq.length; j++)
                {
                    tf[i] += termfreq[j][i];
                }
            }
            System.out.println(Arrays.toString(tf));

            List<Integer> alldocIDlst = new ArrayList<>();
            Iterator it = alldocsId.iterator();
            while (it.hasNext())
            {
                alldocIDlst.add(Integer.valueOf(it.next().toString()));
            }
            System.out.println(alldocIDlst);

            calculateVSM(tf,doclen,IDF,avdl,alldocIDlst);

        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }

    public static void calculateVSM(int[] tf, double[] doclen, float idf, float avdl, List<Integer> alldocsId)
    {
        float s = (float) 0.2;
        float[] vsm = new float[tf.length];
        Map<Float,String> docRank = new TreeMap<>(Comparator.reverseOrder());
        for (int i=0;i<tf.length;i++)
        {
            if (tf[i] > 0)
            {
                vsm[i] = (float) ((1+Math.log(tf[i]))/((1-s)+s*(doclen[i] / avdl)) *idf);
                docRank.put(vsm[i], alldocsId.get(i).toString());
            }
            else
            {
                vsm[i] = (float) 0.0;
                docRank.put(vsm[i],alldocsId.get(i).toString());
            }
        }

        System.out.println(docRank);
    }
}
