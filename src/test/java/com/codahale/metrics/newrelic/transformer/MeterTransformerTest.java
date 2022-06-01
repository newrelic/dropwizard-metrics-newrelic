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

import com.codahale.metrics.Meter;
import com.codahale.metrics.newrelic.transformer.customizer.MetricCustomizerTestUtils;
import com.codahale.metrics.newrelic.transformer.interfaces.CountingTransformer;
import com.codahale.metrics.newrelic.transformer.interfaces.MeteredTransformer;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.metrics.Count;
import com.newrelic.telemetry.metrics.Gauge;
import com.newrelic.telemetry.metrics.Metric;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.collections.Sets;

class MeterTransformerTest {

  private static final String baseName = "$$";
  private final MeteredTransformer converter = mock(MeteredTransformer.class);
  private final CountingTransformer countingTransformer = mock(CountingTransformer.class);

  @BeforeEach
  public void setup() {
    reset(converter);
    reset(countingTransformer);
  }

  @Test
  void testTransform() {
    Meter meter = new Meter();
    Metric expectedMetric = createExpectedGaugeMetric(new Attributes());
    Count expectedCount = createExpectedCountMetric(new Attributes());

    Collection<Metric> expectedMeters = Sets.newSet(expectedMetric, expectedCount);

    when(converter.transform(eq(baseName), eq(meter), notNull())).thenReturn(expectedMeters);
    when(countingTransformer.transform(eq(baseName), eq(meter), notNull()))
        .thenReturn(singleton(expectedCount));

    MeterTransformer testClass = new MeterTransformer(converter, countingTransformer);

    Collection<Metric> result = testClass.transform(baseName, meter);

    assertEquals(expectedMeters, result);
  }

  @Test
  void testTransformWithAttributeAndNameCustomization() {
    Attributes otherAttributes = new Attributes().put("tag", "value").put("otherTag", "otherValue");
    Meter meter = new Meter();
    Metric expectedMetric = createExpectedGaugeMetric(otherAttributes);
    Count expectedCount = createExpectedCountMetric(otherAttributes);

    Collection<Metric> expectedMeters = Sets.newSet(expectedMetric, expectedCount);

    when(converter.transform(eq(baseName), eq(meter), notNull())).thenReturn(expectedMeters);
    when(countingTransformer.transform(eq(baseName), eq(meter), notNull()))
        .thenReturn(singleton(expectedCount));

    MeterTransformer testClass =
        new MeterTransformer(
            converter,
            countingTransformer,
            MetricCustomizerTestUtils.NAME_TAG_STRIPPER,
            MetricCustomizerTestUtils.ATTRIBUTES_FROM_TAGGED_NAME);

    Collection<Metric> result =
        testClass.transform(baseName + "[tag:value,otherTag:otherValue]", meter);

    assertEquals(expectedMeters, result);
  }

  @Test
  void testRemove() {
    CountingTransformer counting = mock(CountingTransformer.class);
    MeterTransformer testClass = new MeterTransformer(null, counting);
    testClass.onMeterRemoved("something");
    verify(counting).remove("something");
  }

  @Test
  void testRemoveWithNameCustomization() {
    CountingTransformer counting = mock(CountingTransformer.class);
    MeterTransformer testClass =
        new MeterTransformer(null, counting, MetricCustomizerTestUtils.NAME_TAG_STRIPPER, null);
    testClass.onMeterRemoved("something[tag:value]");
    verify(counting).remove("something");
  }

  private Metric createExpectedGaugeMetric(Attributes optionalAttributes) {
    return new Gauge(
        baseName,
        85299999.21,
        System.currentTimeMillis(),
        new Attributes().put("sourceType", "meter").putAll(optionalAttributes));
  }

  private Count createExpectedCountMetric(Attributes optionalAttributes) {
    return new Count(
        baseName,
        14.5,
        System.currentTimeMillis(),
        System.currentTimeMillis() + 12,
        optionalAttributes.put("something", "else"));
  }
}
