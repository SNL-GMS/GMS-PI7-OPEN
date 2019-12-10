/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gms.processors.intervalwait;

import java.time.Duration;
import java.time.Instant;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.PropertyValue;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.*;

@Tags({"interval, wait"})
@CapabilityDescription("Routes to wait or timeout based upon intilization time")
@ReadsAttributes({
    @ReadsAttribute(attribute = "initialization-time", description = "The time the process started."
        + "The value should be a date string in ISO-8601 instant format.")})
public class IntervalWaitProcessor extends AbstractProcessor {

  public static final PropertyDescriptor TIMEOUT = new PropertyDescriptor
    .Builder().name("Timeout")
    .description("The duration in milliseconds before timeout")
    .required(true)
    .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
    .build();

  public static final Relationship SUCCESS = new Relationship.Builder()
    .name("success")
    .description("Successful execution of wait time")
    .build();

  public static final Relationship FAILURE = new Relationship.Builder()
    .name("failure")
    .description("Unsuccessful execution of wait time - timeout occurred")
    .build();

  private List<PropertyDescriptor> descriptors;

  private Set<Relationship> relationships;

  @Override
  protected void init(final ProcessorInitializationContext context) {
    final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
    descriptors.add(TIMEOUT);
    this.descriptors = Collections.unmodifiableList(descriptors);

    final Set<Relationship> relationships = new HashSet<Relationship>();
    relationships.add(SUCCESS);
    relationships.add(FAILURE);
    this.relationships = Collections.unmodifiableSet(relationships);
  }

  @Override
  public Set<Relationship> getRelationships() {
      return this.relationships;
  }

  @Override
  public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
      return descriptors;
  }

  @OnScheduled
  public void onScheduled(final ProcessContext context) {}

  @Override
  public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
    FlowFile flowFile = session.get();
    if ( flowFile == null ) {
        return;
    }

    String timeoutStr = context.getProperty(TIMEOUT.getName()).getValue();
    Duration timeoutDuration = Duration.parse(timeoutStr);
    long timeout = timeoutDuration.toMillis();

    // Calculate difference for timeout.
    Instant initTime = Instant.parse(flowFile.getAttribute("initialization-time"));
    Instant currentTime = Instant.now();
    long timeDifference = currentTime.toEpochMilli() - initTime.toEpochMilli();

    // Determine if we timeout or if we wait.
    if (timeDifference > timeout) {
      session.transfer(flowFile, FAILURE);
    }
    else {
      // No timeout, so wait.
      flowFile = session.penalize(flowFile);
      session.transfer(flowFile, SUCCESS);
    }
  }
}
