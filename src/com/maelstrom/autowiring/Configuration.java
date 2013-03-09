package com.maelstrom.autowiring;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

import org.ho.yaml.Yaml;


public class Configuration {

    private HashMap<String, Object> root;

    @SuppressWarnings("unchecked")
    private Configuration() {
        try {
            root = Yaml.loadType(new File("config.yml"), HashMap.class);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    private static Configuration single;
    
    public static Configuration instance() {
        if (single == null)
            single = new Configuration();
        
        return single;
    }

    public String[] path(String... steps) { return steps; }

    public String getDefaultImplementationName(String interfaceName) {
        return get(path("class", interfaceName, "default")).toString();
    }
    
    public String getImplementationName(String interfaceName) {
        return getImplementationName(interfaceName, "current");       
    }
    
    public String getImplementationName(String interfaceName, String variant) {
        String prefix = get(path("project prefix")).toString();
        
        String[] path = path("class", interfaceName, variant);

        if (pathExists(path))
            return prefix + "." + get(path).toString();
        else
            return prefix + "." + getDefaultImplementationName(interfaceName);
               
    }
    
    Object get(String[] path) {
        return get(path, 0, root);
    }
    
    Object get(String[] path, int position, Object point) {
        if (path.length <= position)
            return point;
        
        assert point instanceof HashMap<?, ?>;
        
        HashMap<?,?> node = (HashMap<?,?>) point;
        
        Object descendant = node.get(path[position]);
        
        assert descendant != null;
        
        return get(path, position + 1, descendant);
    }
    
    boolean pathExists(String[] path) {
        return pathExists(path, 0, root);
    }
    
    boolean pathExists(String[] path, int position, Object point) {
        if (path.length <= position)
            return true;
        
        if (!(point instanceof HashMap<?, ?>)) return false;
        
        HashMap<?,?> node = (HashMap<?,?>) point;
        
        Object descendant = node.get(path[position]);
        
        if (descendant == null) return false;
        
        return pathExists(path, position + 1, descendant);
    }

    public String toString() {
        return root.toString();
    }
}
