package demo;

import soot.*;

public class Traverse {
    public static void main(String[] args) {
        String classpath = args[0];
        String clazzname = args[1];

        PackManager.v().getPack("wjtp").add(
                new Transform("wjtp.traverse", new TraverseTransformer())
        );

        System.out.println(classpath);
        soot.Main.main(new String[] {
                "-w",
                "-f", "J",
                "-p", "jb", "use-original-names:true",
                "-p", "wjtp.traverse", "enabled:true",
                "-soot-class-path", classpath,
                clazzname
        });
    }
}
