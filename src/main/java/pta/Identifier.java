package pta;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashSet;

public class Identifier {
    Identifier fa;
    String name;
    Identifier(String str){
        this.fa=null;
        this.name=str;
    }
    Identifier(Identifier f, String str){
        this.fa=f;
        this.name=str;
    }

    public Identifier addPrefix(String prefix){
        if (this.fa==null) {
            return new Identifier(prefix+this.name);
        }
        return new Identifier(this.fa.addPrefix(prefix), this.name);
    }
    public String getMethod(){
        if(this.fa!=null){
            return this.fa.getMethod();
        }
        return this.name;
    }
    public String toString(){
        if(this.fa!=null){
            return this.fa.toString()+"."+this.name;
        }
        return this.name;
    }
    public List<String> genStringList(Map<String, Set<String>> pointTo) {
        List<String> ret = new ArrayList<String>();
        String query = this.name;
        if (this.fa == null){
            query = this.name;
            if (!pointTo.containsKey(query)) {
                pointTo.put(query, new HashSet<String>());
            }
            pointTo.get(query).add(query);
            ret.addAll(pointTo.get(query));
            return ret;
        }
        else{
            
            for (String c : this.fa.genStringList(pointTo)) {
                query = c+"."+this.name;
                if (!pointTo.containsKey(query)) {
                    pointTo.put(query, new HashSet<String>());
                }
                pointTo.get(query).add(query);
                ret.addAll(pointTo.get(query));
                
            }
            return ret;
        }
    }
}

