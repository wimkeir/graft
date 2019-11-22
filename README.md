# Graft

Graft is a static analysis tool for Java programs, based on the theory of *[code property graphs](https://www.sec.cs.tu-bs.de/pubs/2014-ieeesp.pdf)* (CPGs).

The CPG is generated from Java bytecode and stored in a local graph database. 
The graph can be traversed to find various common security vulnerabilities, including taint-related vulnerabilities.

A command line shell is also provided that allows the user to perform ad-hoc traversals on the CPG interactively.

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

The user can also define their own custom analyses by implementing the `graft.analysis.GraftAnalysis` interface.
They can be run with `graft run <fully-qualified-name>`, after ensuring that they are on the classpath.

### `graft shell`

This command opens the Graft shell, a command line interface that allows the user to run traversals interactively on the CPG.

The CPG is available as `cpg`, and all Graft constants and traversals in the DSL are in scope. 
Traversals can be run by using `cpg.traversal()` as the source.

### `graft dot <dotfile>`

This command prints the CPG out to the given file in dot format for visualisation.
Not recommended for anything other than trivially small programs (this command is more useful for debugging really).

### `graft dump <dumpfile>`

This command dumps the CPG to the given file for portability. Again not recommended for larger graphs.
