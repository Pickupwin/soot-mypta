package test;

import benchmark.internal.BenchmarkN;
import test.myClass.A;
import test.myClass.B;

public class FuncClone {

  public FuncClone() {}

  public void test() {
    BenchmarkN.alloc(2);
    B b2 = new B();
    BenchmarkN.alloc(3);
    B b3 = new B();
    BenchmarkN.alloc(4);
    A a2 = new A(b2);
    BenchmarkN.alloc(5);
    A a3 = new A(b3);

    BenchmarkN.test(1, a2.f);
    BenchmarkN.test(2, a3.f);
  }
  

  public static void main(String[] args) {

    BenchmarkN.alloc(1);
    FuncClone fs2 = new FuncClone();
    fs2.test();
    
  }

}


/*
 * 1: 2
 * 2: 3
 */