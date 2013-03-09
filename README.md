OVERVIEW:
---------

A distributed metadata benchmark targeting distributed file systems with scalable metadata servers. Generates a namespace (directories and files) and subsequently executes a workload consisting of various metadata operations (for example create, mkdir, ls, open, etc.) on the namespace elements. Runtime, throughput and latency of the single file system operations are measured and reported.

For the namespace generation, various creation strategies can be used. For example, the directory structure can be created using the Barabasi-Albert algorithm, which results in a scale-free (degree distribution follows power law) directory graph. When generating files, the parent directories can be selected based on different probability distributions, for example Zipfian distribution.

The operation mix in the workload can be specified by providing the probability of each operation type. If needed, more complex workload definition methods can be implemented.

The benchmark currently relies on [Hazelcast](http://www.hazelcast.org/), an open-source clustering middleware, for distributed, in-memory data storage and messaging. It is possible to use other clustering software as well.

On the backend, multiple file systems can be accessed by implementing and plugging in a simple file system client interface. Currently, an HDFS client implementation is offered.

Measurements are done using modified versions of the [YCSB++](http://www.pdl.cmu.edu/ycsb++/index.shtml) measurement classes.

Currently, the application has one master node that generates operations and dispatches them for execution to an arbitrary number of slave nodes. In the future, multiple, parallel master nodes should be supported.

Logging is done using SLF4J and Logback. By default, logs are exported to standard out and to a file.

Performance can be tuned by changing various configuration parameters. See "Usage" for more details.

USAGE:
------

You can start a master (generator) node on a machine by calling `java -jar metadatabench.jar -master`, and a slave node by `java -jar metadatabench.jar -slave`. This assumes that you have provided a configuration properties file in the same folder as the jar. See details below.

The jar file contains the compiled source code and the Hazelcast and Logback configuration XML files. In the same folder where the jar file is located, there must be a lib folder containing all libraries that the benchmark depends on (all of them are provided in the benchmark's repository), and an optional configuration properties file. Dependencies are currently managed manually, in the future e.g. Maven may be used.

The configuration properties file is called by default *metadatabench.properties* and should be located in the same folder as the jar, though you can specify a custom path or omit the file by providing the required parameters on the command line and using pre-configured defaults for the rest of the parameters (though this is not recommended).

A sample configuration file is provided, containing all possible parameters and a description for each of them. The list of available command line parameters and usage help can be read by calling `java -jar metadatabench.jar -help`.

Further configuration possibilities (mainly network-related) are provided by Hazelcast. Those can be customized by changing the included *hazelcast-master.xml* and *hazelcast-slave.xml* files. Logging configuration is located in *logback.xml*. You have to rebuild the jar file for those to take effect.

If you need to change generation strategies, file system clients or any other piece of the source code, you can import the source for example into Eclipse. After changing the sources, the new jar file can be created by running the Ant task *build-jar.xml* (the jar will be placed into the build/ folder inside the project). Please note that the Ant task assumes that the compiled classes are located in the bin/ folder inside the project directory (default behavior in Eclipse). You can of course change the Ant xml as well, if you would like to use a different folder or want Ant to compile the source code for you.