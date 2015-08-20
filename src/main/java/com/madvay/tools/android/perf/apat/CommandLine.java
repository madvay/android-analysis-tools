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
import com.madvay.tools.android.perf.common.StePredicates;
import com.madvay.tools.android.perf.common.TraceTransformers;

import com.google.common.base.*;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
    private static final Splitter SPEC_SPLIT = Splitter.on(':').limit(2);
    private static final Splitter CONJ_SPLIT = Splitter.on(',');
    private static final Splitter LIST_SPLIT = Splitter.on(',');

    public CommandLine(String[] argv) {
        if (argv.length < 1) {
            throw new IllegalArgumentException("1st argument must be name of a command.");
        }
        command = argv[0];

        args = new ArrayList<>();
        flags = LinkedListMultimap.create();
        List<String> allArgs = Lists.newArrayList(argv);
        allArgs.remove(0);
        parseArgs(allArgs);
    }

    private void parseArgs(Iterable<String> allArgs) {
        for (String arg : allArgs) {
            parseArg(arg);
        }
    }

    private void parseArg(String arg) {
        if (arg.startsWith("--")) {
            List<String> kv = KV_SPLIT.splitToList(arg.substring(2));
            if (kv.get(0).equals("config")) {
                handleConfigFile(kv.get(1));
            } else {
                flags.put(kv.get(0), kv.get(1));
            }
        } else {
            args.add(arg);
        }
    }

    private void handleConfigFile(String fname) {
        List<String> internalArgs;
        try {
            internalArgs = Files.readAllLines(Paths.get(fname), Charsets.UTF_8);
        } catch (IOException err) {
            throw new IllegalArgumentException("Cannot load config file: " + fname, err);
        }
        for (String arg : internalArgs) {
            arg = arg.trim();
            // Skip comments
            if (arg.startsWith("#") || arg.isEmpty()) {
                continue;
            }
            parseArg(arg);
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

    public List<String> getMultiFlagWithInternalLists(String name) {
        return Lists.newArrayList(Iterables
                .concat(Iterables.transform(flags.get(name), new Function<String, List<String>>() {
                    @Override
                    public List<String> apply(String input) {
                        return LIST_SPLIT.splitToList(input);
                    }
                })));
    }

    public List<FilterSpec> getFilterSpecsFlag(final String column) {
        List<String> flagStr = getMultiFlag(column);
        return Lists.transform(flagStr, new Function<String, FilterSpec>() {
            @Override
            public FilterSpec apply(String input) {
                if (input == null || input.isEmpty()) {
                    return null;
                }
                List<String> spl = SPEC_SPLIT.splitToList(input);
                if (spl.size() == 2) {
                    FilterSpec.FilterType t = filterTypeByPrefix(spl.get(0));
                    return new FilterSpec(column, t, spl.get(1));
                } else if (spl.size() == 1) {
                    // Assume equals.
                    return new FilterSpec(column, FilterSpec.FilterType.EQUALS, spl.get(0));
                } else {
                    throw new IllegalArgumentException("Bad filter spec: " + input);
                }
            }
        });
    }

    public List<TraceTransformers.TT> getTraceTransformsFlag(final String name) {
        List<String> flagStr = getMultiFlag(name);
        return Lists.transform(flagStr, new Function<String, TraceTransformers.TT>() {
            @Override
            public TraceTransformers.TT apply(String input) {
                if (input == null || input.isEmpty()) {
                    return null;
                }
                return parseTraceTransform(input);
            }
        });
    }

    private static Predicate<StackTraceElement> parseSteP(String s) {
        return Predicates.and(Lists.transform(CONJ_SPLIT.splitToList(s),
                new Function<String, Predicate<StackTraceElement>>() {
                    @Override
                    public Predicate<StackTraceElement> apply(String input) {
                        return parseStePOperator(input);
                    }
                }));
    }

    private static Predicate<StackTraceElement> parseStePOperator(String s) {
        List<String> spl = SPEC_SPLIT.splitToList(s);
        String operator = spl.get(0);
        if (spl.size() == 1) {
            return StePredicates.contains(operator);
        }
        String arg = spl.get(1);
        switch (operator) {
            case "underPackage":
                return StePredicates.underPackage(arg);
            case "inPackage":
                return StePredicates.inPackage(arg);
            case "class":
                return StePredicates.classContains(arg);
            case "classEq":
                return StePredicates.classEq(arg);
            case "classRe":
                return StePredicates.classRe(arg);
            case "method":
                return StePredicates.methodContains(arg);
            case "methodEq":
                return StePredicates.methodEq(arg);
            case "methodRe":
                return StePredicates.methodRe(arg);
            case "site":
                return StePredicates.siteContains(arg);
            case "siteEq":
                return StePredicates.siteEq(arg);
            case "siteRe":
                return StePredicates.siteRe(arg);
            case "contains":
                return StePredicates.contains(arg);
            default:
                throw new IllegalArgumentException("Unknown element spec: " + s);
        }
    }

    private static TraceTransformers.TT parseTraceTransform(String s) {
        List<String> spl = SPEC_SPLIT.splitToList(s);
        switch (spl.get(0)) {
            case "pruneRecursion":
                return TraceTransformers.pruneRecursion();
            case "prune":
                return TraceTransformers.prune(parseSteP(spl.get(1)));
            case "keep":
                return TraceTransformers.keep(parseSteP(spl.get(1)));
            case "pruneAbove":
                return TraceTransformers.pruneAbove(parseSteP(spl.get(1)));
            case "pruneBelow":
                return TraceTransformers.pruneBelow(parseSteP(spl.get(1)));
            case "keepAbove":
                return TraceTransformers.keepAbove(parseSteP(spl.get(1)));
            case "keepBelow":
                return TraceTransformers.keepBelow(parseSteP(spl.get(1)));
            default:
                throw new IllegalArgumentException("Unknown trace transform: " + s);
        }
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
            case "contains":
            case "ss":
                return FilterSpec.FilterType.CONTAINS;
            case "notcontains":
            case "nss":
                return FilterSpec.FilterType.NOT_CONTAINS;
            default:
                throw new IllegalArgumentException("Bad filter type: " + pr);
        }
    }
}
