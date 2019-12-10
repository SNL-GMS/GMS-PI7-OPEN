# <sup> GMS Frameworks </sup><br>**Object Transmission and Storage**

The term **Domain Object** describes a Java class which represents
some specific entity in larger domain model. A `SignalDetection` or
`EventHypothesis` would be example domain objects in the GMS domain.

**The GMS preference is to have simple domain objects that act as
informational containers with no business logic.**  

The frameworks goal is to support writing a single class definiton
which can be used not only as a data container, but also directly as a
*Data Transmission Object* (DTO) and a *Data Access Object* (DAO).

## Object Annotations

The following COTS libraries are used for annotating an *abstract*
class to produce a single domain object that can be used
***directly*** for data transmission and persistence:

> NOTE: the direct usage of a domain object for data persistence is
> still being prototyped and may evolve from what is described here.

* [**Immutables**](https://immutables.github.io/immutable.html)
  provide annotations used to generate consistent *immutable* value objects
  from simple *abstract* class definitions.
* [**Jackson Annotations**](https://github.com/FasterXML/jackson-annotations)
  are used to to automatically encode and decode immutable objects to and
  from [JSON](https://en.wikipedia.org/wiki/JSON) for data transmission.
* [**BSON Library**](https://mongodb.github.io/mongo-java-driver/)
  provides annotations used to automatically encode and decode
  immutable objects to and from
  [BSON](https://en.wikipedia.org/wiki/BSON) for data persistence.

### Domain Object Definition Using Immutables

Consider a notional **Signal Detector** domain object.

For this example, we want a simple data container with an unique ID, a
station name, and a list of `SignalDetectionHypothesis`. To implement
this domain object, we would define the following *abstract* class in Java
and annotate with `Immutable` annotations.

**SignalDetection.java**
```java
 1. import org.immutables.value.Value;
 2:
 3: @Style(jdkOnly = true, allParameters = true)
 4: @Value.Immutable
 5: public abstract class SignalDetection {
 6:   public abstract UUID getId();
 7:   public abstract String getStationName();
 8:   public abstrct List<SignalDetectionHypothesis> getHypothesis();
 9: }
```

* The `@Value.Immutable` annotation marks this class as an
  **Immutable**. At compile-time **Immutables** will processes this
  annotation to generate the `ImmutableSignalDetection` implementation class
  that extends this *abstract* class.

* The `@Style` annotation customizes the generated code. The `jdkOnly`
  option indicates that the generated clases will not use any
  dependencies outside the Java Development Kit (JDK). The
  `allParameters` option indicates that every attribute becomes a
  parameter that must be set on building.
  
* Note that **Immutables** also supports declaring our class as an
  interface. We prefer abstract classes for consistency and because
  it's easier to add behavior methods to them if desired (interface
  behavior methods have to be marked as default).

The resulting generated `ImmutableSignalDetection` class would be the
implementation of the abstract class that would be used in development.

### Data Transfer Extensions Using Jackson

The `@JsonSerialze` and `@JsonDeserialize` annotations are a
convenience supported by **Immutables** which will inject **Jackson**
`@JsonCreator` and `@JsonProperty` for each member variable in the
generated class. Here is the `SignalDetector` example again shown with
the additional annotations.

**SignalDetection.java**
```java
 1: import com.fasterxml.jackson.annotation.*;
 2. import org.immutables.value.Value;
 3:
 4: @Style(jdkOnly = true, allParameters = true)
 5: @JsonSerialize(as = ImmutableSignalDetection.class)
 6: @JsonDeserialize(as = ImmutableSignalDetection.class)
 7: @Value.Immutable
 8: public abstract class SignalDetection {
 9:   public abstract UUID getId();
10:   public abstract String getStationName();
11:   public abstrct List<SignalDetectionHypothesis> getHypothesis();
12: }
```

**There is still active prototyping work being done in this area. Stay tuned for additional developments.**

