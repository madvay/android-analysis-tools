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

CONFIGURATION


CALL SITE TRANSFORMS:
 --traceTransform=<transformSpec>

 Adjusts every stack trace per the spec.
 Given a stack trace [A, B, C], A is the ultimate call site,
 B is the penultimate call site and so on.  A is considered
 'above' B for the purposes of transforms.  A is also
 considered 'top' and C 'bottom'.

 The transformSpec is one of:

 pruneRecursion        - Removes A from [A, B] if A has the same class
                         and method as B.  Note that overloaded variants
                         will be treated as "recursion" by this definition.

 prune:<elementSpec>   - Removes stack trace elements (STE)
                         which match the elementSpec.

 keep:<elementSpec>    - Keeps only STEs which match the
                         elementSpec.

 pruneAbove:<elementSpec>
                       - Remove all STE above the first STE
                         which matches the elementSpec. If no STE
                         matches the elementSpec, all STEs are pruned.

 pruneBelow:<elementSpec>
                       - Remove all STE below the last STE
                         which matches the elementSpec. If no STE
                         matches the elementSpec, all STEs are pruned.

 keepAbove:<elementSpec>
                       - Keeps only STE above the first STE
                         which matches the elementSpec. If no STE
                         matches the elementSpec, all STEs are kept.

 keepBelow:<elementSpec>
                       - Keeps only STE above the last STE
                         which matches the elementSpec. If no STE
                         matches the elementSpec, all STEs are kept.

 Multiple transforms may be specified with multiple flags.
 Note all such transformations are applied in order, and
 before any filtering of rows based on the stack trace (see below).
 Note that a row will not be filtered out automatically because no STEs
 are left.

 The elementSpec is a semicolon separated sequence of expressions:

 [contains:]<text>     - STE textual form contains the text
                         (default when no other operator is specified)

 underPackage:<package>
                       - STE class under package or any subpackage.
 inPackage:<package>   - STE class in the package (only).

 class:<class>         - STE class equal to that class.
 classRe:<classRegex>  - STE class that matches the given pattern.

 method:<method>       - STE method equal to that method.
 methodRe:<methodRegex>
                       - STE method that matches the given pattern.

 site:<classAndMethod> - STE "class.method" equal to the site.
 siteRe:<classAndMethodRegex>
                       - STE "class.method" that matches the given pattern.

 When multiple expressions are present, the elementSpec is treated as a
 conjunction.


ROW MATCHING FILTERS:
 --id=<filter>         - Allocation id (numeric)

 --allocated=<filter>  - Allocated class name

 --size=<filter>       - Allocation size in bytes (numeric)

 --thread=<filter>     - Allocating thread id (numeric)

 --stackTrace=<filter> - Joined allocation site stack trace

 Match attributes via a filter spec,
 [<comparison>:]<rhs> where comparison is one of:
     eq  - lhs == rhs, default comparison when none specified
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

OTHER FLAGS:

 --splitByTrace=true   - Splits a row with multiple trace elements [A,B,C]
                         into three rows, each one a single trace element
                         [A], [B], and [C].
                         Default: false

 --sort=<spec>         - Sorts the rows, where spec is a comma-separated
                         list of columns.  A column prefixed with a hyphen
                         is sorted in descending order (otherwise
                         ascending order).
                         Ex: --sort=thread,-size (asc. by thread, then
                         desc. by size)

 --format=csv|pretty   - Selects the output format.
                         Default: pretty


 Flags are processed in the order:
 1. Call site transformation
 2. Split by trace.
 3. Row filtering.
 4. Sorting.
 5. Formating.

EXAMPLES:

apat allocs parse file.alloc --sort=-size --thread=1 --format=csv \
    --allocated=eq:java.lang.String

  Dumps a CSV of all String allocations on thread 1 in descending size order.


apat allocs parse file.alloc --sort=id --size=gt:1024

  Pretty-prints in order of allocation everything above 1KB.


apat allocs parse file.alloc --sort=id --format=pretty \
    --traceTransform=prune:underPackage:java \
    --traceTransform=prune:underPackage:javax

  Pretty-prints in order of allocation, with all java.** and javax.**
  methods elided from the stack traces.


apat allocs parse file.alloc --sort=thread,id --format=pretty \
    --traceTransform=pruneAbove:underPackage:com.example \
    --stackTrace=re:.\*com\\.example\\..\*

  Pretty-prints in order of allocation but separated by thread,
  showing the ultimate allocation site as the last responsible method
  under the com.example.** package, and excluding all allocations that
  did not involve the com.example package.


apat allocs parse file.alloc --sort=thread,id --format=pretty \
    --traceTransform=pruneAbove:underPackage:com.example;method:\<init\> \
    --stackTrace=re:.\*com\\.example\\..\*

  Pretty-prints in order of allocation but separated by thread,
  showing the ultimate allocation site as the last responsible constructor
  under the com.example.** package, and excluding all allocations that
  did not involve the com.example package.
```

## Building from source
1.  Clone the repository:
`git clone https://github.com/madvay/android-analysis-tools.git`
2.  Build and run from the local repository: `./apat [args]`

## License
See the [LICENSE](LICENSE) and the [NOTICE](NOTICE) (per Section 4 d of the
License) files.
