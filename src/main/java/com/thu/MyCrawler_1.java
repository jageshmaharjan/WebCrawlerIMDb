package com.thu;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jagesh on 05/24/2016.
 */
public class MyCrawler_1 extends Thread
{
    public static void main(String[] args) throws IOException
    {
        try
        {
            int pageno = 15212; // (15211 == 10745)

            while (pageno < 341228 )  //341228
            {
                String mylink = "http://www.imdb.com/search/title?start="+pageno+"&title_type=feature";
                //"http://www.imdb.com/search/title?genres=action&num_votes=25000,&pf_rd_i=top&pf_rd_m=A2FGELUUNOQJNL&pf_rd_p=2406822102&pf_rd_r=1B1TRCWF2WPB06FTXJ3H&pf_rd_s=right-6&pf_rd_t=15506&ref_=chttp_gnr_1&sort=user_rating,desc&start="+pageno +"&title_type=feature";
                Connection connection = Jsoup.connect(mylink);
                connection.userAgent("Mozilla/5.0");
                Document doc = connection.get();

                //Elements elements = doc.body().select("tr.even detailed");
                Element table = doc.select("table").get(0);
                Elements rows = table.select("tr");

                for(int i =1; i<rows.size(); i++)
                {
                    String title = rows.select("tr").get(i).getElementsByClass("title").select("a").get(0).text();
                    String year = rows.select("tr").get(i).getElementsByClass("year_type").text();

                    String rating = rows.select("tr").get(i).getElementsByClass("rating-rating").text();
                    String storyline = rows.select("tr").get(i).getElementsByClass("outline").text();

                    String genre = rows.select("tr").get(i).getElementsByClass("genre").text();
                    String imagelink = rows.select("tr").get(i).getElementsByClass("Image").select("img").attr("src");
                    String movielink = "http://www.imdb.com" + rows.select("tr").get(i).getElementsByClass("title").select("a").get(0).attr("href").toString();

                    try
                    {
                        Integer.parseInt(year.substring(1,5));
                        if (Integer.parseInt(year.substring(1,5)) < 2016)
                        {
                            movieByGenre(movielink, title, year, rating, storyline, genre,imagelink);
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        System.out.println(e);
                    }
                }
                pageno += 50;
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }

    private static void movieByGenre(String inLinks, String title, String year, String rating, String story, String genre,String imagelink)
    {
        System.setProperty("https.proxyHost","127.0.0.1");
        System.setProperty("https.proxyPort","8200");
        Connection con = Jsoup.connect(inLinks).userAgent("Mozilla");
        con.timeout(5000);
        try
        {
            Document docin1 = Jsoup.connect(inLinks).get();

            String[] rate = rating.split("/");

            String[] gene = genre.split(" \\|");

            //String dirUrl = docin1.getElementsByClass("credit_summary_item").get(0).select("a").attr("href");
            String[] directors = docin1.getElementsByClass("credit_summary_item").get(0).text().split(":");
            String[] director = directors[1].split(",");

            String[] stars = null;
            String[] star = null;

            if (((docin1.getElementsByClass("credit_summary_items")).size()) == 0 )
            {
                if (docin1.getElementsByClass("credit_summary_item").size() == 3)
                {
                    stars = docin1.getElementsByClass("credit_summary_item").get(2).text().split("Stars:|\\|");

                    if (stars.length > 1)
                    {
                        star = stars[1].split(",");
                    }
                    else
                    {
                        star = stars[0].split(",");
                    }

                }
                else if (docin1.getElementsByClass("credit_summary_item").size() == 2)
                {
                    stars = docin1.getElementsByClass("credit_summary_item").get(1).text().split("Stars:|\\|");

                    if (stars.length > 1)
                    {
                        star = stars[1].split(",");
                    }
                    else
                    {
                        star = stars[0].split(",");
                    }
                }
            }

            String[] languages = docin1.getElementsByClass("txt-block").get(5).text().split(":");
            String[] lang = null;
            if(languages.length > 1)
            {
                lang = languages[1].split(" \\|");
            }

            String reviewLinks = inLinks + "reviews?ref_=tt_urv";
            Connection cons = Jsoup.connect(reviewLinks);
            cons.userAgent("Mozilla/5.0");
            Document doc = cons.get();

            Elements reviewContent = doc.getElementById("tn15content").select("p");
            List<String> reviews = new ArrayList<>();
            for (Element review : reviewContent)
            {
                reviews.add(review.text());
            }

            try
            {
                IMDbDatabaseSchema.databaseConnection(inLinks,title,year.substring(1,5),rate[0],story,gene,director,star,lang,reviews,imagelink);
                IMDbPostintTbl.normalization(inLinks,title,year.substring(1,5),rate[0],story,gene,director,star,lang,reviews);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
