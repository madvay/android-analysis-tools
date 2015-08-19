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

import com.madvay.tools.android.perf.common.RowAdapter;
import com.madvay.tools.android.perf.common.TraceTransformableTable;

import java.util.List;

/**
 *
 */
public class AllocTable extends TraceTransformableTable<AllocRow> {

    private static final RowAdapter<AllocRow> ADAPTER = new AllocRow.Adapter();

    public AllocTable(List<AllocRow> l) {
        super(l);
    }

    @Override
    public RowAdapter<AllocRow> getAdapter() {
        return ADAPTER;
    }

    @Override
    protected AllocRow newRowWithTrace(AllocRow input, List<StackTraceElement> trace) {
        return new AllocRow(input.id, input.allocatedClass, input.bytes, input.thread, trace);
    }
}
