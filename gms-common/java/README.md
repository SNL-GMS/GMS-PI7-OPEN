# GMS Common Java Code

Some `gradle` commands that can be run from this directory:

```bash
# Build all the Java code and run unit tests
gradle build

# Build all the java code (without running unit tests)
gradle build -x test

# Build and tag all the docker images
gradle docker dockerTag

# Build JavaDocs for the GMS system
gradle alljavadoc

```
