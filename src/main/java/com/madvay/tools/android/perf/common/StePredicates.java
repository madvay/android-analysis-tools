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

package com.madvay.tools.android.perf.common;

import com.google.common.base.Predicate;

import java.util.regex.Pattern;

/**
 * Predicates for StackTraceElements.
 */
public final class StePredicates {
    public static Predicate<StackTraceElement> contains(final String n) {
        return new Predicate<StackTraceElement>() {
            @Override
            public boolean apply(StackTraceElement input) {
                return input.toString().contains(n);
            }
        };
    }

    public static Predicate<StackTraceElement> underPackage(final String n) {
        return new Predicate<StackTraceElement>() {
            private final String nPrefix = n + ".";

            @Override
            public boolean apply(StackTraceElement input) {
                return input.getClassName().startsWith(nPrefix);
            }
        };
    }

    public static Predicate<StackTraceElement> inPackage(final String n) {
        return new Predicate<StackTraceElement>() {
            private final String nPrefix = n + ".";

            @Override
            public boolean apply(StackTraceElement input) {
                if (!input.getClassName().startsWith(nPrefix)) {
                    return false;
                }
                // Make sure there is exactly one identifier after the prefix.
                return !input.getClassName().substring(nPrefix.length()).contains(".");
            }
        };
    }

    public static Predicate<StackTraceElement> classEq(final String n) {
        return new Predicate<StackTraceElement>() {
            @Override
            public boolean apply(StackTraceElement input) {
                return input.getClassName().equals(n);
            }
        };
    }

    public static Predicate<StackTraceElement> methodEq(final String n) {
        return new Predicate<StackTraceElement>() {
            @Override
            public boolean apply(StackTraceElement input) {
                return input.getMethodName().equals(n);
            }
        };
    }

    public static Predicate<StackTraceElement> siteEq(final String n) {
        return new Predicate<StackTraceElement>() {
            @Override
            public boolean apply(StackTraceElement input) {
                return (input.getClassName() + "." + input.getMethodName()).equals(n);
            }
        };
    }

    public static Predicate<StackTraceElement> classContains(final String n) {
        return new Predicate<StackTraceElement>() {
            @Override
            public boolean apply(StackTraceElement input) {
                return input.getClassName().contains(n);
            }
        };
    }

    public static Predicate<StackTraceElement> methodContains(final String n) {
        return new Predicate<StackTraceElement>() {
            @Override
            public boolean apply(StackTraceElement input) {
                return input.getMethodName().contains(n);
            }
        };
    }

    public static Predicate<StackTraceElement> siteContains(final String n) {
        return new Predicate<StackTraceElement>() {
            @Override
            public boolean apply(StackTraceElement input) {
                return (input.getClassName() + "." + input.getMethodName()).contains(n);
            }
        };
    }

    public static Predicate<StackTraceElement> classRe(final String n) {
        return new Predicate<StackTraceElement>() {
            private final Pattern p = Pattern.compile(n);

            @Override
            public boolean apply(StackTraceElement input) {
                return p.matcher(input.getClassName()).matches();
            }
        };
    }

    public static Predicate<StackTraceElement> methodRe(final String n) {
        return new Predicate<StackTraceElement>() {
            private final Pattern p = Pattern.compile(n);

            @Override
            public boolean apply(StackTraceElement input) {
                return p.matcher(input.getMethodName()).matches();
            }
        };
    }

    public static Predicate<StackTraceElement> siteRe(final String n) {
        return new Predicate<StackTraceElement>() {
            private final Pattern p = Pattern.compile(n);

            @Override
            public boolean apply(StackTraceElement input) {
                return p.matcher(input.getClassName() + "." + input.getMethodName()).matches();
            }
        };
    }
}
