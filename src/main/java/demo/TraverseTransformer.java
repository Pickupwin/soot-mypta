package demo;

import soot.*;
import soot.util.Chain;

import java.util.List;
import java.util.Map;

public class TraverseTransformer extends SceneTransformer {
    @Override
    protected void internalTransform(String s, Map<String, String> map) {
        // SootClass c = Scene.v().getMainClass();
        Chain<SootClass> cs = Scene.v().getApplicationClasses();
        System.out.println("size = "+cs.size());
        for(SootClass c : cs){
            System.out.println(c.getName());
            List<SootMethod> ms = c.getMethods();
            Chain<SootField> fs = c.getFields();

            for (SootField f : fs) {
                System.out.println(f.getDeclaration());
                System.out.println(f.getType());
            }
            for (SootMethod m : ms) {
                System.out.println(m.getDeclaration());
                System.out.println(m.getReturnType());
                System.out.println(m.getParameterTypes());
            }
        }
    }
}
