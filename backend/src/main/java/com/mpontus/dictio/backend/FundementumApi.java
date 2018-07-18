/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mpontus.dictio.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.gson.Gson;
import com.mpontus.dictio.backend.model.Prompt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

@Api(name = "fundamentum",
        version = "v1",
        namespace =
        @ApiNamespace(
                ownerDomain = "dictio.mpontus.com",
                ownerName = "dictio.mpontus.com",
                packagePath = ""
        ))
public class FundementumApi {
    @ApiMethod(name = "getPrompts")
    public List<Prompt> getPrompts() {
        try {
            Gson gson = new Gson();
            FileReader fileReader = new FileReader(new File("WEB-INF/prompts.json"));
            Prompt[] prompts = gson.fromJson(fileReader, Prompt[].class);

            return Arrays.asList(prompts);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}

