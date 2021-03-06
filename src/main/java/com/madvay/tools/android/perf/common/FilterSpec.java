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

/**
 *
 */
public class FilterSpec {
    public FilterSpec(String columnName, FilterType filterType, String rhs) {
        this.columnName = columnName;
        this.filterType = filterType;
        this.rhs = rhs;
    }

    public enum FilterType {
        EQUALS,
        NOT_EQUALS,
        LESS,
        LEQ,
        GREATER,
        GEQ,
        RE_MATCH,
        NOT_RE_MATCH,
        CONTAINS,
        NOT_CONTAINS
    }

    public final String columnName;
    public final FilterType filterType;
    public final String rhs;


}
