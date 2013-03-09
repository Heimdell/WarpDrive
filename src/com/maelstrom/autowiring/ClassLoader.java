package com.maelstrom.autowiring;

public class ClassLoader {

    public Class<?> implementationOf(String interfaceName) {
        String name = Configuration.instance().getImplementationName(interfaceName);
        
        try {
            return Class.forName(name); 
        }
        catch (IllegalArgumentException
             | SecurityException
             | ClassNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public Class<?> constructorOf(String interfaceName, String variant) {
        String name = Configuration.instance().getImplementationName(interfaceName, variant);
        
        try {
            return Class.forName(name); 
        }
        catch (IllegalArgumentException
             | SecurityException
             | ClassNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
