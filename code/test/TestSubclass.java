package test;

// Expected ouptut:
// 1: 1
// 2: 3 4

import benchmark.internal.BenchmarkN;
import benchmark.objects.A;
import benchmark.objects.P;
import benchmark.objects.Q;

public class TestSubclass {
    public static void main(String[] args) {
        BenchmarkN.alloc(1);
        A obj1 = new A();
        BenchmarkN.alloc(2);
        Q obj2 = new P(obj1);
        
        BenchmarkN.alloc(3);
        A obj3 = new A();
        BenchmarkN.alloc(4);
        A obj4 = new A();
        A obj34;
        if (args.length > 1) {
            obj34 = obj3;
        } else {
            obj34 = obj4;
        }


        BenchmarkN.test(1, obj2.getA());
        obj2.alias(obj34);
        BenchmarkN.test(2, obj2.getA());
    }
    
}
