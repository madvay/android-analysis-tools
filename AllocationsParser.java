/*
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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class AllocationsParser {
  public static ByteBuffer mapFile(File f, long offset, ByteOrder byteOrder) throws IOException {
    FileInputStream dataFile = new FileInputStream(f);
    try {
      FileChannel fc = dataFile.getChannel();
      MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, offset, f.length() - offset);
      buffer.order(byteOrder);
      return buffer;
    } finally {
      dataFile.close(); // this *also* closes the associated channel, fc
    }
  }

  public static String getString(ByteBuffer buf, int len) {
    char[] data = new char[len];
    for (int i = 0; i < len; i++)
      data[i] = buf.getChar();
    return new String(data);
  }

  /**
   * Converts a VM class descriptor string ("Landroid/os/Debug;") to
   * a dot-notation class name ("android.os.Debug").
   */
  private static String descriptorToDot(String str) {
    // count the number of arrays.
    int array = 0;
    while (str.startsWith("[")) {
      str = str.substring(1);
      array++;
    }

    int len = str.length();

        /* strip off leading 'L' and trailing ';' if appropriate */
    if (len >= 2 && str.charAt(0) == 'L' && str.charAt(len - 1) == ';') {
      str = str.substring(1, len - 1);
      str = str.replace('/', '.');
    }
    else {
      // convert the basic types
      if ("C".equals(str)) {
        str = "char";
      }
      else if ("B".equals(str)) {
        str = "byte";
      }
      else if ("Z".equals(str)) {
        str = "boolean";
      }
      else if ("S".equals(str)) {
        str = "short";
      }
      else if ("I".equals(str)) {
        str = "int";
      }
      else if ("J".equals(str)) {
        str = "long";
      }
      else if ("F".equals(str)) {
        str = "float";
      }
      else if ("D".equals(str)) {
        str = "double";
      }
    }

    // now add the array part
    for (int a = 0; a < array; a++) {
      str += "[]";
    }

    return str;
  }

  /**
   * Reads a string table out of "data".
   * <p/>
   * This is just a serial collection of strings, each of which is a
   * four-byte length followed by UTF-16 data.
   */
  private static void readStringTable(ByteBuffer data, String[] strings) {
    int count = strings.length;
    int i;

    for (i = 0; i < count; i++) {
      int nameLen = data.getInt();
      String descriptor = getString(data, nameLen);
      strings[i] = descriptorToDot(descriptor);
    }
  }

  /*
   * Message format:
   *   Message header (all values big-endian):
   *     (1b) message header len (to allow future expansion); includes itself
   *     (1b) entry header len
   *     (1b) stack frame len
   *     (2b) number of entries
   *     (4b) offset to string table from start of message
   *     (2b) number of class name strings
   *     (2b) number of method name strings
   *     (2b) number of source file name strings
   *   For each entry:
   *     (4b) total allocation size
   *     (2b) threadId
   *     (2b) allocated object's class name index
   *     (1b) stack depth
   *     For each stack frame:
   *       (2b) method's class name
   *       (2b) method name
   *       (2b) method source file
   *       (2b) line number, clipped to 32767; -2 if native; -1 if no source
   *   (xb) class name strings
   *   (xb) method name strings
   *   (xb) source file strings
   *
   *   As with other DDM traffic, strings are sent as a 4-byte length
   *   followed by UTF-16 data.
  */
  
  public static void parse( ByteBuffer data) {
    int messageHdrLen, entryHdrLen, stackFrameLen;
    int numEntries, offsetToStrings;
    int numClassNames, numMethodNames, numFileNames;

    /*
     * Read the header.
     */
    messageHdrLen = (data.get() & 0xff);
    entryHdrLen = (data.get() & 0xff);
    stackFrameLen = (data.get() & 0xff);
    numEntries = (data.getShort() & 0xffff);
    offsetToStrings = data.getInt();
    numClassNames = (data.getShort() & 0xffff);
    numMethodNames = (data.getShort() & 0xffff);
    numFileNames = (data.getShort() & 0xffff);


    /*
     * Skip forward to the strings and read them.
     */
    data.position(offsetToStrings);

    String[] classNames = new String[numClassNames];
    String[] methodNames = new String[numMethodNames];
    String[] fileNames = new String[numFileNames];

    readStringTable(data, classNames);
    readStringTable(data, methodNames);
    readStringTable(data, fileNames);

    /*
     * Skip back to a point just past the header and start reading
     * entries.
     */
    data.position(messageHdrLen);

    for (int i = 0; i < numEntries; i++) {
      int totalSize;
      int threadId, classNameIndex, stackDepth;

      totalSize = data.getInt();
      threadId = (data.getShort() & 0xffff);
      classNameIndex = (data.getShort() & 0xffff);
      stackDepth = (data.get() & 0xff);
      /* we've consumed 9 bytes; gobble up any extra */
      for (int skip = 9; skip < entryHdrLen; skip++) {
        data.get();
      }

      String[] steArray = new String[stackDepth];

      /*
       * Pull out the stack trace.
       */
      for (int sti = 0; sti < stackDepth; sti++) {
        int methodClassNameIndex, methodNameIndex;
        int methodSourceFileIndex;
        short lineNumber;
        String methodClassName, methodName, methodSourceFile;

        methodClassNameIndex = (data.getShort() & 0xffff);
        methodNameIndex = (data.getShort() & 0xffff);
        methodSourceFileIndex = (data.getShort() & 0xffff);
        lineNumber = data.getShort();

        methodClassName = classNames[methodClassNameIndex];
        methodName = methodNames[methodNameIndex];
        methodSourceFile = fileNames[methodSourceFileIndex];

        steArray[sti] = formatStackTrace(methodClassName, methodName, methodSourceFile, lineNumber);

        /* we've consumed 8 bytes; gobble up any extra */
        for (int skip = 8; skip < stackFrameLen; skip++) {
          data.get();
        }
      }

      System.out.printf("Alloc#: %1$d, Allocated Class: %2$s, Size: %3$d, Thread: %4$d\n", numEntries - i, classNames[classNameIndex],
                        totalSize, (short)threadId);
      for (String stackElement : steArray) {
        System.out.printf("       %1$s\n", stackElement);
      }
    }
  }

  private static String formatStackTrace(String methodClassName, String methodName, String methodSourceFile, short lineNumber) {
    return methodClassName + "." + methodName +
           (methodSourceFile != null && lineNumber >= 0 ?
             "(" + methodSourceFile + ":" + lineNumber + ")" :
             (methodSourceFile != null ?  "("+methodSourceFile+")" : "(Unknown Source)"));

  }

  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("Provide path to .alloc file");
      return;
    }

    try {
      ByteBuffer buf = mapFile(new File(args[0]), 0, ByteOrder.BIG_ENDIAN);
      parse(buf);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }
}
