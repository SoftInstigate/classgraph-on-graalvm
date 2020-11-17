package com.softinstigate;

@ClassMarker(name = "findMe")
public class FindMe {
    @MethodMarker(name = "executeMe")
    public void executeMe() {
        System.out.println("Hello World!!");
    }
}
