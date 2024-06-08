package com.github.benhalasi.xp.classfileapi.template;

public class FooImpl implements Foo {
  public FooImpl() {
  }

  public void foo() {
    System.out.printf("%s, %s%n", "foo", "baz");
  }
}
