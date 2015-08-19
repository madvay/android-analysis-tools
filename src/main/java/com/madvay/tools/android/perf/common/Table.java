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
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import java.util.List;
import java.util.regex.Pattern;

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

    public void matching(FilterSpec spec) {
        final FilterSpec filterSpec = spec;
        rows = Lists.newArrayList(Collections2.filter(rows, new RowFilter(filterSpec)));
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

    private final class RowFilter implements Predicate<T> {
        private final Pattern pat;
        private final FilterSpec filterSpec;

        public RowFilter(FilterSpec filterSpec) {
            this.filterSpec = filterSpec;
            pat = filterSpec.filterType == FilterSpec.FilterType.RE_MATCH ||
                  filterSpec.filterType == FilterSpec.FilterType.NOT_RE_MATCH ?
                  Pattern.compile(filterSpec.rhs) : null;
        }

        @Override
        public boolean apply(T input) {
            RowAdapter<T> adapter = getAdapter();
            Object lhsRaw = adapter.get(input, filterSpec.columnName);
            String lhsStr = lhsRaw.toString();
            int i = adapter.columns.indexOf(filterSpec.columnName);
            switch (adapter.types.get(i)) {
                case NUMERIC: {
                    long lhs = Long.parseLong(lhsStr);
                    long rhs = Long.parseLong(filterSpec.rhs);
                    long c = Long.compare(lhs, rhs);
                    return doCompare(lhsStr, c);
                }
                case TEXT: {
                    return doCompare(lhsStr, lhsStr.compareTo(filterSpec.rhs));
                }
                default:
                    throw new IllegalArgumentException("Bad coerceType: " + adapter.types.get(i));
            }
        }

        private boolean doCompare(String lhsStr, long c) {
            switch (filterSpec.filterType) {
                case GEQ:
                    return c >= 0;
                case GREATER:
                    return c > 0;
                case LEQ:
                    return c <= 0;
                case LESS:
                    return c < 0;
                case EQUALS:
                    return c == 0;
                case NOT_EQUALS:
                    return c != 0;
                case RE_MATCH:
                    return pat.matcher(lhsStr).matches();
                case NOT_RE_MATCH:
                    return !pat.matcher(lhsStr).matches();
                default:
                    throw new IllegalArgumentException("Bad filterType: " + filterSpec.filterType);
            }
        }
    }
}
