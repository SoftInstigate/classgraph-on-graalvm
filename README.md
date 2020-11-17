# Using ClassGraph on GraalVM

This repo demonstrates how to build a native image of a java application that uses [ClassGraph](https://github.com/classgraph/classgraph) with [GraalVM](https://github.com/oracle/graal).

## Build and test

Install GraalVM

```bash
$ mvn clean package
```

Execute the java image

```bash
$ java -jar target/classgraph-on-graalvm-1.0-SNAPSHOT.jar
Initializing class com.softinstigate.App
annotated classes [com.softinstigate.FindMe]
	annotated method executeMe
Executing targets [{ com.softinstigate.FindMe, executeMe }]
	class annotation name: findMe
	class annotation priority: 10
	method annotation name: executeMe
	method annotation priority: 10
Hello World!!
```

Execute the native image

```bash
$ ./target/cog
Executing targets [{ com.softinstigate.FindMe, executeMe }]
	class annotation name: findMe
	class annotation priority: 10
	method annotation name: executeMe
	method annotation priority: 10
Hello World!!
```

## The issue

`ClassGraph.scan()` simply doesn't work at runtime on a native image. However it does work at build time (see [Build-Time-Scanning](https://github.com/classgraph/classgraph/wiki/Build-Time-Scanning) on ClassGraph wiki).

## Approach

The `native-image.properties` includes the following option:

```
--initialize-at-build-time=com.softinstigate.App,io.github.classgraph.,nonapi.io.github.classgraph.
```

`com.softinstigate.App` performs the class scanning at class initialization time defining:

```java
public class App {
    static {
        // here executes ClassGraph.scan()
    }
}
```

Since the class `com.softinstigate.App` is initialized at build time by `native-image`, the scanning occurs.

The classes funded are saved to a static field and used in the class. The example code looks for classes and methods with given annotations and them invoke the methods using reflection.

Since reflection is used, native-image must run properly configured. The following section provides instructions on how to automatically generate the configuration.

See [Reflection Use in Native Images](https://www.graalvm.org/reference-manual/native-image/Reflection/) from GraalVM documentation.

## Generate native-image configuration

Run the app with the `native-image-agent`

```
$ mvn clean package
$ java -agentlib:native-image-agent=config-merge-dir=src/main/resources/META-INF/native-image/com.softinstigate/classgraph-on-graalvm/ -jar target/classgraph-on-graalvm-1.0-SNAPSHOT.jar
```

