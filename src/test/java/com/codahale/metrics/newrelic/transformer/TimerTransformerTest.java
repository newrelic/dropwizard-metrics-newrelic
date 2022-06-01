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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codahale.metrics.Timer;
import com.codahale.metrics.newrelic.transformer.customizer.MetricCustomizerTestUtils;
import com.codahale.metrics.newrelic.transformer.interfaces.CountingTransformer;
import com.codahale.metrics.newrelic.transformer.interfaces.MeteredTransformer;
import com.codahale.metrics.newrelic.transformer.interfaces.SamplingTransformer;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.metrics.Count;
import com.newrelic.telemetry.metrics.Gauge;
import com.newrelic.telemetry.metrics.Metric;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.collections.Sets;

class TimerTransformerTest {

  public static final String baseName = "fast";

  @Test
  void testTransform() {
    long now = System.currentTimeMillis();
    Gauge result1 = new Gauge(baseName, 55d, now, new Attributes());
    Gauge result2 = new Gauge(baseName, 999d, now, new Attributes());
    Count expectedCount = createExpectedCountMetric(new Attributes());

    Timer timer = mock(Timer.class);
    SamplingTransformer samplingTransformer = mock(SamplingTransformer.class);
    MeteredTransformer meteredTransformer = mock(MeteredTransformer.class);
    CountingTransformer countingTransformer = mock(CountingTransformer.class);

    when(samplingTransformer.transform(eq(baseName), eq(timer), notNull()))
        .thenReturn(singleton(result1));
    when(meteredTransformer.transform(eq(baseName), eq(timer), notNull()))
        .thenReturn(singleton(result2));
    when(countingTransformer.transform(eq(baseName), eq(timer), notNull()))
        .thenReturn(singleton(expectedCount));

    TimerTransformer timerTransformer =
        new TimerTransformer(samplingTransformer, meteredTransformer, countingTransformer);

    Collection<Metric> results = timerTransformer.transform(baseName, timer);
    Collection<Metric> expected = Sets.newSet(result1, result2, expectedCount);
    assertEquals(expected, results);
  }

  @Test
  void testTransformWithAttributeAndNameCustomization() {
    Attributes otherTags = new Attributes().put("tag", "value").put("otherTag", "otherValue");
    long now = System.currentTimeMillis();
    Gauge result1 = new Gauge(baseName, 55d, now, otherTags);
    Gauge result2 = new Gauge(baseName, 999d, now, otherTags);
    Count expectedCount = createExpectedCountMetric(otherTags);

    Timer timer = mock(Timer.class);
    SamplingTransformer samplingTransformer = mock(SamplingTransformer.class);
    MeteredTransformer meteredTransformer = mock(MeteredTransformer.class);
    CountingTransformer countingTransformer = mock(CountingTransformer.class);

    when(samplingTransformer.transform(eq(baseName), eq(timer), notNull()))
        .thenReturn(singleton(result1));
    when(meteredTransformer.transform(eq(baseName), eq(timer), notNull()))
        .thenReturn(singleton(result2));
    when(countingTransformer.transform(eq(baseName), eq(timer), notNull()))
        .thenReturn(singleton(expectedCount));

    TimerTransformer timerTransformer =
        new TimerTransformer(
            samplingTransformer,
            meteredTransformer,
            countingTransformer,
            MetricCustomizerTestUtils.NAME_TAG_STRIPPER,
            MetricCustomizerTestUtils.ATTRIBUTES_FROM_TAGGED_NAME);

    Collection<Metric> results =
        timerTransformer.transform(baseName + "[tag:value,otherTag:otherValue]", timer);
    Collection<Metric> expected = Sets.newSet(result1, result2, expectedCount);

    assertEquals(expected, results);
  }

  @Test
  void testRemove() throws Exception {
    CountingTransformer counting = mock(CountingTransformer.class);
    TimerTransformer testClass = new TimerTransformer(null, null, counting);
    testClass.onTimerRemoved("money");
    verify(counting).remove("money");
  }

  @Test
  void testRemoveWithNameCustomization() {
    CountingTransformer counting = mock(CountingTransformer.class);
    TimerTransformer testClass =
        new TimerTransformer(
            null, null, counting, MetricCustomizerTestUtils.NAME_TAG_STRIPPER, null);
    testClass.onTimerRemoved("money[tag:value]");
    verify(counting).remove("money");
  }

  @NotNull
  private Count createExpectedCountMetric(Attributes attributes) {
    return new Count(
        baseName,
        99999.6666,
        System.currentTimeMillis(),
        System.currentTimeMillis() + 100,
        attributes);
  }
}
