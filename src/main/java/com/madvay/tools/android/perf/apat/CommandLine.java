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

import com.madvay.tools.android.perf.common.FilterSpec;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class CommandLine {

    public final String command;

    public final List<String> args;

    public final Multimap<String, String> flags;

    private static final Splitter KV_SPLIT = Splitter.on('=').limit(2);
    private static final Splitter FILTER_SPLIT = Splitter.on(':').limit(2);

    public CommandLine(String[] argv) {
        if (argv.length < 1) {
            throw new IllegalArgumentException("1st argument must be name of a command.");
        }
        command = argv[0];

        args = new ArrayList<>();
        flags = LinkedListMultimap.create();
        for (int i = 1; i < argv.length; i++) {
            if (argv[i].startsWith("--")) {
                List<String> kv = KV_SPLIT.splitToList(argv[i].substring(2));
                flags.put(kv.get(0), kv.get(1));
            } else {
                args.add(argv[i]);
            }
        }
    }

    public String getUnaryFlagWithDefault(String name, String def) {
        return Iterables.getLast(flags.get(name), def);
    }

    public String getJoinedListFlagWithDefault(String name, String def) {
        List<String> multiFlag = getMultiFlag(name);
        if (multiFlag.isEmpty()) {
            return def;
        }
        return Joiner.on(',').join(multiFlag);
    }

    public List<String> getMultiFlag(String name) {
        return Lists.newArrayList(flags.get(name));
    }

    public List<FilterSpec> getFilterSpecsFlag(final String column) {
        List<String> flagStr = getMultiFlag(column);
        return Lists.transform(flagStr, new Function<String, FilterSpec>() {
            @Override
            public FilterSpec apply(String input) {
                if (input == null || input.isEmpty()) {
                    return null;
                }
                List<String> spl = FILTER_SPLIT.splitToList(input);
                FilterSpec.FilterType t = filterTypeByPrefix(spl.get(0));
                return new FilterSpec(column, t, spl.get(1));
            }
        });
    }

    private static FilterSpec.FilterType filterTypeByPrefix(String pr) {
        switch (pr) {
            case "eq":
                return FilterSpec.FilterType.EQUALS;
            case "ne":
            case "neq":
                return FilterSpec.FilterType.NOT_EQUALS;
            case "lt":
            case "l":
                return FilterSpec.FilterType.LESS;
            case "le":
            case "leq":
                return FilterSpec.FilterType.LEQ;
            case "gt":
            case "g":
                return FilterSpec.FilterType.GREATER;
            case "ge":
            case "geq":
                return FilterSpec.FilterType.GEQ;
            case "re":
                return FilterSpec.FilterType.RE_MATCH;
            case "nre":
                return FilterSpec.FilterType.NOT_RE_MATCH;
            default:
                throw new IllegalArgumentException("Bad filter type: " + pr);
        }
    }
}
