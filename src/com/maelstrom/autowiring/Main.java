package com.maelstrom.autowiring;

import java.lang.reflect.InvocationTargetException;

import com.maelstrom.astronomicon.Kind;

public class Main {

    public static void main(String[] args) {
        try {
            System.out.println(
                new ClassLoader()
                    .implementationOf("IBlock")
                    .getConstructor(Kind.class)
                    .newInstance(Kind.VOID)
                    .toString());
        }
        catch (InstantiationException
             | NoSuchMethodException
             | IllegalAccessException
             | IllegalArgumentException
             | InvocationTargetException e) 
        {
            e.printStackTrace();
        }
    }

}
