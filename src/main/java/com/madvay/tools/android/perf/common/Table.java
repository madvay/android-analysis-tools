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

import com.google.common.collect.Ordering;

import java.util.List;

/**
 *
 */
public abstract class Table<T extends Row> {
    private List<T> rows;

    public Table(List<T> rows) {
        this.rows = rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    public List<T> getRows() {
        return rows;
    }

    public abstract RowAdapter<T> getAdapter();


    public void sortOn(List<String> cols) {
        Ordering<T> order = null;
        for (String colSpec : cols) {
            String col = colSpec.startsWith("-") ? colSpec.substring(1) : colSpec;
            Ordering<T> cur = new ColumnOrdering(col);
            if (colSpec.startsWith("-")) {
                cur = cur.reverse();
            }
            if (order == null) {
                order = cur;
            } else {
                order = order.compound(cur);
            }
        }
        rows = order.sortedCopy(rows);
    }

    private final class ColumnOrdering extends Ordering<T> {

        private final String col;

        public ColumnOrdering(String col) {
            this.col = col;
        }

        @Override
        public int compare(T left, T right) {
            Comparable l = (Comparable) getAdapter().get(left, col);
            Comparable r = (Comparable) getAdapter().get(right, col);
            return Ordering.natural().compare(l, r);
        }
    }
}
