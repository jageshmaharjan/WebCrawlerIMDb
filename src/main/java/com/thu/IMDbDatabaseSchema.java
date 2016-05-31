package com.thu;

import java.sql.*;
import java.util.List;

/**
 * Created by jagesh on 05/27/2016.
 */
public class IMDbDatabaseSchema
{
    public static void databaseConnection(String inLinks, String title, String year, String rating, String story, String[] gene, String[] director, String[] star, String[] lang, List<String> reviews,String imglink) throws SQLException
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
            conn = DriverManager.getConnection(url+dbName,userName,password);

            String sql_movie = "INSERT INTO movie (MovieName,ReleaseDate,Rating,Storyline,url,coverpicurl) VALUES (?,?,?,?,?,?)";
            PreparedStatement prepstm_movie = conn.prepareStatement(sql_movie);

            prepstm_movie.setString(1,title);
            prepstm_movie.setInt(2, Integer.parseInt(year));
            prepstm_movie.setString(3,rating);
            prepstm_movie.setString(4,story);
            prepstm_movie.setString(5,inLinks);
            prepstm_movie.setString(6,imglink);

            prepstm_movie.execute();
            prepstm_movie.close();

            String q_mov = "SELECT * FROM movie WHERE MovieName=? ";
            PreparedStatement ps_mov = conn.prepareStatement(q_mov);
            ps_mov.setString(1,title);
            ResultSet rs_mov = ps_mov.executeQuery();
            int movie_id = 0;
            while (rs_mov.next())
            {
                movie_id = rs_mov.getInt("MovieID");
            }
            ps_mov.close();

            String sql_act = "INSERT INTO stars(Name) VALUES (?) ";
            PreparedStatement prepstm_act = conn.prepareStatement(sql_act);
            for (String s : star)
            {
                prepstm_act.setString(1,s);
                try
                {
                    prepstm_act.execute();
                }
                catch (SQLException sqlex)
                {   System.out.println("actor rec exist: ");   }
            }
            prepstm_act.close();


            String sql_mov_act = "INSERT INTO movie_actor(movieID, starID) VALUES (?,?)";
            PreparedStatement ps_mov_act = conn.prepareStatement(sql_mov_act);
            for (int i=0;i<star.length; i++)
            {
                int act_id = 0;
                String q_act = "SELECT * FROM stars WHERE Name = ?";
                PreparedStatement ps_act = conn.prepareStatement(q_act);
                ps_act.setString(1,star[i]);
                ResultSet rs_act = ps_act.executeQuery();
                while (rs_act.next())
                {
                    act_id = rs_act.getInt("idstars");
                }
                ps_mov_act.setInt(1,movie_id);
                ps_mov_act.setInt(2,act_id);
                ps_mov_act.execute();
            }
            ps_mov_act.close();

            String sql_dir = "INSERT INTO director(Name) VALUES (?)";
            PreparedStatement prepstm_dir = conn.prepareStatement(sql_dir);
            for (String d : director)
            {
                prepstm_dir.setString(1,d);
                try
                {
                    prepstm_dir.execute();
                }
                catch (SQLException sqlex)
                {   System.out.println("director rec exist: ");   }
            }
            prepstm_dir.close();

            String sql_mov_dir = "INSERT INTO movie_director(movieID, directorID) VALUES (?,?)";
            PreparedStatement ps_mov_dir = conn.prepareStatement(sql_mov_dir);
            for (int i=0;i<director.length;i++)
            {
                int dir_id = 0;
                String q_dir = "SELECT * FROM director WHERE Name=? ";
                PreparedStatement ps_dir = conn.prepareStatement(q_dir);
                ps_dir.setString(1,director[i]);
                ResultSet rs_dir = ps_dir.executeQuery();
                while (rs_dir.next())
                {
                    dir_id = rs_dir.getInt("iddirector");
                }
                ps_mov_dir.setInt(1,movie_id);
                ps_mov_dir.setInt(2,dir_id);
                ps_mov_dir.execute();
            }
            ps_mov_dir.close();

            String sql_gen = "INSERT INTO genre(genreName) VALUES (?)";
            PreparedStatement prepstm_gen = conn.prepareStatement(sql_gen);
            for (String g : gene)
            {
                prepstm_gen.setString(1,g);
                try
                {
                    prepstm_gen.execute();
                }
                catch (SQLException sqlex)
                {   System.out.println("gen. rec exist: ");   }
            }
            prepstm_gen.close();

            String sql_mov_gen = "INSERT INTO movie_genre(movieid, genreid) VALUES (?,?) ";
            PreparedStatement ps_mov_gen = conn.prepareStatement(sql_mov_gen);
            for (int i=0;i<gene.length;i++)
            {
                int gen_id = 0;
                String q_gen = "SELECT * FROM genre WHERE genreName=?";
                PreparedStatement ps_gen = conn.prepareStatement(q_gen);
                ps_gen.setString(1,gene[i]);
                ResultSet rs_gen = ps_gen.executeQuery();
                while (rs_gen.next())
                {
                    gen_id = rs_gen.getInt("idgenre");
                }
                ps_mov_gen.setInt(1,movie_id);
                ps_mov_gen.setInt(2,gen_id);
                ps_mov_gen.execute();
            }
            ps_mov_gen.close();

            String sql_lang = "INSERT INTO languages(name) VALUES (?)";
            PreparedStatement prepstm_lang = conn.prepareStatement(sql_lang);
            for (String l : lang)
            {
                prepstm_lang.setString(1,l);
                try
                {
                    prepstm_lang.execute();
                }
                catch (SQLException sqlex)
                {   System.out.println("lang. rec exist: ");   }
            }
            prepstm_lang.close();

            String sql_mov_lang = "INSERT INTO movie_language(movieID, languageID) VALUES (?,?)";
            PreparedStatement ps_mov_lang=conn.prepareStatement(sql_mov_lang);
            for (int i=0;i<lang.length; i++)
            {
                int lang_id=0;
                String q_lang = "SELECT * FROM languages WHERE name = ?";
                PreparedStatement ps_lang = conn.prepareStatement(q_lang);
                ps_lang.setString(1,lang[i]);
                ResultSet rs_lang = ps_lang.executeQuery();
                while (rs_lang.next())
                {
                    lang_id = rs_lang.getInt("idlanguage");
                }
                ps_mov_lang.setInt(1,movie_id);
                ps_mov_lang.setInt(2,lang_id);
                ps_mov_lang.execute();
            }
            ps_mov_lang.close();

            String sql_review = "INSERT INTO review(movieid, comments) VALUES (?,?) ";
            PreparedStatement prepstm_review = conn.prepareStatement(sql_review);
            for (String r : reviews)
            {
                prepstm_review.setInt(1,movie_id);
                prepstm_review.setString(2,r);
                prepstm_review.execute();
            }
            prepstm_review.close();


            conn.close();
        }
        catch (Exception e)
        {   System.out.println(e);  }


    }
}
