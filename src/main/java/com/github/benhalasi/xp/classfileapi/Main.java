package com.github.benhalasi.xp.classfileapi;

import com.github.benhalasi.xp.classfileapi.template.Foo;
import com.github.benhalasi.xp.classfileapi.template.FooImpl;
import com.github.benhalasi.xp.classfileapi.template.FooImplWithForLoop;
import com.github.benhalasi.xp.classfileapi.template.FooImplWithThisRef;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.Opcode;
import java.lang.classfile.constantpool.LoadableConstantEntry;
import java.lang.classfile.instruction.ConstantInstruction;
import java.lang.constant.ClassDesc;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.lang.classfile.ClassTransform.transformingMethodBodies;

@SuppressWarnings("preview")
public class Main {
  private static final OpenClassLoader CLASS_LOADER = new OpenClassLoader();
  private static final ClassFile CLASS_FILE = ClassFile.of();
  private static final Path templateDir = Path.of("target/classes/com/github/benhalasi/xp/classfileapi/template");

  private static final ClassTransform REPLACE_FOO_BAR = transformingMethodBodies(
      _ -> true,
      (builder, element) -> {
        switch (element) {
          case ConstantInstruction.LoadConstantInstruction fooLci when fooLci.constantValue().equals("foo") -> {
            LoadableConstantEntry loadableConstantEntry = builder.constantPool().loadableConstantEntry("bar");
            ConstantInstruction.LoadConstantInstruction barLci = ConstantInstruction.ofLoad(Opcode.LDC, loadableConstantEntry);
            builder.with(barLci);
          }
          default -> builder.with(element);
        }
      }
  );

  static {
    // Use template classes and methods, so they aren't dead-code, to force the compiler to compile them.
    List.of(
        new FooImpl(),
        new FooImplWithThisRef(),
        new FooImplWithForLoop()
    ).forEach(foo -> {
      System.out.println(foo.getClass().getSimpleName());
      foo.foo();
    });
  }

  public static void main(String[] args) throws IOException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    System.out.println("Hello world!");

    ClassTransform transform = ClassTransform.ACCEPT_ALL;

    Class<?> fooImpl = copy("BarImpl", "FooImpl.class", transform);
    Foo fooImplInstance = (Foo) fooImpl.getDeclaredConstructor().newInstance();
    fooImplInstance.foo();

    Class<?> barImplWithForLoop = copy("BarImplWithForLoop", "FooImplWithForLoop.class", transform);
    Foo barImplWithForLoopInstance = (Foo) barImplWithForLoop.getDeclaredConstructor().newInstance();
    barImplWithForLoopInstance.foo();

    try {
      Class<?> barImplWithThisRef = copy("BarImplWithThisRef", "FooImplWithThisRef.class", transform);
      Foo barImplWithThisRefInstance = (Foo) barImplWithThisRef.getDeclaredConstructor().newInstance();
      barImplWithThisRefInstance.foo();
    } catch (VerifyError e) {
      e.printStackTrace(System.err);
    }

    System.out.println("exit");
  }

  private static Class<?> copy(String generatedClassName, String templateClassFileName, ClassTransform transform) throws IOException {
    ClassModel templateClassModel = CLASS_FILE.parse(templateDir.resolve(templateClassFileName));
    byte[] bytes = CLASS_FILE.transform(templateClassModel, ClassDesc.of(generatedClassName), transform);

    List<VerifyError> verifyErrorList = CLASS_FILE.verify(bytes);
    verifyErrorList.forEach(System.err::println);
    if (verifyErrorList.isEmpty()) {
      System.out.println("Bytecode verified");
    }

    dumpClassFile(generatedClassName, bytes);
    return CLASS_LOADER.defineClass(generatedClassName, bytes);
  }

  @SuppressWarnings("SameParameterValue")
  private static void dumpClassFile(String className, byte[] bytes) throws IOException {
    File classFile = Files.createFile(Path.of(".", "%s.class".formatted(className))).toFile();
    classFile.deleteOnExit();
    try (FileOutputStream fileOutputStream = new FileOutputStream(classFile)) {
      fileOutputStream.write(bytes);
      fileOutputStream.flush();
    } catch (IOException e) {
      e.printStackTrace(System.err);
    }
    System.out.printf("Dumped class file `%s`%n", classFile.getCanonicalPath());
  }
}