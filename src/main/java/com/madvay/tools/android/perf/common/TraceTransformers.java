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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TraceTransformers {
    // Convenience: shorter name.
    public interface TT extends Function<List<StackTraceElement>, List<StackTraceElement>> {

    }

    public static TT prune(final Predicate<StackTraceElement> spec) {
        return new TT() {
            @Override
            public List<StackTraceElement> apply(List<StackTraceElement> input) {
                return Lists.newArrayList(Collections2.filter(input, Predicates.not(spec)));
            }
        };
    }

    public static TT keep(final Predicate<StackTraceElement> spec) {
        return new TT() {
            @Override
            public List<StackTraceElement> apply(List<StackTraceElement> input) {
                return Lists.newArrayList(Collections2.filter(input, spec));
            }
        };
    }

    public static TT pruneAbove(final Predicate<StackTraceElement> spec) {
        return new TT() {
            @Override
            public List<StackTraceElement> apply(List<StackTraceElement> input) {
                int matchIdx = Iterables.indexOf(input, spec);
                if (matchIdx == -1) {
                    return Lists.newArrayList();
                }
                return Lists.newArrayList(Iterables.skip(input, matchIdx));
            }
        };
    }

    public static TT pruneBelow(final Predicate<StackTraceElement> spec) {
        return new TT() {
            @Override
            public List<StackTraceElement> apply(List<StackTraceElement> input) {
                int last = -1;
                for (int i = input.size() - 1; i >= 0; i--) {
                    if (spec.apply(input.get(i))) {
                        last = i;
                        break;
                    }
                }
                return Lists.newArrayList(Iterables.limit(input, last + 1));
            }
        };
    }

    public static TT keepAbove(final Predicate<StackTraceElement> spec) {
        return new TT() {
            @Override
            public List<StackTraceElement> apply(List<StackTraceElement> input) {
                int matchIdx = Iterables.indexOf(input, spec);
                if (matchIdx == -1) {
                    return input;
                }
                return Lists.newArrayList(Iterables.limit(input, matchIdx));
            }
        };
    }

    public static TT keepBelow(final Predicate<StackTraceElement> spec) {
        return new TT() {
            @Override
            public List<StackTraceElement> apply(List<StackTraceElement> input) {
                int last = -1;
                for (int i = input.size() - 1; i >= 0; i--) {
                    if (spec.apply(input.get(i))) {
                        last = i;
                        break;
                    }
                }
                return Lists.newArrayList(Iterables.skip(input, last + 1));
            }
        };
    }

    public static TT pruneRecursion() {
        return new TT() {
            @Override
            public List<StackTraceElement> apply(List<StackTraceElement> input) {
                String lastClass = "~", lastMethod = "~";
                List<StackTraceElement> ret = new ArrayList<>();
                for (int i = input.size() - 1; i >= 0; i--) {
                    StackTraceElement ste = input.get(i);
                    if (ste.getClassName().equals(lastClass) &&
                        ste.getMethodName().equals(lastMethod)) {
                        continue;
                    }
                    lastClass = ste.getClassName();
                    lastMethod = ste.getMethodName();
                    ret.add(0, ste);
                }
                return ret;
            }
        };
    }

}
