package com.mpontus.dictio.data.model;

import java.util.List;

public class ResourceFile {
    private int version;

    private List<Prompt> prompts;

    public ResourceFile(int version, List<Prompt> prompts) {
        this.version = version;
        this.prompts = prompts;
    }

    public int getVersion() {
        return version;
    }

    public List<Prompt> getPrompts() {
        return prompts;
    }
}
