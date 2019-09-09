/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.codahale.metrics.newrelic.transformer;

import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.RatioGauge;
import com.codahale.metrics.RatioGauge.Ratio;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.metrics.Metric;
import java.math.BigDecimal;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GaugeTransformerTest {

  private static final long timestamp = System.currentTimeMillis();
  private Clock clock;
  private static final String GAUGE_NAME = "gaugalicious";

  @BeforeEach
  void setUp() {
    clock = mock(Clock.class);
    when(clock.getTime()).thenReturn(timestamp);
  }

  @Test
  void testDoubleGauge() {
    // Given
    double value = 12345d;
    Gauge<Double> wizardGauge = () -> value;
    Metric expectedMetric =
        new com.newrelic.telemetry.metrics.Gauge(GAUGE_NAME, value, timestamp, new Attributes());

    GaugeTransformer converter = new GaugeTransformer(clock);

    // When
    Collection<Metric> result = converter.transform(GAUGE_NAME, wizardGauge);

    // Then
    assertEquals(singleton(expectedMetric), result);
  }

  @Test
  void testRatioGauge() {
    // Given
    Ratio ratio = Ratio.of(37, 31);
    RatioGauge wizardGauge =
        new RatioGauge() {
          @Override
          protected Ratio getRatio() {
            return ratio;
          }
        };
    Metric expectedMetric =
        new com.newrelic.telemetry.metrics.Gauge(
            GAUGE_NAME, ratio.getValue(), timestamp, new Attributes());

    GaugeTransformer converter = new GaugeTransformer(clock);

    // When
    Collection<Metric> result = converter.transform(GAUGE_NAME, wizardGauge);

    // Then
    assertEquals(singleton(expectedMetric), result);
  }

  @Test
  void testGaugeOfString() {
    // Given
    Gauge<String> wizardGauge = () -> "amanaplanacanalpanama";
    GaugeTransformer converter = new GaugeTransformer(clock);

    // When
    Collection<Metric> newRelicMetric = converter.transform(GAUGE_NAME, wizardGauge);

    // Then
    assertTrue(newRelicMetric.isEmpty());
  }

  @Test
  void testNullValue() {
    // Given
    Gauge<String> wizardGauge = () -> null;
    GaugeTransformer converter = new GaugeTransformer(clock);

    // When
    Collection<Metric> newRelicMetric = converter.transform(GAUGE_NAME, wizardGauge);

    // Then
    assertTrue(newRelicMetric.isEmpty());
  }

  @Test
  void testOtherNumericValue() {
    // Given
    BigDecimal value = new BigDecimal(12345d);
    Gauge<BigDecimal> wizardGauge = () -> value;
    Metric expectedMetric =
        new com.newrelic.telemetry.metrics.Gauge(
            GAUGE_NAME, value.doubleValue(), timestamp, new Attributes());

    GaugeTransformer converter = new GaugeTransformer(clock);

    // When
    Collection<Metric> result = converter.transform(GAUGE_NAME, wizardGauge);

    // Then
    assertEquals(singleton(expectedMetric), result);
  }
}
