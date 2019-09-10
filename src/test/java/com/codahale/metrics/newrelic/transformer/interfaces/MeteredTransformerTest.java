/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.codahale.metrics.newrelic.transformer.interfaces;

import static com.codahale.metrics.MetricAttribute.M15_RATE;
import static com.codahale.metrics.MetricAttribute.M1_RATE;
import static com.codahale.metrics.MetricAttribute.M5_RATE;
import static com.codahale.metrics.MetricAttribute.MEAN_RATE;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricAttribute;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.metrics.Gauge;
import com.newrelic.telemetry.metrics.Metric;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MeteredTransformerTest {

  private static final long timestamp = System.currentTimeMillis();
  private static final String METER_NAME = "Meatier";

  private final Gauge mean =
      new Gauge(METER_NAME + ".rates", 1000, timestamp, attributeTo(MEAN_RATE));
  private final Gauge oneMinuteRate =
      new Gauge(METER_NAME + ".rates", 2000, timestamp, attributeTo(M1_RATE));
  private final Gauge fiveMinuteRate =
      new Gauge(METER_NAME + ".rates", 3000, timestamp, attributeTo(M5_RATE));
  private final Gauge fifteenMinuteRate =
      new Gauge(METER_NAME + ".rates", 4000, timestamp, attributeTo(M15_RATE));
  private final Supplier<Attributes> baseAttributes = Attributes::new;
  private Clock clock;
  private Meter meter;

  @BeforeEach
  void setUp() {
    clock = mock(Clock.class);
    meter = mock(Meter.class);
    when(clock.getTime()).thenReturn(timestamp);
    when(meter.getCount()).thenReturn(Long.valueOf(10));
    when(meter.getMeanRate()).thenReturn(100d);
    when(meter.getOneMinuteRate()).thenReturn(200d);
    when(meter.getFiveMinuteRate()).thenReturn(300d);
    when(meter.getFifteenMinuteRate()).thenReturn(400d);
  }

  @Test
  void testMeter() {
    // Given
    meter.mark();
    meter.mark(10);

    Collection<Metric> expectedMetrics =
        Stream.of(mean, oneMinuteRate, fiveMinuteRate, fifteenMinuteRate).collect(toSet());

    MeteredTransformer converter = new MeteredTransformer(clock, 10L, x -> true);

    // When
    Collection<Metric> newRelicMetrics = converter.transform(METER_NAME, meter, baseAttributes);

    // Then
    assertEquals(expectedMetrics, newRelicMetrics);
  }

  @Test
  void testDisabledMetricAttributes() {
    // Given
    meter.mark();
    meter.mark(10);

    Collection<Metric> expectedMetrics = Stream.of(fifteenMinuteRate).collect(toSet());

    // Only want the one M15_RATE
    MeteredTransformer converter = new MeteredTransformer(clock, 10L, M15_RATE::equals);

    // When
    Collection<Metric> newRelicMetrics = converter.transform(METER_NAME, meter, baseAttributes);

    // Then
    assertEquals(expectedMetrics, newRelicMetrics);
  }

  private Attributes attributeTo(MetricAttribute attribute) {
    return new Attributes().put("rate", attribute.getCode());
  }
}
