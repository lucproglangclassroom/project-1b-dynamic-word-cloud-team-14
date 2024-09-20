[![Open in Codespaces](https://classroom.github.com/assets/launch-codespace-2972f46106e565e64193e422d61a12cf1da4916b45550586e14ef0a7c637dd04.svg)](https://classroom.github.com/open-in-codespaces?assignment_repo_id=16032384)

# Team project
```
Learning objectives

Transition from Java to Scala ("Scala as a better Java")
Functional versus nonfunctional requirements
An understanding of
stream processing
pipes and filters architecture
Observer design pattern
time/space complexity and scalability
separation of processing and I/O concerns
finding suitable third-party libraries
using libraries for logging and command-line argument parsing
test-driven development (TDD) in Scala and the command line/Gitpod or IntelliJ IDEA
Managing multiple dimensions of cognitive complexity
Functional requirements
```
## Usage
```
./topwords --cloud-size howmany --length-at-least minlength --window-size lastnwords

The defaults for these values are 10, 6, and 1000, respectively.
```

## Narrative
```
Your program, topwords, prints an updated "word cloud" for each sufficiently long word read from the standard input.

The program takes up to three positive command-line arguments:

cloud-size (c) indicates the size of the word cloud, i.e., the number of words in descending order of frequency to be shown
length-at-least (l as in Lima) indicates the minimum length of a word to be considered; shorter words are ignored
window-size (w) indicates the size of a moving window of n most recent words of sufficient length for which to update the word cloud
Your program then reads a continuous stream of words, separated by whitespace, from the standard input. For each word read, your program prints to standard output, on a single line, a textual representation of the word cloud of the form

w1: f1 w2: f2 ... whowmany: fhowmany

where w1 is the most frequent word within the last n words and f1 is its frequency, etc.

For full credit, your program must handle SIGPIPE on stdout by terminating gracefully (see section 3.2.1 of these notes).

Note that your program will not print anything for the first n - 1 words because it will not yet have enough words to generate the word cloud.
```

## Motivation
```
The idea is to connect this tool to a streaming data source, such as Twitter, or speech-to-text from a 24-hour news channel, and be able to tell from the word cloud in real time what the current "hot" topics are.
```

## Examples
```
In the following examples, lines that include colons are output lines. The others are input lines.

./topwords -c 3 -l 2 -w 5
EOF

./topwords -c 3 -l 2 -w 5
a b c
EOF

./topwords -c 3 -l 2 -w 5
a b c  
aa bb cc
aa bb aa bb
bb: 2 aa: 2 cc: 1
bb: 2 aa: 2 cc: 1
bb: 2 aa: 2 cc: 1
a b c
aa aa aa
aa: 3 bb: 2
aa: 3 bb: 2
aa: 4 bb: 1
EOF

We can also use a longer input as text, such as the full text of Les Miserables available under the Sakai course resources. When we run the program with the default arguments, we see output lines such as

Bishop: 22 Myriel: 16 Digitized: 10 bishop: 10 Google: 10 hospital: 9 Mademoiselle: 8 Magloire: 7 Madame: 7 episcopal: 6
...
Marius: 23 Fauchelevent: 13 himself: 9 Cosette: 8 Digitized: 8 marriage: 8 Google: 8 Thénardier: 7 coachman: 7 police: 6
...
Cosette: 32 Valjean: 30 Marius: 26 himself: 16 little: 12 Digitized: 9 Google: 9 should: 9 nothing: 8 Marins: 8

This suggests that the bishop character is more active toward the beginning of the story and over time focus shifts to other characters. (The occurrence of the words "Digitized" and "Google" appears to be an artifact of the automated digtization from the scanned text.)

Specifically, the first and last ten lines should look very close to these examples, especially for the eight most frequent words on each line, though you won't necessarily have the log output starting with [main] DEBUG, which shows the command-line arguments in effect:

❯ ./target/universal/stage/bin/imperative < lesmisrables01unkngoog_djvu.txt | head
[main] DEBUG edu.luc.cs.cs371.topwords.Main - howMany=10 minLength=6 lastNWords=1000 everyKSteps=10 minFrequency=3
bishop: 24 google: 17 myriel: 16 digitized: 15 public: 13 domain: 9 hospital: 8 mademoiselle: 7 search: 6 madame: 6
bishop: 24 google: 17 myriel: 16 digitized: 16 public: 13 domain: 9 hospital: 8 mademoiselle: 7 search: 6 expenses: 6
bishop: 24 google: 17 myriel: 16 digitized: 16 public: 11 domain: 8 hospital: 8 mademoiselle: 7 search: 6 expenses: 6
bishop: 24 google: 17 myriel: 16 digitized: 16 public: 10 hospital: 8 mademoiselle: 7 madame: 6 search: 6 expenses: 6
bishop: 24 google: 17 myriel: 16 digitized: 16 public: 9 hospital: 8 mademoiselle: 7 expenses: 6 search: 6 madame: 6
bishop: 24 google: 17 myriel: 16 digitized: 16 public: 9 mademoiselle: 8 hospital: 8 madame: 7 magloire: 7 expenses: 6
bishop: 25 myriel: 16 digitized: 16 google: 16 hospital: 8 public: 8 mademoiselle: 8 expenses: 7 madame: 7 magloire: 7
bishop: 25 myriel: 16 digitized: 16 google: 16 hospital: 9 mademoiselle: 8 expenses: 7 madame: 7 magloire: 7 public: 6
bishop: 25 myriel: 16 digitized: 16 google: 16 hospital: 9 mademoiselle: 8 expenses: 7 magloire: 7 madame: 7 search: 6
bishop: 26 myriel: 16 digitized: 16 google: 15 hospital: 9 mademoiselle: 8 expenses: 7 madame: 7 magloire: 7 public: 6

❯ ./target/universal/stage/bin/imperative < lesmisrables01unkngoog_djvu.txt | tail
[main] DEBUG edu.luc.cs.cs371.topwords.Main - howMany=10 minLength=6 lastNWords=1000 everyKSteps=10 minFrequency=3
cosette: 30 valjean: 16 marius: 13 little: 11 digitized: 10 google: 9 monsieur: 9 doctor: 9 children: 8 misérables: 8
cosette: 30 valjean: 15 marius: 13 little: 11 digitized: 10 google: 9 monsieur: 9 doctor: 9 children: 8 misérables: 8
cosette: 29 valjean: 15 marius: 13 little: 11 digitized: 10 google: 9 monsieur: 9 doctor: 9 children: 8 misérables: 8
cosette: 29 valjean: 15 marius: 13 little: 11 digitized: 10 google: 9 monsieur: 9 doctor: 9 children: 8 misérables: 8
cosette: 29 valjean: 15 marius: 12 little: 11 digitized: 10 doctor: 9 google: 9 children: 8 monsieur: 8 misérables: 8
cosette: 29 valjean: 15 marius: 12 digitized: 10 little: 10 google: 9 doctor: 9 misérables: 9 children: 8 monsieur: 8
cosette: 29 valjean: 15 marius: 12 little: 10 doctor: 9 digitized: 9 children: 8 misérables: 8 monsieur: 8 google: 8
cosette: 29 valjean: 14 marius: 12 digitized: 10 little: 10 doctor: 9 misérables: 9 google: 9 monsieur: 8 children: 8
cosette: 27 valjean: 13 marius: 12 little: 10 digitized: 10 google: 9 misérables: 9 doctor: 9 monsieur: 8 children: 8
cosette: 27 valjean: 13 digitized: 12 marius: 12 google: 11 little: 9 doctor: 9 misérables: 9 monsieur: 8 children: 8
```

## Nonfunctional requirements
```
Static

language: Scala
build tool: SBT
test framework: JUnit or ScalaTest (also consider using Checkers where appropriate)
to start working on this project, accept this GitHub classroom invitation based on the iterators-scala example, then copy your resulting private repository URL and clone the repository
use mutable variables and data structures as required, but use val to make immutable all variables that you don't need to update (i.e., most)
testability: for full credit, modularize your program in such a way that you can drive the main line moving stats functionality from both an application and test cases and your test cases do not have to look at unstructured (string) output
test coverage: for full credit, test each version using a test suite that includes the same range of cases including boundary cases; modularize your test code to minimize repetition (DRY); attempt to achieve at least 80% test coverage (use the sbt scoverage plugin
use a suitable third-party library for command-line argument parsing
use a suitable third-party library for logging (diagnostic output)
maintainability: follow good style and design principles
```
```
Dynamic

handle SIGPIPE as described in the notes (look for section 3.2.1)
scalability: for full credit, make sure your program works with arbitrarily large input sequences; imagine a service, such as Twitter, that continually produces lots of messages; this requires your program to run in constant space, which you can verify by running the program like so and using using a profiler such as VisualVM to watch its memory footprint:
yes helloworld | ./topwords > /dev/null
```

## Hints

## Reading the standard input as lines and words
```
This gives you an iterator of strings with each item representing one line. When the iterator has no more items, you are done reading all the input. (See also this concise reference.)

val lines = scala.io.Source.stdin.getLines

To break the standard input down further into words

val words =
import scala.language.unsafeNulls
lines.flatMap(l => l.split("(?U)[^\\p{Alpha}0-9']+"))
```
## Separating computational and output concerns
```
In the process tree example, input and output are both finite structures. By contrast, this project supports unbounded (infinite) sequences of words as input, which you can easily represent as iterators. Because the output can also be unbounded, however, there is no convenient way to represent it as a data structure.

Instead, think about the output as a sequence of events received by an observer responsible for performing the actual output. For each input word that the computational module processes, it produces a set of statistics and sends them to the observer as an event. Concretely, the event means that the computational module invokes a method on the observer, passing the stats as the argument.

For production, the observer simply prints the statistics to the console. For testing, the observer can collect a finite sequence of statistics and expose them in such a way that we can test assertions on them. The iterators example illustrates these techniques.
```

## EOF in IntelliJ IDEA console input
```
In the IntelliJ IDEA Run Tool View, to indicate end-of-file (EOF), use ctrl-D on Windows/Linux and command-D on Mac.
```
## Reading from a file
```
To redirect standard input to come from a file, it is convenient to run your applications from the command line outside of IDEA using sbt, e.g.:

sbt "runMain laufer.cs371.project2a.TopWords" < myinput.txt

This requires a standalone installation of sbt. However, in some environments, entering EOF in a terminal won't work properly when running programs through sbt.

Even better, to run your program outside of sbt, you can use the sbt-native-packager as shown in the iterators-scala example. This line should already exist near the end of build.sbt:

enablePlugins(JavaAppPackaging)

and this one in project/plugins.sbt:

addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.11")

After any change to your sources, you should rerun

sbt stage

and then run the generated start script from the command line like so

./target/universal/stage/bin/topwords
```

## Performance
```
These are the performance figures for my reference implementation; both laptops have 16 GB of RAM. If your solution takes more than two or three times as long as mine, you are probably overcomplicating things and should discuss your approach with the TA and/or instructor.

Something to think about: Why does it take only about three or four times as much to process 10 copies of the input than one copy?
```
## 2020 MacBook Air: CPU: 8-core Intel Core i5-1030NG7 (-MCP-) speed/min/max: 1100/400/400 MHz
```
❯ time ./target/universal/stage/bin/imperative < lesmisrables01unkngoog_djvu.txt > /dev/null
[main] DEBUG edu.luc.cs.cs371.topwords.Main - howMany=10 minLength=6 lastNWords=1000 everyKSteps=10 minFrequency=3
./target/universal/stage/bin/imperative < lesmisrables01unkngoog_djvu.txt >   3.20s user 0.52s system 151% cpu 2.457 total
❯ time ./target/universal/stage/bin/imperative < lestimes10.txt > /dev/null
[main] DEBUG edu.luc.cs.cs371.topwords.Main - howMany=10 minLength=6 lastNWords=1000 everyKSteps=10 minFrequency=3
./target/universal/stage/bin/imperative < lestimes10.txt > /dev/null  9.21s user 0.52s system 150% cpu 6.451 total
❯ time ./target/universal/stage/bin/imperative < lestimes100.txt > /dev/null
[main] DEBUG edu.luc.cs.cs371.topwords.Main - howMany=10 minLength=6 lastNWords=1000 everyKSteps=10 minFrequency=3
./target/universal/stage/bin/imperative < lestimes100.txt > /dev/null  59.32s user 2.20s system 102% cpu 1:00.14 total
```

## 2017 Dell XPS: CPU: dual core Intel Core i7-7500U (-MT MCP-) speed/min/max: 720/400/3500 MHz
```
❯ time ./target/universal/stage/bin/imperative < lesmisrables01unkngoog_djvu.txt > /dev/null
[main] DEBUG edu.luc.cs.cs371.topwords.Main - howMany=10 minLength=6 lastNWords=1000 everyKSteps=10 minFrequency=3
./target/universal/stage/bin/imperative < lesmisrables01unkngoog_djvu.txt >   2.91s user 0.25s system 179% cpu 1.762 total
❯ time ./target/universal/stage/bin/imperative < lestimes10.txt > /dev/null
[main] DEBUG edu.luc.cs.cs371.topwords.Main - howMany=10 minLength=6 lastNWords=1000 everyKSteps=10 minFrequency=3
./target/universal/stage/bin/imperative < lestimes10.txt > /dev/null  9.16s user 0.29s system 150% cpu 6.254 total
❯ time ./target/universal/stage/bin/imperative < lestimes100.txt > /dev/null
[main] DEBUG edu.luc.cs.cs371.topwords.Main - howMany=10 minLength=6 lastNWords=1000 everyKSteps=10 minFrequency=3
./target/universal/stage/bin/imperative < lestimes100.txt > /dev/null  54.62s user 0.94s system 106% cpu 52.285 total
```

## Extra credit
```
1 Read from a file an "ignore list" of words to be ignored regardless of length.
0.5 Treat words case-insensitively, i.e., ignore capitalization and the like.
0.5 Add an command-line argument for updating the word cloud only every k steps.
1 Add an command-line argument for a minimum frequency to include a word in the word cloud.
2 Dynamic graphical visualization of the word cloud.
Be sure to indicate in your README if you have included any extra credit feature.
```

## Submission
```
Your GitHub Classroom repository will be shared automatically with the instructor and TA. As you work on this project, keep your repo current by committing and pushing frequently. Once your project is ready to grade, submit a brief inline note to Sakai with the complete repo URL.
```

## Grading criteria
```
2 correctness (w.r.t. functional and nonfunctional immutability/purity requirements)
0.5 testability
1 automated test suite with good coverage
0.5 maintainability
0.5 command-line argument parsing
0.5 logging
1 scalability (include profiler screenshot)
```
## References
```
The process tree and iterators examples, while deliberately over-engineered for examples this size, demonstrate the following:

Scala console I/O
multiple main classes in the same project
using Scala traits to avoid code duplication (DRY)
separation of I/O and functional concerns
using the Iterator and Observer patterns to handle indefinite streams
For the purpose of this project, focus on the imperative/mutable implementations of these examples.

See also the consoleapp-java example.
```