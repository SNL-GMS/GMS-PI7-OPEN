# <sup> GMS Frameworks </sup><br>**System Configuration**

The *System Configuration* framework is a **key-value** store for GMS
system configuration values.

* A single **SystemConfig** class provides access to configuration
  *values* based on *key* names.
* The **SystemConfig** is tied to a *control name* on construction.
* **Key-value** pairs may be scoped to the *control name* with
  **key-values** specific to a *control name* overriding generic
  **key-value** definitions.
* Configuration *values* may be returned in a variety of types
  (*String*, *int*, *long*, *double*, *boolean*, *Path*).
* A **MissingResourceException** is thrown if no value can be
  found for a requested key.

Configuration **key-values** are stored in a hierarchy of backend
repositories:

* Configuration values are primarily managed via an [**etcd
  server**](https://etcd.io).
* Configuration values can be overriden for development and testing
  with a local `configuration_overrides.properties` file in your home
  directory.

## Usage

* Construct a **SystemConfiguration** client, specifying the *name* of the control 
  class it is associated with:
  ```java
  SystemConfig systemConfig = SystemConfig().create("control-class-name");
  ```

* Retrieve a system configuration value:
  ```java
  try {
      String host = systemConfig.getValue(SystemConfig.HOST);
  } catch (MissingResourceException e) {
      logger.error(e.toString());
  }
  ```
  
* Retrieve a system configuration value as an *integer*:
  ```java
  try {
      int port = systemConfig.getValueAsInt(SystemConfig.PORT);
  } catch (MissingResourceException | NumberFormatException e) {
      logger.error(e.toString());
  }
  ```

* Retrieve a system configuration value as an *long*:
  ```java
  try {
      long value = systemConfig.getValueAsInt("key");
  } catch (MissingResourceException | NumberFormatException e) {
      logger.error(e.toString());
  }
  ```
  
* Retrieve a system configuration value as an *double*:
  ```java
  try {
      double value = systemConfig.getValueAsDouble("key");
  } catch (MissingResourceException | NumberFormatException e) {
      logger.error(e.toString());
  }
  ```

* Retrieve a system configuration value as an *boolean*:
  ```java
  try {
      boolean value = systemConfig.getValueAsBoolean("key");
  } catch (MissingResourceException | IllegalFormatException e) {
      logger.error(e.toString());
  }
  ```
  
## Common Configuration Names

Several common configuration names are defined as static strings in
the **SystemConfig** class for reference.

| Name                             | Type   | Description |
|:---------------------------------|:-------|:------------|
|**HOST**                          | String | Service host identifier. |
|**PORT**                          | int    | Port at which a service is provided. |
|**IDLE_TIMEOUT_MILLIS**           | int    | The number of milliseconds before a request should time out. |
|**MIN_THREADS**                   | int    | The minimum number of threads that should be available to satisfy service requests. |
|**MAX_THREADS**                   | int    | The maximum number of threads that should be available to satisfy service requests. |
|**PROCESSING_CONFIGURATION_ROOT** | Path   | File system location of the processing configuration. |

## Scope and Overrides

Key lookup is scoped so that a **key-value** definition specific to a
control name overrides a generic **key-value** definition.

Consider the following key-value pairs:

```properties
 port = 8080
 
 detection.host = detection
 detection.port = 80
  
 correlation.host = correlation
```
In this example:
* The value of *"port"* resolved for the *detection* control would be *80*. 
  * Since a control-specific key `detection.port` was defined, that value overrides
    the value for `port`.
* The value of *"port"* resolved for the *correlation* component would be *8080*.
  * Since `correlation.port` was not defined, the value for `port` is returned. 

In Java:
```java
SystemConfig detectionConfig = SystemConfig().create("detection");
int port = detectionConfig.getValueAsInt("port");  // would return 80

SystemConfig correlationConfig = SystemConfig().create("correlation");
int port = correlationConfig.getValueAsInt("port");  // would return 8080
```

*Key-value* definitions can also be overridden for development by
specifying them in a *configuration_overrides.properties* text file in
your home directory.

Key resolution will follow this scoping, with the key resolving to the first
found definition:

* Control-specific value from **Overrides File**
  * Control-specific value from **Etcd**
    * Generic value from **Overrides File**
      * Generic value from **Etcd**

## Etcd

The key value store is managed as an [**etcd**](https://etcd.io)
service. The service endpoint by default will resolve to *etcd:2379*.
In a docker swarm cluster, this will resolve to the local system etcd
container.

During development, you may set an environment variable
**GMS_ETCD_ENDPOINTS** to point to a specific etcd service
endpoint.

* Each enclave running in a Docker Swarm will expose their etcd port.
  You can point to a specific testbed for development.  TODO: what
  would this look like?

* Run `export GMS_ETCD_ENDPOINTS=localhost:2379` to use a local etcd
  server running on your development machine.


