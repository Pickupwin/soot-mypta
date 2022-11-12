package test;

import benchmark.internal.BenchmarkN;

class TypeBase{
  int id;
}

class TypeMid{
  TypeBase base;
}

class TypeTop{
  TypeMid m;
}

class TypeR{
  TypeR r;
  TypeMid m;
  TypeTop t;
}

public class TestPickupwin {

  public static void main(String[] args) {
    BenchmarkN.alloc(1); 
    TypeR tr = new TypeR();
    tr.r = tr;
    BenchmarkN.alloc(2);
    TypeTop tt = new TypeTop();
    BenchmarkN.alloc(3);
    tt.m = new TypeMid();
    BenchmarkN.alloc(4);
    TypeBase tb = new TypeBase();

    tr.r.r.t = tt;
    tr.r.t.m = tt.m;
    tt.m.base = tb;

    BenchmarkN.test(1, tr.t); 
    BenchmarkN.test(2, tr.m); 
    BenchmarkN.test(3, tr.t.m); 
    BenchmarkN.test(4, tr.t.m.base); 
  }
}


/*
 * 1: 2
 * 2:
 * 3: 3
 * 4: 4
 */