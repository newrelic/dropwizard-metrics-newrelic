/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.codahale.metrics.newrelic.transformer;

import static java.util.Collections.singleton;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Counter;
import com.codahale.metrics.newrelic.transformer.customizer.MetricAttributesCustomizer;
import com.codahale.metrics.newrelic.transformer.customizer.MetricNameCustomizer;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.metrics.Gauge;
import com.newrelic.telemetry.metrics.Metric;
import java.util.Collection;
import java.util.Objects;

public class CounterTransformer implements DropWizardMetricTransformer<Counter> {

  private final Clock clock;
  private final MetricNameCustomizer nameCustomizer;
  private final MetricAttributesCustomizer attributeCustomizer;

  public CounterTransformer() {
    this(Clock.defaultClock());
  }

  public CounterTransformer(
      MetricNameCustomizer nameCustomizer, MetricAttributesCustomizer attributeCustomizer) {
    this(Clock.defaultClock(), nameCustomizer, attributeCustomizer);
  }

  // exists for testing
  CounterTransformer(Clock clock) {
    this(clock, MetricNameCustomizer.DEFAULT, MetricAttributesCustomizer.DEFAULT);
  }

  CounterTransformer(
      Clock clock,
      MetricNameCustomizer nameCustomizer,
      MetricAttributesCustomizer attributeCustomizer) {
    this.clock = clock;
    this.nameCustomizer = Objects.requireNonNull(nameCustomizer);
    this.attributeCustomizer = Objects.requireNonNull(attributeCustomizer);
  }

  @Override
  public Collection<Metric> transform(String name, Counter counter) {
    String customizedName = nameCustomizer.customizeMetricName(name);
    Attributes customizedAttributes =
        attributeCustomizer.customizeMetricAttributes(name, counter, new Attributes());
    return singleton(
        new Gauge(customizedName, counter.getCount(), clock.getTime(), customizedAttributes));
  }
}
