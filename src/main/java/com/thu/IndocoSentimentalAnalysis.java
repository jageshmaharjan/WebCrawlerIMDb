package com.thu;

import io.indico.Indico;
import io.indico.api.image.FacialEmotion;
import io.indico.api.results.BatchIndicoResult;
import io.indico.api.results.IndicoResult;
import io.indico.api.text.Language;
import io.indico.api.utils.IndicoException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jagesh on 05/08/2016.
 */
public class IndocoSentimentalAnalysis implements MyConstants
{
    public static void main(String[] args) throws IOException, IndicoException
    {
        IndocoSentimentalAnalysis senti = new IndocoSentimentalAnalysis();

        senti.simpleSentiment();

//        senti.languageDetect();

//        senti.imageFeatures();

//        senti.facialDetection();


    }

    public void simpleSentiment() throws IOException, IndicoException
    {
        Indico indico = new Indico(MY_API_KEY);

        IndicoResult single = indico.sentiment.predict("i like her");

        Double result = single.getSentiment();
        System.out.println(result);

        String[] example = {
                "The Door was BRUTAL in its final minutes, and you're still crying over it, admit it",
                "That was an incredible episode of Thrones packed with huge revelations for both book readers and show watchers"
        };

        BatchIndicoResult multiple = indico.sentiment.predict(example);

        List<Double> results = multiple.getSentiment();

        System.out.println(results);
    }

    public void languageDetect() throws IOException, IndicoException
    {
        Map params = new HashMap();
        params.put("threshold", 0.1);

        Indico indico = new Indico(MY_API_KEY );

        IndicoResult single = indico.language.predict("i love natural language processing?");

        Map<Language, Double> result = single.getLanguage();
        System.out.println(result);

        String[] example = {
                "how ar you?",
                "你好"
        };

        BatchIndicoResult multiple = indico.language.predict(example);
        List<Map<Language, Double> > results = multiple.getLanguage();

        System.out.println(results);
    }

    public void facialDetection() throws IOException, IndicoException
    {
        Indico indico = new Indico(MY_API_KEY);

        IndicoResult single = indico.fer.predict(IMG_PATH_1);

        Map<FacialEmotion, Double> result = single.getFer();
        System.out.println(result);

//        String[] example = {
//                IMG_PATH_2,  IMG_PATH_3
//        };
//
//        BatchIndicoResult multiple = indico.fer.predict(example);
//        List<Map<FacialEmotion, Double> > results = multiple.getFer();
//        System.out.println(results);

    }

    public void imageFeatures() throws IOException, IndicoException
    {
        Indico indico = new Indico(MY_API_KEY);
        IndicoResult single = indico.imageFeatures.predict(IMG_PATH_1);
        List<Double> result = single.getImageFeatures();
        System.out.println(result);

        String[] example = {
                IMG_PATH_2,
                IMG_PATH_3
        };

        BatchIndicoResult multiple = indico.imageFeatures.predict(example);
        List<List<Double>> results = multiple.getImageFeatures();
        System.out.println(results);

    }
}
