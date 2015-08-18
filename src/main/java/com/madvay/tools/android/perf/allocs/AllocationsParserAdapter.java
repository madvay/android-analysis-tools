/*
 * Copyright (c) 2015 by Advay Mengle.
 *
 * A modified version of AllocationsParser.java.  Original:
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
 */

package com.madvay.tools.android.perf.allocs;

import com.android.ddmlib.AllocationInfo;
import com.android.ddmlib.AllocationsParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class AllocationsParserAdapter {
    public static ByteBuffer mapFile(File f, long offset, ByteOrder byteOrder) throws IOException {
        FileInputStream dataFile = new FileInputStream(f);
        try {
            FileChannel fc = dataFile.getChannel();
            MappedByteBuffer buffer =
                    fc.map(FileChannel.MapMode.READ_ONLY, offset, f.length() - offset);
            buffer.order(byteOrder);
            return buffer;
        } finally {
            dataFile.close(); // this *also* closes the associated channel, fc
        }
    }

    public static List<AllocRow> parse(ByteBuffer data) {
        AllocationInfo[] orig = AllocationsParser.parse(data);
        List<AllocRow> ret = new ArrayList<>();
        for (AllocationInfo a : orig) {
            ret.add(new AllocRow(a.getAllocNumber(), a.getAllocatedClass(), a.getSize(),
                    a.getThreadId(), a.getStackTrace()));
        }
        return ret;
    }

    public static List<AllocRow> parse(String allocFilePath) {
        try {
            ByteBuffer buf = mapFile(new File(allocFilePath), 0, ByteOrder.BIG_ENDIAN);
            return parse(buf);
        } catch (IOException e) {
            throw new RuntimeException("Could not load " + allocFilePath, e);
        }
    }
}
