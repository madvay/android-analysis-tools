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

package com.madvay.tools.android.perf.allocs;

import com.madvay.tools.android.perf.common.Row;
import com.madvay.tools.android.perf.common.RowAdapter;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 *
 */
public class AllocRow extends Row {
    public final int id;
    public final String allocatedClass;
    public final int bytes;
    public final int thread;
    public final List<StackTraceElement> stackTrace;

    public AllocRow(int id, String allocatedClass, int bytes, int thread,
                    StackTraceElement[] stackTrace) {
        this.id = id;
        this.allocatedClass = allocatedClass;
        this.bytes = bytes;
        this.thread = thread;
        this.stackTrace = ImmutableList.copyOf(stackTrace);
    }

    static final class Adapter extends RowAdapter<AllocRow> {

        Adapter() {
            super("id", "allocatedClass", "size", "thread", "stackTrace");
        }

        @Override
        public Object get(AllocRow row, String column) {
            switch (column) {
                case "id":
                    return row.id;
                case "allocatedClass":
                    return row.allocatedClass;
                case "size":
                    return row.bytes;
                case "thread":
                    return row.thread;
                case "stackTrace":
                    return row.stackTrace;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }
}
