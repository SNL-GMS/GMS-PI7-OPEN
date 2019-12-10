# NiFi Invoke Interval Processor
## Overview
This is the NiFi processor that initiates processing after a configured time interval. The output of the `mvn build` will be a NiFi Application Resource (NAR) file to include in the NiFi deployment.

## Building the project
### Requirements
* Maven
* Java 8

### Steps
1. `mvn compile`
2. `mvn test`

-- or --

1. `mvn install`

## Configuring this processor
You need to make sure to hit the **PROCESSING** endpoint, NOT the *stations* endpoint. If you do the latter, you will have errors because it will return a *list of stations*, and it will break the parsing if the JSON document it gets back isn't a single station.

## Note on ISO 8601 and Java Duration.parse()
Java's `Duration.parse(CharSequence text)` **does not use the ISO 8601 year and month**. The largest unit of time used in the implementation is a day. For reference, review the documentation on Duration, where it states:
```
This will parse a textual representation of a duration, including the string produced by toString(). The formats accepted are based on the ISO-8601 duration format PnDTnHnMn.nS with days considered to be exactly 24 hours.
```

## Logging
To see the logging messages for debug, you need to mess with NiFi's logging. [This question](https://community.hortonworks.com/questions/65937/in-apache-nifi-where-can-i-find-the-debug-log-whic.html) on the HortonWorks website has information on how to do this.

## Notes
### NiFi
* https://github.com/apache/nifi/blob/master/nifi-nar-bundles/nifi-standard-bundle/nifi-standard-processors/src/main/java/org/apache/nifi/processors/standard/GenerateFlowFile.java
* https://nifi.apache.org/docs/nifi-docs/html/developer-guide.html#onenabled
* https://nifi.apache.org/docs/nifi-docs/html/developer-guide.html#ingress
* https://github.com/apache/nifi/blob/master/nifi-api/src/main/java/org/apache/nifi/processor/ProcessContext.java
* https://github.com/apache/nifi/blob/master/nifi-api/src/main/java/org/apache/nifi/components/PropertyDescriptor.java
* https://github.com/apache/nifi/blob/master/nifi-api/src/main/java/org/apache/nifi/components/PropertyValue.java
* https://github.com/apache/nifi/blob/master/nifi-api/src/main/java/org/apache/nifi/flowfile/FlowFile.java
* https://github.com/apache/nifi/blob/master/nifi-api/src/main/java/org/apache/nifi/provenance/ProvenanceReporter.java

### Unirest
* https://stackoverflow.com/questions/23630681/how-to-parse-json-results-from-unirest-call
* https://fasterxml.github.io/jackson-databind/javadoc/2.7/com/fasterxml/jackson/databind/JsonNode.html
* http://unirest.io/java.html
* https://stackoverflow.com/questions/16788213/jackson-how-to-transform-jsonnode-to-arraynode-without-casting

### WireMock
* http://wiremock.org/docs/stubbing/

### Duration
* https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html
* https://en.wikipedia.org/wiki/ISO_8601#Durations

