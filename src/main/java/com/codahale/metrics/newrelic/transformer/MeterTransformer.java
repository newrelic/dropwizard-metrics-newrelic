/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.codahale.metrics.newrelic.transformer;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricAttribute;
import com.codahale.metrics.newrelic.transformer.interfaces.CountingTransformer;
import com.codahale.metrics.newrelic.transformer.interfaces.MeteredTransformer;
import com.codahale.metrics.newrelic.util.TimeTracker;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.metrics.Metric;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class MeterTransformer implements DropWizardMetricTransformer<Meter> {

  static final Supplier<Attributes> BASE_ATTRIBUTES = Attributes::new;

  private final MeteredTransformer meteredTransformer;
  private final CountingTransformer countingTransformer;

  public static MeterTransformer build(
      TimeTracker timeTracker,
      long rateFactor,
      Predicate<MetricAttribute> metricAttributePredicate) {
    return new MeterTransformer(
        new MeteredTransformer(rateFactor, metricAttributePredicate),
        new CountingTransformer(timeTracker));
  }

  MeterTransformer(MeteredTransformer meteredTransformer, CountingTransformer countingTransformer) {
    this.meteredTransformer = meteredTransformer;
    this.countingTransformer = countingTransformer;
  }

  @Override
  public Collection<Metric> transform(String name, Meter meter) {
    return concat(
            countingTransformer.transform(name, meter, BASE_ATTRIBUTES).stream(),
            meteredTransformer.transform(name, meter, BASE_ATTRIBUTES).stream())
        .collect(toSet());
  }

  @Override
  public void onMeterRemoved(String name) {
    countingTransformer.remove(name);
  }
}
