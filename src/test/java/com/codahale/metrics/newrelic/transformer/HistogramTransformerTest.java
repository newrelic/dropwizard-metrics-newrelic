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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.newrelic.transformer.interfaces.CountingTransformer;
import com.codahale.metrics.newrelic.transformer.interfaces.SamplingTransformer;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Count;
import com.newrelic.telemetry.Gauge;
import com.newrelic.telemetry.Metric;
import java.util.Collection;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.collections.Sets;

class HistogramTransformerTest {

  @Test
  void testHistogramTransform() {
    Histogram histogram = new Histogram(new ExponentiallyDecayingReservoir());

    long startTime = System.currentTimeMillis();
    long endTime = startTime + 11000;

    Supplier<Attributes> baseAttrSupplier = HistogramTransformer.ATTRIBUTES_SUPPLIER;
    Count expectedCount = new Count("countery", 12.6, startTime, endTime, new Attributes());
    Metric expectedSamplingResult = new Gauge("samplery", 77.1, endTime, new Attributes());

    Collection<Metric> expectedMetrics = Sets.newSet(expectedCount, expectedSamplingResult);

    CountingTransformer counter = mock(CountingTransformer.class);
    SamplingTransformer sampler = mock(SamplingTransformer.class);

    when(counter.transform("history", histogram, baseAttrSupplier))
        .thenReturn(singleton(expectedCount));
    when(sampler.transform("history", histogram, baseAttrSupplier))
        .thenReturn(singleton(expectedSamplingResult));

    HistogramTransformer testClass = new HistogramTransformer(counter, sampler);
    Collection<Metric> result = testClass.transform("history", histogram);

    assertEquals(expectedMetrics, result);
  }

  @Test
  void testRemove() {
    CountingTransformer counting = mock(CountingTransformer.class);
    HistogramTransformer testClass = new HistogramTransformer(counting, null);
    testClass.onHistogramRemoved("jim");
    verify(counting).remove("jim");
  }
}
