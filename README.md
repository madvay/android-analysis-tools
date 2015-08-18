# android-analysis-tools
Tools to analyze performance of Android applications.

## Building from source
1.  Clone the repository: `git clone https://github.com/madvay/android-analysis-tools.git`
2.  Build with gradle: `./gradlew clean build`
3.  The `./apat` script can now be run per the Usage section.

## Usage
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
    parse <file>        - Parses a DDMS .alloc file
```

## License
See the [LICENSE](LICENSE) and the [NOTICE](NOTICE) (per Section 4 d of the License) files.
