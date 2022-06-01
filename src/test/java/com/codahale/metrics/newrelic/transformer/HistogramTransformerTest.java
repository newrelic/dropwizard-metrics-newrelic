/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.codahale.metrics.newrelic.transformer;

import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.newrelic.transformer.customizer.MetricAttributesCustomizer;
import com.codahale.metrics.newrelic.transformer.customizer.MetricCustomizerTestUtils;
import com.codahale.metrics.newrelic.transformer.interfaces.CountingTransformer;
import com.codahale.metrics.newrelic.transformer.interfaces.SamplingTransformer;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.metrics.Count;
import com.newrelic.telemetry.metrics.Gauge;
import com.newrelic.telemetry.metrics.Metric;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.collections.Sets;

class HistogramTransformerTest {

  CountingTransformer counter = mock(CountingTransformer.class);
  SamplingTransformer sampler = mock(SamplingTransformer.class);

  @BeforeEach
  public void setup() {
    reset(counter);
    reset(sampler);
  }

  @Test
  void testTransform() {
    Histogram histogram = new Histogram(new ExponentiallyDecayingReservoir());

    long startTime = System.currentTimeMillis();
    long endTime = startTime + 11000;

    Count expectedCount = new Count("countery", 12.6, startTime, endTime, new Attributes());
    Metric expectedSamplingResult = new Gauge("samplery", 77.1, endTime, new Attributes());

    Collection<Metric> expectedMetrics = Sets.newSet(expectedCount, expectedSamplingResult);

    when(counter.transform(eq("history"), eq(histogram), notNull()))
        .thenReturn(singleton(expectedCount));
    when(sampler.transform(eq("history"), eq(histogram), notNull()))
        .thenReturn(singleton(expectedSamplingResult));

    HistogramTransformer testClass = new HistogramTransformer(counter, sampler);
    Collection<Metric> result = testClass.transform("history", histogram);

    assertEquals(expectedMetrics, result);
  }

  @Test
  void testTransformWithAttributeAndNameCustomization() {
    Histogram histogram = new Histogram(new ExponentiallyDecayingReservoir());
    String baseName = "history";
    String name = baseName + "[tag:value,otherTag:otherValue]";
    Attributes otherAttributes = new Attributes().put("tag", "value").put("otherTag", "otherValue");

    long startTime = System.currentTimeMillis();
    long endTime = startTime + 11000;

    Count expectedCount = new Count("countery", 12.6, startTime, endTime, otherAttributes);
    Metric expectedSamplingResult = new Gauge("samplery", 77.1, endTime, otherAttributes);

    Collection<Metric> expectedMetrics = Sets.newSet(expectedCount, expectedSamplingResult);

    when(counter.transform(eq("history"), eq(histogram), notNull()))
        .thenReturn(singleton(expectedCount));
    when(sampler.transform(eq("history"), eq(histogram), notNull()))
        .thenReturn(singleton(expectedSamplingResult));

    HistogramTransformer testClass =
        new HistogramTransformer(
            counter,
            sampler,
            MetricCustomizerTestUtils.NAME_TAG_STRIPPER,
            MetricCustomizerTestUtils.ATTRIBUTES_FROM_TAGGED_NAME);
    Collection<Metric> result = testClass.transform(name, histogram);

    assertEquals(expectedMetrics, result);
  }

  @Test
  void testRemove() {
    CountingTransformer counting = mock(CountingTransformer.class);
    HistogramTransformer testClass = new HistogramTransformer(counting, null);
    testClass.onHistogramRemoved("jim");
    verify(counting).remove("jim");
  }

  @Test
  void testRemoveWithNameCustomization() {
    CountingTransformer counting = mock(CountingTransformer.class);
    HistogramTransformer testClass =
        new HistogramTransformer(
            counting,
            null,
            MetricCustomizerTestUtils.NAME_TAG_STRIPPER,
            MetricAttributesCustomizer.DEFAULT);
    testClass.onHistogramRemoved("jim[tag:value]");
    verify(counting).remove("jim");
  }
}
