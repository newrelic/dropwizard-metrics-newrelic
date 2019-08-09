/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.codahale.metrics.newrelic.transformer.interfaces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Sampling;
import com.codahale.metrics.SlidingWindowReservoir;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.UniformReservoir;
import com.codahale.metrics.newrelic.util.TimeTracker;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Gauge;
import com.newrelic.telemetry.Metric;
import com.newrelic.telemetry.Summary;
import java.util.Collection;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.collections.Sets;

class SamplingTransformerTest {

  private static final long now = System.currentTimeMillis();
  private Supplier<Attributes> baseAttributes;

  @BeforeEach
  void setUp() {
    baseAttributes = Attributes::new;
  }

  @Test
  void testBasicSampling() {
    Sampling sampling = buildSampling();

    Snapshot snapshot = sampling.getSnapshot();

    Collection<Metric> expected =
        Sets.newSet(
            new Summary("blobby", 6, 23, .3, .5, now, now, new Attributes()),
            new Gauge(
                "blobby",
                snapshot.getMedian() / 10d,
                now,
                percentileAttributes(50.0).put("commonName", "median")),
            new Gauge(
                "blobby", 0.1d * snapshot.get75thPercentile(), now, percentileAttributes(75.0)),
            new Gauge(
                "blobby", 0.1d * snapshot.get95thPercentile(), now, percentileAttributes(95.0)),
            new Gauge(
                "blobby", 0.1d * snapshot.get98thPercentile(), now, percentileAttributes(98.0)),
            new Gauge(
                "blobby", 0.1d * snapshot.get99thPercentile(), now, percentileAttributes(99.0)),
            new Gauge(
                "blobby", 0.1d * snapshot.get999thPercentile(), now, percentileAttributes(99.9)));

    Clock clock = mock(Clock.class);

    when(clock.getTime()).thenReturn(now);

    SamplingTransformer testClass = new SamplingTransformer(new TimeTracker(clock), 10);

    Collection<Metric> result = testClass.transform("blobby", sampling, baseAttributes);

    assertEquals(expected, result);
  }

  private Histogram buildSampling() {
    Histogram histogram = new Histogram(new UniformReservoir(10));

    recordSomeData(histogram);
    return histogram;
  }

  @Test
  void testMultipleVisitsGetYouSomeState() {
    Histogram histogram = new Histogram(new SlidingWindowReservoir(10));
    recordSomeData(histogram);

    long later = now + 5000;
    TimeTracker timeTracker = mock(TimeTracker.class);
    when(timeTracker.getCurrentTime()).thenReturn(later);
    when(timeTracker.getPreviousTime()).thenReturn(now);

    SamplingTransformer testClass = new SamplingTransformer(timeTracker, 0.10d);
    testClass.transform("hollerMonkey", histogram, baseAttributes);
    recordSomeData(histogram);
    Collection<Metric> result = testClass.transform("hollerMonkey", histogram, baseAttributes);

    Snapshot snapshot = histogram.getSnapshot();
    Collection<Metric> expected =
        Sets.newSet(
            new Summary("hollerMonkey", 10, 40, 3 * 10, 5 * 10, now, later, new Attributes()),
            new Gauge(
                "hollerMonkey",
                snapshot.getMedian() * 10,
                later,
                percentileAttributes(50.0).put("commonName", "median")),
            new Gauge(
                "hollerMonkey",
                snapshot.get75thPercentile() * 10,
                later,
                percentileAttributes(75.0)),
            new Gauge(
                "hollerMonkey",
                snapshot.get95thPercentile() * 10,
                later,
                percentileAttributes(95.0)),
            new Gauge(
                "hollerMonkey",
                snapshot.get98thPercentile() * 10,
                later,
                percentileAttributes(98.0)),
            new Gauge(
                "hollerMonkey",
                snapshot.get99thPercentile() * 10,
                later,
                percentileAttributes(99.0)),
            new Gauge(
                "hollerMonkey",
                snapshot.get999thPercentile() * 10,
                later,
                percentileAttributes(99.9)));
    assertEquals(expected, result);
  }

  private void recordSomeData(Histogram histogram) {
    histogram.update(3);
    histogram.update(3);
    histogram.update(5);
    histogram.update(4);
    histogram.update(3);
    histogram.update(5);
  }

  private Attributes percentileAttributes(double percentile) {
    return new Attributes().put("percentile", percentile).put("groupingAs", "percentiles");
  }
}
