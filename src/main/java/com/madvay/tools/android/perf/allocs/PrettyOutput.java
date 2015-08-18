/*
 * Copyright (c) 2015 by Advay Mengle.
 *
 * Portions derived from AllocationsParser.java:
 * Copyright (C) 2015 The Android Open Source Project
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

import com.google.common.base.Function;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 */
public class PrettyOutput implements Function<AllocRow, String> {

    @Override
    public String apply(AllocRow input) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.printf("Alloc#: %1$d, Allocated Class: %2$s, Size: %3$d, Thread: %4$d\n", input.id,
                input.allocatedClass, input.bytes, input.thread);
        for (StackTraceElement stackElement : input.stackTrace) {
            pw.printf("       %1$s\n", stackElement.toString());
        }
        return sw.toString();
    }
}
