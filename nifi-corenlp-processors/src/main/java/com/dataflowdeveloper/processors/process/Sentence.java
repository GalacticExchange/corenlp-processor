package com.dataflowdeveloper.processors.process;

import java.util.ArrayList;

/**
 * Created by iliya on 28.04.17.
 */
public class Sentence{
    ArrayList<Word> words;
    ArrayList<Double> sentimentProbs;
    ArrayList<String> mentions;


    public Sentence(){
        mentions = new ArrayList<String>();
        words = new ArrayList<Word>();
        sentimentProbs = new ArrayList<Double>();
    }

    public void addWord(Word word){
        this.words.add(word);
    }

    public void addSentimentProbs(double prob){
        sentimentProbs.add(prob);
    }

    public void addMention(String mention){
        mentions.add(mention);
    }
}
