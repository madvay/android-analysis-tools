/*
 * Copyright (c) 2015 by Advay Mengle.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.madvay.tools.android.perf.apat;

import com.google.common.base.Splitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class CommandLine {

    public final String command;

    public final List<String> args;

    public final Map<String, String> flags;

    private static final Splitter KV_SPLIT = Splitter.on('=').limit(2);

    public CommandLine(String[] argv) {
        if (argv.length < 1) {
            throw new IllegalArgumentException("1st argument must be name of a command.");
        }
        command = argv[0];

        args = new ArrayList<>();
        flags = new HashMap<>();
        for (int i = 1; i < argv.length; i++) {
            if (argv[i].startsWith("--")) {
                List<String> kv = KV_SPLIT.splitToList(argv[i].substring(2));
                flags.put(kv.get(0), kv.get(1));
            } else {
                args.add(argv[i]);
            }
        }
    }

}
