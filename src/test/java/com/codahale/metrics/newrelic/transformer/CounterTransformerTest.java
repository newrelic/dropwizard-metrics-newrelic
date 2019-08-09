/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.codahale.metrics.newrelic.transformer;

import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Counter;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Gauge;
import com.newrelic.telemetry.Metric;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CounterTransformerTest {

  private static final String COUNTER_NAME = "countme";
  private static final Attributes EMPTY_ATTRIBUTES = new Attributes().put("sourceType", "counter");
  private static final long timestamp = System.currentTimeMillis();
  private Clock clock;

  @BeforeEach
  void setUp() {
    clock = mock(Clock.class);
    when(clock.getTime()).thenReturn(timestamp);
  }

  @Test
  void testFirstTimeSeen() {
    Counter counter = new Counter();
    counter.inc(44);
    Gauge expected = new Gauge(COUNTER_NAME, 44, timestamp, EMPTY_ATTRIBUTES);

    CounterTransformer converter = new CounterTransformer(clock);

    Collection<Metric> result = converter.transform(COUNTER_NAME, counter);

    assertEquals(singleton(expected), result);
  }

  @Test
  void testSecondTimeSeen() {
    Counter counter = new Counter();
    counter.inc(44);
    Gauge expected = new Gauge(COUNTER_NAME, 30, timestamp, EMPTY_ATTRIBUTES);

    CounterTransformer converter = new CounterTransformer(clock);

    converter.transform(COUNTER_NAME, counter);
    counter.dec(14);
    Collection<Metric> result = converter.transform(COUNTER_NAME, counter);

    assertEquals(singleton(expected), result);
  }
}
