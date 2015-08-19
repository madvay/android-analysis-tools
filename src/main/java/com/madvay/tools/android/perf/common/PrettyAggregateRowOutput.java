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

/**
 *
 */
public class PrettyAggregateRowOutput implements Function<AggregateRow, String> {

    private final int weightCols, groupCols;

    public PrettyAggregateRowOutput(Table<AggregateRow> tab) {
        int w = 0, g = 0;
        for (AggregateRow row : tab.getRows()) {
            w = Math.max(w, String.valueOf(row.value).length());
            g = Math.max(g, row.group.toString().length());
        }
        weightCols = w + 2;
        groupCols = g + 2;
    }

    @Override
    public String apply(AggregateRow input) {
        StringBuilder sb = new StringBuilder();
        String gs = input.group.toString();
        sb.append(gs);
        for (int w = gs.length(); w < groupCols; w++) {
            sb.append(' ');
        }
        sb.append(" | ");

        String ws = Long.toString(input.value);
        for (int w = ws.length(); w < weightCols; w++) {
            sb.append(' ');
        }
        sb.append(ws);
        sb.append('\n');
        return sb.toString();
    }
}
