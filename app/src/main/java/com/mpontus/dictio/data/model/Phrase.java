package com.mpontus.dictio.data.model;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Phrase {

    private static Pattern WORD_PATTERN = Pattern.compile("(?i)[\\w'-]+");

    private final String text;
    private final ArrayList<String> words = new ArrayList<>();
    private final ArrayList<Integer> wordStarts = new ArrayList<>();
    private final ArrayList<Integer> wordEnds = new ArrayList<>();

    public Phrase(String text) {
        this.text = text;

        Matcher matcher = WORD_PATTERN.matcher(text);

        while (matcher.find()) {
            words.add(matcher.group());
            wordStarts.add(matcher.start());
            wordEnds.add(matcher.end());
        }
    }

    public String getText() {
        return text;
    }

    public int getWordCount() {
        return words.size();
    }

    public String getWord(int wordIndex) {
        return words.get(wordIndex);
    }

    public int getWordStart(int wordIndex) {
        return wordStarts.get(wordIndex);
    }

    public int getWordEnd(int wordIndex) {
        return wordEnds.get(wordIndex);
    }

    public Match emptyMatch() {
        return Match.empty();
    }

    public Match match(String against) {
        return Match.create(this, new Phrase(against));
    }
}
