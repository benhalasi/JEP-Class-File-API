package com.github.benhalasi.xp.classfileapi.template;

public class FooImplWithThisRef implements Foo {

  private final String foo;

  public FooImplWithThisRef() {
    this.foo = "foo";
  }

  public void foo() {
    System.out.printf("%s, %s%n", this.foo, "baz");
  }
}
