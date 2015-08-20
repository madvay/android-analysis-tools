Improving memory performance walkthrough with apat:

I wanted to find out how much memory was being allocated on startup, in a method called inflate.

```sh
apat allocs top $AF --stackTrace=contains:inflate --traceTransform=pruneAbove:site:inflate  | less
```

```
XX.inflateLevelData   |   1688992
XX.inflate            |        16
```

Having confirmed I was allocating over[1] 1.5MB there, now I wanted to dig into what was being allocated:

```sh
apat allocs top $AF --stackTrace=contains:inflate --groupBy=allocated  | less
```

```
java.util.TreeMap$KeySet$1   |   439392
java.util.TreeMap            |   395424
java.util.TreeMap$Node       |   178432
java.lang.Object[]           |   161504
java.util.TreeMap$KeySet     |   131824
java.util.TreeSet            |   131808
long[]                       |    98608
boolean[]                    |    53248
X                            |    21600
char[]                       |    12192
X                            |     9120
...
```

Are these lots of small allocations, or just huge monolithic data structures?

```sh
# weight=id causes allocations to be counted, instead of a sum of bytes allocated
apat allocs top $AF --stackTrace=contains:inflate --groupBy=allocated --weight=id | less
```

```
java.util.TreeMap$KeySet$1   |   13731
java.util.TreeMap$KeySet     |    8239
java.util.TreeMap            |    8238
java.util.TreeSet            |    8238
java.util.TreeMap$Node       |    5576
```

Lot's of ~30byte allocs.  What is a TreeMap$KeySet$1?  Let's get a stack trace.

```sh
apat allocs list $AF --stackTrace=contains:inflate --allocated=contains:TreeMap\$KeySet\$1 \
  --sample=1  | less
```

```
Alloc#: 65284, Allocated Class: java.util.TreeMap$KeySet$1, Size: 32, Thread: 17
       java.util.TreeMap$KeySet.iterator(TreeMap.java:957)
       java.util.TreeSet.iterator(TreeSet.java:217)
       java.util.AbstractCollection.addAll(AbstractCollection.java:74)
       java.util.TreeSet.addAll(TreeSet.java:132)
       XX.YY.MM.getStyle(MM.java:112)
       XX.YY.MM.paintLayer(MM.java:88)
       XX.YY.MM.paint(MM.java:34)
       XX.YY.ZZ.inflateLevelData(XX.java:341)
       XX.YY.ZZ.inflate(XX.java:123)
       ...
```


Let's confirm that the iterator construction is where we're taking up memory:

```sh
apat allocs top $AF --stackTrace=contains:inflate --allocated=contains:Tree  | less
```

```
java.util.TreeMap$KeySet.iterator           |   439392
java.util.TreeSet.<init>                    |   395424
java.util.TreeMap.find                      |   178432
java.util.TreeMap.keySet                    |   131824
com.google.common.collect.Sets.newTreeSet   |   131808
```

Nothing ground breaking there.  Let's make sure that MM.getStyle is the best
bang for our buck to tackle within our own codebase, and see the line numbers as well:

```sh
apat allocs top $AF --stackTrace=contains:inflate --groupBy=allocator \
  --allocated=contains:TreeMap\$KeySet\$1 --traceTransform=pruneAbove:site:XX  | less
```

```
XX.YY.MM.getStyle(MM.java:125)   |   87904
XX.YY.MM.getStyle(MM.java:111)   |   87872
XX.YY.MM.getStyle(MM.java:112)   |   87872
XX.YY.MM.getStyle(MM.java:113)   |   87872
XX.YY.MM.getStyle(MM.java:119)   |   87872
```

Indeed.  What seemed like a quick way to test containment in a set of maximum 8 items
is responsible for ~400KB of allocation.

To confirm, we can check getStyle's contribution to allocation overall:

```sh
apat allocs top $AF  --traceTransform=pruneAbove:site:getStyle  | less
```

```
{none}              |   1870330
XX.YY.MM.getStyle   |   1393232
```

1.4MB out of 3.3MB total in the trace, and out of 1.9MB of inflate() (see top), are taken up in these containment tests.
Definitely worth getting rid of.

After I put in the fix, I reran the inflate() analysis from the beginning
```sh
apat allocs top $AF2 --stackTrace=contains:inflate --traceTransform=pruneAbove:site:inflate  | less
```

```
XX.inflateLevelData   |   352736
XX.inflate            |    13184
```

Success!

[1] The 'before' allocation amount was actually significantly higher than this, because the .alloc file is capped at 64K entries.
After the fix the inflation did in fact fit within the cap, so the win was higher than initially realized.
