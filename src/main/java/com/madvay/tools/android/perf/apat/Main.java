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

package com.madvay.tools.android.perf.apat;

import com.madvay.tools.android.perf.allocs.AllocationsParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 *
 */
public class Main {

    private static void out(String s) {
        System.out.println(s);
    }

    private static void err(String s) {
        System.out.println("Error: " + s + "\n");
        System.out.flush();
    }

    private static void err(Exception e) {
        err(e.getMessage());
        e.printStackTrace();
    }

    private static void printUsage() {
        try {
            InputStream is = Main.class.getResourceAsStream("/README");
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(is, Charset.forName("UTF-8")));
            String s = br.readLine();
            while (s != null) {
                out(s);
                s = br.readLine();
            }
            br.close();
        } catch (IOException err) {
            err(err);
        }
    }

    private static String getVersion() {
        return
                Package.getPackage("com.madvay.tools.android.perf.apat").getSpecificationVersion();
    }

    private static void printVersion() {
        out("Version " + getVersion());
        out("---------------------------------------------------------------");
        try {
            InputStream is = Main.class.getResourceAsStream("/NOTICE");
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(is, Charset.forName("UTF-8")));
            String s = br.readLine();
            while (s != null) {
                out(s);
                s = br.readLine();
            }
            br.close();
        } catch (IOException err) {
            err(err);
        }
    }

    public static void main(String[] argv) {
        CommandLine cmd = null;
        try {
            cmd = new CommandLine(argv);
        } catch (IllegalArgumentException e) {
            err(e.getMessage());
            printUsage();
            System.exit(-1);
            return;
        }

        try {
            switch (cmd.command) {
                case "help":
                    printUsage();
                    break;
                case "version":
                    printVersion();
                    break;
                case "allocs":
                    runAllocs(cmd);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown command: " + cmd.command);
            }
        } catch (Exception e) {
            err(e.getMessage());
            printUsage();
            System.exit(-1);
        }
        return;
    }

    private static void runAllocs(CommandLine cmd) {
        switch (cmd.flags.get(0)) {
            case "parse":
                AllocationsParser.process(cmd.flags.get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown allocs subcommand: " + cmd.flags.get(0));
        }
    }
}
