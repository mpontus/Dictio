package com.mpontus.dictio.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mpontus.dictio.data.model.Phrase;

import java.io.IOException;

public class PhraseAdapter extends TypeAdapter<Phrase> {
    @Override
    public void write(JsonWriter out, Phrase value) throws IOException {
        out.value(value.getText());
    }

    @Override
    public Phrase read(JsonReader in) throws IOException {
        return new Phrase(in.nextString());
    }
}
