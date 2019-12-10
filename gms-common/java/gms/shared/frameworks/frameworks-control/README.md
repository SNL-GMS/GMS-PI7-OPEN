# Framework for defining, instantiating, and executing Control classes.

A Control class is: A component deployed either as part of a Service or as part of the NiFi Controller runtime that implements one or more related Processing Operations. Control components may implement Processing Operations directly, or may delegate to a set of underlying Plugins. Control components are responsible for managing the execution of Processing Operations.

The goal of this library is simple: **Make it dead-simple to develop, deploy, and maintain Control components**.  This is accomplished by providing a framework for how to build Control classes.  Using the Control framework turns Control instantiation and deployment into trival affairs.  This framework also enhances robustness and consistency by reducing the amount of boilerplate that Control components need.

## Defining what a Control class needs: the `ControlContext`

`ControlContext` represents initialized versions of the common external dependencies (e.g. PluginRegistry, configuration, etc.) required by a Control component.  These dependencies are given defaults (via a `ControlContextDefaultsFactory`) such that creating this context is straightforward and standard in practice.

## Creating or deploying a Control class with `ControlFactory`

A properly implemented Control class can be instantiated using the `ControlFactory` like so:

```java
YourControl control = ControlFactory.createControl(YourControl.class);
```

Similarly, it can be executed as a service like so:

```java
ControlFactory.runService(YourControl.class);
```

These operations generally throw `IllegalArgumentException` when the Control class is not properly implemented.

### What it means for a Control component to be 'properly implemented'

To be usable with `ControlFactory`, a Control class must have these annotations:
 - Possess a `@Control` annotation (class-level) with the name of the control class; this name is used for looking up configuration values by this components' name
 - Possess a [@Path](https://jax-rs.github.io/apidocs/2.1/javax/ws/rs/Path.html) annotation (class-level).  This denotes the 'base URL' of the component when run as a service.  It is acceptable (common, even) to use the empty-string: `@Path("")`

**These annotations are typically on the Control interface instead of the control class itself**.  Those annotations are inherited by the Control class and visible there.

Additionally the Control class must define a `public static` operation that takes one parameter of type `ControlContext` and returns an instance of the Control class.  This is used for injecting a `ControlContext` to instantiate the Control class.