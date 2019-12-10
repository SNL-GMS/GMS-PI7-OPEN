# PluginRegistry for dynamically looking up plugin class implementations

Primary goal is to make it easy and standard to define and load plugins from control classes.

## The `Plugin` interface

This interface is intended as the base interface of all application-specific (e.g. signal detection) plugin interfaces, i.e. those interfaces should extend `Plugin`.  Interfaces that don't extend `Plugin` cannot be used by this library.  `Plugin` defines method `String getName()` which is used to uniquely identify plugin implementations.

## Using Google AutoService

[Google AutoService](https://github.com/google/auto/tree/master/service) is used to generate (compile-time) the `META-INF/services` file that makes the plugin classes discoverable at runtime.  We use it like so:

```java
@AutoService(Plugin.class)
public interface SomePluginInterface extends Plugin {
    ....
}
```

**The argument to `@AutoService` must be `Plugin.class` (not `SomePluginInterface.class`) or plugins will not be discoverable at runtime.**

## The `PluginRegistry`

There is one way to get a registry: `PluginRegistry.create()`.  A `PluginRegistry` can be used to lookup plugins by their name(s) and required class (like the interface they must implement).

Looking up one plugin by it's name:

```java
PluginRegistry reg = PluginRegistry.create();
SomePluginInterface plugin = reg.get("some-name", SomePluginInterface.class);
```

Looking up multiple plugins (of the same type) at once by their names:

```java
PluginRegistry reg = PluginRegistry.create();
Collection<SomePluginInterface> plugins = reg.get(Set.of("a-name", "another-name"), SomePluginInterface.class);
```

All the retrieval operations of `PluginRegistry` throw `IllegalArgumentException` if a plugin is not found by the provided name or it is not of the required class (i.e. can't be casted to that type).