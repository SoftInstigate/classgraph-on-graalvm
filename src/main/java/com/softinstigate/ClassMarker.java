package com.softinstigate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ClassMarker {
    String name();

    int priority() default 10;
}