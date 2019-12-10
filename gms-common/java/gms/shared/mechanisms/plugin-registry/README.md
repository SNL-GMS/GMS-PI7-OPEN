# Generic Plugin Registery

A generic plugin registry that uses an Interface, Plugin, as the base for ALL possible plugins. This allows the registry to also be the service loader.

## Pattern example

Imagine you want to simulate the bark of different types of dogs. A reasonable approach is to create an interface like so:

```java
public interface Dog {
    void bark();
}
...
public class Poodle implements Dog {
    void bark() {
        sound("yelp!")
    }
}
```

If you wanted your different types of dogs to be plugins, this could be the base interface for the plugin. But you could only use the service loader for Dogs:

```
ServiceLoader<Dog> dogServiceLoader = ServiceLoader.load(Dog.class)
```

This prevents the plugin registry from making use of the service loader; the client of the registry is responsible for "service loading" the plugins it uses.

### Common plugin interface

If we want to move the service-loading responsibilities to the registry, we need an interface that exists with the registry code that can be passed to the service loader.
This is what this prototype registry does. 

```java
public interface Plugin {
    //Empty; only purpose is to act as a common interface to pass to ServiceLoader
}
```

All plugins would implement this interface; they would ALSO implement whatever interface contains the right specifications for their needs:

```java
public class GmsPoodle implements Dog, Plugin {
    public void bark() { sound("yelp!"); }
}
```

### Plugin naming and versioning

The registry must be able to find a plugin by name and version. This version of the registry contains Name and Version annotations. The developer annotates the 
plugin implementation with name and version information:

```java
@Name("poodle")
@Version("1.0.0")
public class GmsPoodle implements Dog, Plugin {
    public void bark() { sound("yelp!"); }
}
```

The registry is capable of finding all plugins that implement Dog simply by passing that class to the lookup method, even though it has no relationship with Plugin:

```java
// Retrieve registry singleton instance
Registry registry = Registry.getRegistry();

// Load plugins
registry.loadAndRegister();

Dog myDog = registry.lookup(PluginInfo.from("poodle", "1.0.0"), Dog.class).get()
myDog.bark()
```

## Plugins using plugins

Plugins themselves can use the registry. Simply get the Registry singleton instance and call the lookup method. This must be done *after* the plugin object is
instantiated, otherwise the registry may not be in a final state when it is invoked. The Registry package provides an Initialize annotation which can be added to
a public method with no arguments that the registry will call after it has built its internal mapping, but before passing the plugin back to the client:

```java
public interface Yelper {
    void yelp();
}

...

@Name("yelper")
@Version("1.0.0")
public class GmsYelper implements Yelper, Plugin {
    public void yelp() { sound ("y-yelp!"); }
}

...

@Name("poodle")
@Version("1.0.0")
public class GmsPoodle implements Dog, Plugin {
    public Yelper plugin;

    @LogCall
    public void bark() { plugin.yelp(); }
    
    @Initialize
    public void preparePoodle() {
        Registry registry = Registry.getRegistry();
        
        this.plugin = registry.lookup(PluginInfo.from("yelper", "1.0.0"), Yelper.class).get()
    }
}
```

Plugins do not need to (and should not) call the loadAndRegister function - if any plugin methods are being invoked, it is because they have been loaded 
into the registry.

## Logging plugin method calls

Notice in the above example that the bark method is annotated with LogCall. Annotating a plugin method with LogCall will cause calls to that method to be logged.
Logged information include the name and version of the plugin being invoked, arguments to the method, and the return type and value of the method.