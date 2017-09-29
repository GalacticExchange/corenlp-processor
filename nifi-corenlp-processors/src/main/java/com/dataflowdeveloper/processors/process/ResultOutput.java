package com.dataflowdeveloper.processors.process;

import java.util.ArrayList;

/**
 * Created by iliya on 28.04.17.
 */
public class ResultOutput {
    ArrayList<Sentence> sentences;
    ArrayList<String> corefChains;

    public ResultOutput(){
        sentences = new ArrayList<Sentence>();
        corefChains = new ArrayList<String>();

    }

    public void addSentence(Sentence sentence){
        sentences.add(sentence);
    }

    public void addCorefChain(String corefChain){
        corefChains.add(corefChain);
    }
}
