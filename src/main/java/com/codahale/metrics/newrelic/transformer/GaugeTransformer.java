/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.codahale.metrics.newrelic.transformer;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Gauge;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.metrics.Metric;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GaugeTransformer implements DropWizardMetricTransformer<Gauge> {

  private static final Logger LOG = LoggerFactory.getLogger(GaugeTransformer.class);
  private final Clock clock;

  public GaugeTransformer() {
    this(Clock.defaultClock());
  }

  // exists for testing
  public GaugeTransformer(Clock clock) {
    this.clock = clock;
  }

  @Override
  public Collection<Metric> transform(String name, Gauge gauge) {
    long timestamp = clock.getTime();
    Object gaugeValue = gauge.getValue();
    if (gaugeValue == null) {
      LOG.debug("Ignoring gauge with null value. Gauge name: {}", name);
      return emptySet();
    }
    if (gaugeValue instanceof Number) {
      Metric metric =
          new com.newrelic.telemetry.metrics.Gauge(
              name, ((Number) gaugeValue).doubleValue(), timestamp, new Attributes());
      return singleton(metric);
    }
    LOG.debug(
        "Ignoring gauge [name: {}] with value of type {} (non-numeric gauges are unsupported)",
        name,
        gaugeValue.getClass().getName());
    return emptySet();
  }
}
