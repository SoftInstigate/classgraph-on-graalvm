package com.softinstigate;

import java.util.HashSet;
import java.util.Set;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;

/**
 * Hello world!
 *
 */
public class App {
    static Set<Target> targets = new HashSet<>();

    static {
        System.out.println("Initializing class " + App.class.getName());

        try (var scanResult = new ClassGraph().disableModuleScanning() // added for GraalVM
                // .disableDirScanning() // added for GraalVM
                // .disableNestedJarScanning() // added for GraalVM
                // .disableRuntimeInvisibleAnnotations() // added for GraalVM
                .addClassLoader(ClassLoader.getSystemClassLoader()) // see
                                                                    // https://github.com/oracle/graal/issues/470#issuecomment-401022008
                .enableAnnotationInfo().enableMethodInfo().initializeLoadedClasses().scan(8)) {

            var annotatedClasses = scanResult.getClassesWithAnnotation(ClassMarker.class.getName());

            System.out.println("annotated classes " + annotatedClasses.getNames());

            for (var clazz : annotatedClasses) {
                for (var method : clazz.getDeclaredMethodInfo()) {
                    if (method.hasAnnotation(MethodMarker.class.getName())) {
                        System.out.println("\tannotated method " + method.getName());
                        targets.add(new Target(clazz.getName(), method.getName()));
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        if (targets == null) {
            System.out.println("targets is null");
            System.exit(1);
        }

        if (targets.isEmpty()) {
            System.out.println("targets is empty");
            System.exit(2);
        }

        System.out.println("Executing targets " + targets);

        for (var t : targets) {
            try {
                var clazz = Class.forName(t.clazz);
                var o = clazz.getDeclaredConstructor().newInstance();

                ClassMarker cmarker = clazz.getAnnotation(ClassMarker.class);
                System.out.println("\tclass annotation name: " + cmarker.name());
                System.out.println("\tclass annotation priority: " + cmarker.priority());

                var m = clazz.getDeclaredMethod(t.method);

                MethodMarker mmarker = m.getAnnotation(MethodMarker.class);
                System.out.println("\tmethod annotation name: " + mmarker.name());
                System.out.println("\tmethod annotation priority: " + mmarker.priority());

                m.invoke(o);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }
}

class Target {
    public final String clazz;
    public final String method;

    public Target(String clazz, String method) {
        this.clazz = clazz;
        this.method = method;
    }

    @Override
    public String toString() {
        return "{ " + this.clazz + ", " + this.method + " }";
    }
}
