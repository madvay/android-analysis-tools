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
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public abstract class TraceTransformableTable<T extends TraceTransformableRow> extends Table<T> {

    public TraceTransformableTable(List<T> rows) {
        super(rows);
    }

    public void transformTraces(final TraceTransformers.TT tt) {
        setRows(Lists.transform(getRows(), new Function<T, T>() {
            @Override
            public T apply(T input) {
                return newRowWithTrace(input, tt.apply(input.getTransformableTrace()));
            }
        }));
    }

    public void splitTraces() {
        setRows(Lists.newArrayList(
                Iterables.concat(Iterables.transform(getRows(), new Function<T, Iterable<T>>() {
                    @Override
                    public Iterable<T> apply(T input) {
                        List<T> ret = new ArrayList<T>();
                        for (StackTraceElement ste : input.getTransformableTrace()) {
                            ret.add(newRowWithTrace(input, Lists.newArrayList(ste)));
                        }
                        return ret;
                    }
                }))));
    }

    protected abstract T newRowWithTrace(T input, List<StackTraceElement> trace);
}
