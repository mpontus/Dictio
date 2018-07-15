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
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;

/**
 * MyApi skeleton endpoints sample
 * Add your first API methods in this class, or you may create another class.
 * In that case, update the src/main/webapp/WEB-INF/web.xml and modify
 * the class set to the services param as a comma separated list.
 * <p>
 * For example:
 * <init-param>
 * <param-name>services</param-name>
 * <param-value>com.example.skeleton.FirstApi, com.example.skeleton.SecondApi</param-value>
 * </init-param>
 */
@Api(name = "echo",
        version = "v1",
        namespace =
        @ApiNamespace(
                ownerDomain = "echo.example.com",
                ownerName = "echo.example.com",
                packagePath = ""
        ))
public class MyApi {
    @ApiMethod(name = "echo")
    public Message echo(Message message, @Named("n") @Nullable Integer n) {
        return doEcho(message, n);
    }

    private Message doEcho(Message message, Integer n) {
        if (n != null && n >= 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) {
                if (i > 0) {
                    sb.append(" ");
                }
                sb.append(message.getMessage());
            }
            message.setMessage(sb.toString());
        }
        return message;
    }
}

