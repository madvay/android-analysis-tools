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
import com.google.common.collect.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public enum AggregationType {
        /** The number of rows */
        COUNT,
        /** The number of rows with unique values for this column */
        UNIQUE,
        /** The sum/concatenation of values in this column. */
        SUM
    }

    public Table<AggregateRow> groupAndAggregate(final String groupByColumn,
                                                 final String weightColumn,
                                                 final AggregationType aggregationType) {
        final RowAdapter<T> adapter = getAdapter();
        final boolean groupNumeric = adapter.types.get(adapter.columns.indexOf(groupByColumn)) ==
                                     RowAdapter.CoerceType.NUMERIC;
        boolean weightNumeric = adapter.types.get(adapter.columns.indexOf(weightColumn)) ==
                                RowAdapter.CoerceType.NUMERIC;
        ImmutableListMultimap<Object, T> indexed = Multimaps.index(rows, new Function<T, Object>() {
            @Override
            public Object apply(T input) {
                return adapter.get(input, groupByColumn).toString();
            }
        });
        Map<Object, Long> map = Maps.transformEntries(Multimaps.asMap(indexed),
                new Maps.EntryTransformer<Object, List<T>, Long>() {
                    @Override
                    public Long transformEntry(Object key, List<T> value) {
                        switch (aggregationType) {
                            case COUNT:
                                return (long) value.size();
                            case SUM: {
                                long l = 0;
                                for (T t : value) {
                                    l += Long.parseLong(adapter.get(t, weightColumn).toString());
                                }
                                return l;
                            }
                            case UNIQUE: {
                                Set<String> set = new HashSet<>();
                                for (T t : value) {
                                    set.add(adapter.get(t, weightColumn).toString());
                                }
                                return (long) set.size();
                            }
                        }
                        throw new IllegalArgumentException();
                    }
                });
        List<AggregateRow> newRows = Lists.newArrayList(Iterables
                .transform(map.entrySet(), new Function<Map.Entry<Object, Long>, AggregateRow>() {
                    @Override
                    public AggregateRow apply(Map.Entry<Object, Long> input) {
                        return new AggregateRow(input.getValue(), input.getKey());
                    }
                }));
        Table<AggregateRow> ret = new Table<AggregateRow>(newRows) {
            final RowAdapter<AggregateRow> adap = new AggregateRow.Adapter(groupNumeric);

            @Override
            public RowAdapter<AggregateRow> getAdapter() {
                return adap;
            }
        };
        return ret;
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
                    return doCompare(lhsStr, c, filterSpec.rhs);
                }
                case TEXT: {
                    return doCompare(lhsStr, lhsStr.compareTo(filterSpec.rhs), filterSpec.rhs);
                }
                default:
                    throw new IllegalArgumentException("Bad coerceType: " + adapter.types.get(i));
            }
        }

        private boolean doCompare(String lhsStr, long c, String rhsStr) {
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
                case CONTAINS:
                    return lhsStr.contains(rhsStr);
                case NOT_CONTAINS:
                    return !lhsStr.contains(rhsStr);
                default:
                    throw new IllegalArgumentException("Bad filterType: " + filterSpec.filterType);
            }
        }
    }
}
