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
import com.newrelic.telemetry.Gauge;
import com.newrelic.telemetry.Metric;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MeteredTransformerTest {

  private static final long timestamp = System.currentTimeMillis();
  private Clock clock;
  private static final String METER_NAME = "Meatier";

  @BeforeEach
  void setUp() {
    clock = mock(Clock.class);
    when(clock.getTime()).thenReturn(timestamp);
  }

  @Test
  void testMeter() {
    // Given
    Supplier<Attributes> baseAttributes = Attributes::new;
    Meter meter = mock(Meter.class);
    when(meter.getCount()).thenReturn(Long.valueOf(10));
    when(meter.getMeanRate()).thenReturn(100d);
    when(meter.getOneMinuteRate()).thenReturn(200d);
    when(meter.getFiveMinuteRate()).thenReturn(300d);
    when(meter.getFifteenMinuteRate()).thenReturn(400d);

    meter.mark();
    meter.mark(10);

    Gauge mean = new Gauge(METER_NAME, 1000, timestamp, attributeTo(MEAN_RATE));
    Gauge oneMinuteRate = new Gauge(METER_NAME, 2000, timestamp, attributeTo(M1_RATE));
    Gauge fiveMinuteRate = new Gauge(METER_NAME, 3000, timestamp, attributeTo(M5_RATE));
    Gauge fifteenMinuteRate = new Gauge(METER_NAME, 4000, timestamp, attributeTo(M15_RATE));

    Collection<Metric> expectedMetrics =
        Stream.of(mean, oneMinuteRate, fiveMinuteRate, fifteenMinuteRate).collect(toSet());

    MeteredTransformer converter = new MeteredTransformer(clock, 10L);

    // When
    Collection<Metric> newRelicMetrics = converter.transform(METER_NAME, meter, baseAttributes);

    // Then
    assertEquals(expectedMetrics, newRelicMetrics);
  }

  private Attributes attributeTo(MetricAttribute attribute) {
    return new Attributes().put("rate", attribute.getCode()).put("groupingAs", "rates");
  }
}
