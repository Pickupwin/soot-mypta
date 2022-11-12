package test;

// Expected output:
// 1: 1
// 2: 2
// 3: 2
// 4: 2
// 5: 2

import benchmark.internal.BenchmarkN;
import benchmark.objects.A;
import benchmark.objects.G;
import benchmark.objects.H;
import benchmark.objects.I;

public class TestInterface {
    public static void main(String[] args) {
        BenchmarkN.alloc(1);
        I obj1 = new G();
        BenchmarkN.test(1, obj1);
        BenchmarkN.alloc(2);
        A obj2 = new A();
        BenchmarkN.test(2, obj2);
        BenchmarkN.test(3, obj1.foo(obj2));
        BenchmarkN.test(4, obj1.foo(obj2));
        BenchmarkN.test(5, obj1.foo(obj2));
        
    }
    
}
