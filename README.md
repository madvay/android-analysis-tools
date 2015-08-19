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

USAGE
apat <command> <options>

AVAILABLE COMMANDS
 help               - Prints this usage message.
 version            - Prints version and copyright notice.
 license            - Prints the full LICENSE file.
 allocs             - Allocation tracking analysis:
    parse <file>        - Simple dump from a DDMS .alloc file
    top <file>          - Histogram [TODO]

COMMON FLAGS
Filters:
 --id=<filter>         - Allocation id (numeric)
 --allocated=<filter>  - Allocated class name
 --size=<filter>       - Allocation size in bytes (numeric)
 --thread=<filter>     - Allocating thread id (numeric)
 --stackTrace=<filter> - Joined allocation site stack trace

 Match attributes via a filter spec,
 <comparison>:<rhs> where comparison is one of:
     eq  - lhs == rhs
     ne  - lhs != rhs
     lt  - lhs <  rhs
     le  - lhs <= rhs
     gt  - lhs >  rhs
     ge  - lhs >= rhs
     re  - lhs matches the regular expression rhs
     nre - lhs does not march regular expression rhs
 Repeating a flag creates a conjunction filter.

 Ex: --thread=ne:14 --size=gt:16 --size=le:128
 matches allocations on thread 14, with a size greater
 than 16 bytes and less than or equal to 128 bytes.


Other flags:
 --sort=<spec>         - Sorts the rows, where spec is a comma-separated
                         list of columns.  A column prefixed with a hyphen
                         is sorted in descending order (otherwise
                         ascending order).
                         Ex: --sort=thread,-size (asc. by thread, then
                         desc. by size)

 --format=csv|pretty   - Selects the output format.
                         Default: pretty
```

## Building from source
1.  Clone the repository:
`git clone https://github.com/madvay/android-analysis-tools.git`
2.  Build and run from the local repository: `./apat [args]`

## License
See the [LICENSE](LICENSE) and the [NOTICE](NOTICE) (per Section 4 d of the
License) files.
