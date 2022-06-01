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
import com.codahale.metrics.newrelic.transformer.customizer.MetricAttributesCustomizer;
import com.codahale.metrics.newrelic.transformer.customizer.MetricNameCustomizer;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.metrics.Metric;
import java.util.Collection;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GaugeTransformer implements DropWizardMetricTransformer<Gauge> {

  private static final Logger LOG = LoggerFactory.getLogger(GaugeTransformer.class);
  private final Clock clock;
  private final MetricNameCustomizer nameCustomizer;
  private final MetricAttributesCustomizer attributeCustomizer;

  public GaugeTransformer() {
    this(MetricNameCustomizer.DEFAULT, MetricAttributesCustomizer.DEFAULT);
  }

  public GaugeTransformer(
      MetricNameCustomizer nameCustomizer, MetricAttributesCustomizer attributeCustomizer) {
    this(Clock.defaultClock(), nameCustomizer, attributeCustomizer);
  }

  // exists for testing
  public GaugeTransformer(Clock clock) {
    this(clock, MetricNameCustomizer.DEFAULT, MetricAttributesCustomizer.DEFAULT);
  }

  public GaugeTransformer(
      Clock clock,
      MetricNameCustomizer nameCustomizer,
      MetricAttributesCustomizer attributeCustomizer) {
    this.clock = clock;
    this.nameCustomizer = Objects.requireNonNull(nameCustomizer);
    this.attributeCustomizer = Objects.requireNonNull(attributeCustomizer);
  }

  @Override
  public Collection<Metric> transform(String name, Gauge gauge) {
    String customizedName = nameCustomizer.customizeMetricName(name);
    Attributes customizedAttributes =
        attributeCustomizer.customizeMetricAttributes(name, gauge, new Attributes());
    long timestamp = clock.getTime();
    Object gaugeValue = gauge.getValue();
    if (gaugeValue == null) {
      LOG.debug(
          "Ignoring gauge with null value. Gauge name: {}, Gauge attributes: {}",
          customizedName,
          customizedAttributes);
      return emptySet();
    }
    if (gaugeValue instanceof Number) {
      Metric metric =
          new com.newrelic.telemetry.metrics.Gauge(
              customizedName, ((Number) gaugeValue).doubleValue(), timestamp, customizedAttributes);
      return singleton(metric);
    }
    LOG.debug(
        "Ignoring gauge [name: {}, Attributes: {}] with value of type {} (non-numeric gauges are unsupported)",
        customizedName,
        customizedAttributes,
        gaugeValue.getClass().getName());
    return emptySet();
  }
}
