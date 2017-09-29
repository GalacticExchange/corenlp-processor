package com.dataflowdeveloper.processors.process;

/**
 * Created by iliya on 28.04.17.
 */
public class Word{
    String word;
    String pos;
    String ne;
    String role;

    public Word(String word, String pos, String ne, String role) {
        this.word = word;
        this.pos = pos;
        this.ne = ne;
        this.role = role;
    }
}
