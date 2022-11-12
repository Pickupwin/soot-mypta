package test;

// Expected output:
// 1: 1 2

import benchmark.internal.BenchmarkN;
import benchmark.objects.A;

public class TestIfElse {
    public static void main(String[] args) {
        BenchmarkN.alloc(1);
        A obj1 = new A();
        BenchmarkN.alloc(2);
        A obj2 = new A();
        A obj3;
        if(Math.random() >= 0.5) {
            obj3 = obj1;
        } else {
            obj3 = obj2;
        }
        BenchmarkN.test(1, obj3);
    }
    
}
