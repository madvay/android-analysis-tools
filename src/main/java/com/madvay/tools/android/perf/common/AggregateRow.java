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

import com.google.common.collect.ImmutableList;

/**
 *
 */
public class AggregateRow extends Row {
    public final long value;
    public final Object group;

    public AggregateRow(Long agg, Object group) {
        this.value = agg;
        this.group = group;
    }

    static final class Adapter extends RowAdapter<AggregateRow> {

        Adapter(boolean groupNumeric) {
            super(ImmutableList.of("weight", "group"), ImmutableList
                    .of(CoerceType.NUMERIC, groupNumeric ? CoerceType.NUMERIC : CoerceType.TEXT));
        }

        @Override
        public Object get(AggregateRow row, String column) {
            switch (column) {
                case "weight":
                    return row.value;
                case "group":
                    return row.group;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }
}
