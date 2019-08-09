/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.codahale.metrics.newrelic.transformer;

import static java.util.stream.Collectors.toSet;

import com.codahale.metrics.Timer;
import com.codahale.metrics.newrelic.transformer.interfaces.CountingTransformer;
import com.codahale.metrics.newrelic.transformer.interfaces.MeteredTransformer;
import com.codahale.metrics.newrelic.transformer.interfaces.SamplingTransformer;
import com.codahale.metrics.newrelic.util.TimeTracker;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Metric;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class TimerTransformer implements DropWizardMetricTransformer<Timer> {

  static final Supplier<Attributes> ATTRIBUTES_SUPPLIER =
      () -> new Attributes().put("sourceType", "timer");

  private final SamplingTransformer samplingTransformer;
  private final MeteredTransformer meteredTransformer;
  private final CountingTransformer countingTransformer;

  public static TimerTransformer build(
      TimeTracker timeTracker, long rateFactor, double scaleFactor) {
    return new TimerTransformer(
        new SamplingTransformer(timeTracker, scaleFactor),
        new MeteredTransformer(rateFactor),
        new CountingTransformer(timeTracker));
  }

  TimerTransformer(
      SamplingTransformer samplingTransformer,
      MeteredTransformer meteredTransformer,
      CountingTransformer countingTransformer) {
    this.samplingTransformer = samplingTransformer;
    this.meteredTransformer = meteredTransformer;
    this.countingTransformer = countingTransformer;
  }

  @Override
  public Collection<Metric> transform(String name, Timer timer) {
    return Stream.of(
            samplingTransformer.transform(name, timer, ATTRIBUTES_SUPPLIER),
            meteredTransformer.transform(name, timer, ATTRIBUTES_SUPPLIER),
            countingTransformer.transform(name, timer, ATTRIBUTES_SUPPLIER))
        .flatMap(Collection::stream)
        .collect(toSet());
  }

  @Override
  public void onTimerRemoved(String name) {
    countingTransformer.remove(name);
  }
}
