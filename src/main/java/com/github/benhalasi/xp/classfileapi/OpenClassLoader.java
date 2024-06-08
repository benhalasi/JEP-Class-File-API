package com.github.benhalasi.xp.classfileapi;

/**
 * {@link ClassLoader} that allows to define classes with {@link #defineClass(String, byte[])} delegating to {@code ClassLoader#defineClass(String, byte[], int, int}
 */
public class OpenClassLoader extends ClassLoader {
  public OpenClassLoader(ClassLoader parent) {
    super(parent);
  }

  public OpenClassLoader() {
    super();
  }

  public Class<?> defineClass(String name, byte[] bytes) {
    return defineClass(name, bytes, 0, bytes.length);
  }
}
