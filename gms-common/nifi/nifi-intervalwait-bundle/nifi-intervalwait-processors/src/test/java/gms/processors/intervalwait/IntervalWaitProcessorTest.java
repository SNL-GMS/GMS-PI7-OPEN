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

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.Test;


public class IntervalWaitProcessorTest {

  private TestRunner testRunner;

  @Before
  public void init() {
      testRunner = TestRunners.newTestRunner(IntervalWaitProcessor.class);
  }

  @Test
  public void testProcessorWait() {

    testRunner.setProperty(IntervalWaitProcessor.TIMEOUT, "PT5s");

    Map<String, String> attributesMap = new HashMap<>();
    attributesMap.put("initialization-time", Instant.now().toString());

    testRunner.enqueue(new byte[0], attributesMap);

    testRunner.run();
    testRunner.assertQueueEmpty();
    testRunner.assertAllFlowFilesTransferred(IntervalWaitProcessor.SUCCESS);
  }

  @Test
  public void testProcessorTimeout() {

    testRunner.setProperty(IntervalWaitProcessor.TIMEOUT, "PT5s");

    Map<String, String> attributesMap = new HashMap<>();
    attributesMap.put("initialization-time", (Instant.now().minusMillis(10000)).toString());

    testRunner.enqueue(new byte[0], attributesMap);

    testRunner.run();
    testRunner.assertQueueEmpty();
    testRunner.assertAllFlowFilesTransferred(IntervalWaitProcessor.FAILURE);
  }

}
