/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.codahale.metrics.newrelic.transformer;

import static com.codahale.metrics.newrelic.transformer.MeterTransformer.BASE_ATTRIBUTES;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codahale.metrics.Meter;
import com.codahale.metrics.newrelic.transformer.interfaces.CountingTransformer;
import com.codahale.metrics.newrelic.transformer.interfaces.MeteredTransformer;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Count;
import com.newrelic.telemetry.Gauge;
import com.newrelic.telemetry.Metric;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.collections.Sets;

class MeterTransformerTest {

  @Test
  void testTransform() {
    String name = "$$";
    Meter meter = new Meter();
    Metric expectedMetric =
        new Gauge(
            name,
            85299999.21,
            System.currentTimeMillis(),
            new Attributes().put("sourceType", "meter"));
    Count expectedCount =
        new Count(
            name,
            14.5,
            System.currentTimeMillis(),
            System.currentTimeMillis() + 12,
            new Attributes().put("something", "else"));

    Collection<Metric> expectedMeters = Sets.newSet(expectedMetric, expectedCount);

    MeteredTransformer converter = mock(MeteredTransformer.class);
    CountingTransformer countingTransformer = mock(CountingTransformer.class);

    when(converter.transform(name, meter, BASE_ATTRIBUTES)).thenReturn(expectedMeters);
    when(countingTransformer.transform(name, meter, BASE_ATTRIBUTES))
        .thenReturn(singleton(expectedCount));

    MeterTransformer testClass = new MeterTransformer(converter, countingTransformer);

    Collection<Metric> result = testClass.transform(name, meter);

    assertEquals(expectedMeters, result);
  }

  @Test
  void testRemove() {
    CountingTransformer counting = mock(CountingTransformer.class);
    MeterTransformer testClass = new MeterTransformer(null, counting);
    testClass.onMeterRemoved("something");
    verify(counting).remove("something");
  }
}
