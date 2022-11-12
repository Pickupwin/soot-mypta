package test;

import benchmark.internal.BenchmarkN;
import benchmark.objects.A;
import benchmark.objects.B;

// Expected output:
// 1: 3
// 2: 2

public class ArraySensitivity {

  public static void main(String[] args) {
    BenchmarkN.alloc(1);
    B[] b = new B[2];
    BenchmarkN.alloc(2);
    b[0] = new B();
    BenchmarkN.alloc(3);
    b[1] = new B();

    BenchmarkN.alloc(4);
    A a = new A(b[1]);
    BenchmarkN.test(1, a.f);

    BenchmarkN.test(2, b[0]);
  }

}
