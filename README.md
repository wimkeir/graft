# Graft

Graft is a static analysis tool for Java programs, based on the theory of *[code property graphs](https://www.sec.cs.tu-bs.de/pubs/2014-ieeesp.pdf)* (CPGs).

The CPG is generated from Java bytecode and stored in a local graph database. 
The graph can be traversed to find various common security vulnerabilities, including taint-related vulnerabilities.

A command line shell is also provided that allows the user to perform ad-hoc traversals on the CPG interactively.

## Building and running Graft

Graft is a Gradle project, and can be built by simply running `gradle build` (or `./gradlew build`).

During the build, two executable scripts are generated: `graft` and `graft-shell`. All references to `graft` in the Usage section refer to the `graft` executable.
`graft-shell` opens a Groovy shell on the Graft classpath (see `graft-shell` section in Usage).

## Usage

Graft needs to be initialized within the project by running `graft init` before the CPG can be built and analysed.

### `graft init`

This command initializes the Graft project by creating a `.graft` folder in the project's root directory.
This folder contains a properties file with the project's configuration, and a `db` folder that stores the graph database.

The user provides a name for the project, the project's target directory (where the Java classes are located) and classpath, and selects the graph database implementation.

After initialization, the database contains only a single root node - the CPG can be built with `graft build`.

### `graft build`

This command performs the initial construction of the CPG. 
If the database already contains a CPG, the user has the option of overwriting it.

Construction of the CPG takes some time, and should be run only once after running `graft init` for the first time.
If the program has changed and the CPG needs to be updated, the user should run `graft update` instead.

### `graft status`

Print the status of the CPG - number of nodes and edges, as well as classes changed since the last update.

### `graft update`

This command checks for changes in the target program that are not yet reflected in the CPG by comparing hash values of the class files.
If some classes have been changed, they will be updated in the CPG without affecting the rest of the graph.

This procedure is much faster than reconstructing the entire graph each time, and should probably be run after each incremental change in the program.

### `graft run <analysis>`

This command can be used to run a predefined analysis on the graph. 
Two such analyses (`TaintAnalysis` and `AliasAnalysis`) are built in and can be run with `graft run taint` and `graft run alias` respectively.


### `graft dot <dotfile>`

This command prints the CPG out to the given file in dot format for visualisation.
Not recommended for anything other than trivially small programs (this command is more useful for debugging really).

### `graft dump <dumpfile>`

This command dumps the CPG to the given file for portability. Again not recommended for larger graphs.

### `graft-shell`

This command opens the Graft shell. From here, the user can interactively inspect the CPG and run traversals on it.

Example work flow:

```java
import graft.cpg.structure.CodePropertyGraph
import static graft.traversal.__.*
import static graft.Const.*

cpg = CodePropertyGraph.fromFile('<db_filename>')

// get a list of all method entry nodes in the CPG
entries = cpg.traversal().entries().toList()

// get a list of all assignments to the variable 'x'
assigns = cpg.traversal().getAssignStmts().where(getTgt().has(NAME, 'x')).toList()

// dump the current CPG to a file
cpg.dump('<filename>')

// write the current CPG to a dotfile
cpg.toDot('<filename>')
```
