# apat: android-analysis-tools
Tools to analyze performance of Android applications.

## Running from a distribution
Run the `bin/apat` script per the Usage section.

## Usage
<!-- The content between the ```hashes``` will also be displayed
     as the usage notice by the Java binary. -->
```
android-analysis-tools - https://madvay.com/source/apat
Tools to analyze performance of Android applications.

Usage:
apat <command> <options>

Available commands:
 help               - Prints this usage message.
 version            - Prints version and copyright notice.
 license            - Prints the full LICENSE file.
 allocs             - Allocation tracking analysis:
    parse <file>        - Analyze a DDMS .alloc file
      --sort=<spec>         - Sorts the rows, where spec is a comma-separated
                              list of columns.  A column prefixed with a hyphen
                              is sorted in descending order (otherwise ascending order).
                              Ex: --sort=thread,-size (asc. by thread, then desc. by size)
```

## Building from source
1.  Clone the repository: `git clone https://github.com/madvay/android-analysis-tools.git`
2.  Build and run from the local repository: `./apat [args]`

## License
See the [LICENSE](LICENSE) and the [NOTICE](NOTICE) (per Section 4 d of the License) files.
