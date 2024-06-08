package com.github.benhalasi.xp.classfileapi.template;

public class FooImplWithForLoop implements Foo {

  public FooImplWithForLoop() {}

  public void foo() {
    for (int i = 0; i < 2; i++){
      System.out.printf("%s, %d%n", "foo", i);
    }
  }
}
