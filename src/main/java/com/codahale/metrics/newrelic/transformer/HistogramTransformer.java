/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.codahale.metrics.newrelic.transformer;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.newrelic.transformer.interfaces.CountingTransformer;
import com.codahale.metrics.newrelic.transformer.interfaces.SamplingTransformer;
import com.codahale.metrics.newrelic.util.TimeTracker;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.metrics.Metric;
import java.util.Collection;
import java.util.function.Supplier;

public class HistogramTransformer implements DropWizardMetricTransformer<Histogram> {

  static final Supplier<Attributes> ATTRIBUTES_SUPPLIER = Attributes::new;

  private final CountingTransformer countingTransformer;
  private final SamplingTransformer samplingTransformer;

  public static HistogramTransformer build(TimeTracker timeTracker) {
    return new HistogramTransformer(
        new CountingTransformer(timeTracker), new SamplingTransformer(timeTracker, 1L));
  }

  HistogramTransformer(
      CountingTransformer countingTransformer, SamplingTransformer samplingTransformer) {
    this.countingTransformer = countingTransformer;
    this.samplingTransformer = samplingTransformer;
  }

  @Override
  public Collection<Metric> transform(String name, Histogram histogram) {
    Collection<Metric> counts = countingTransformer.transform(name, histogram, ATTRIBUTES_SUPPLIER);

    return concat(
            counts.stream(),
            samplingTransformer.transform(name, histogram, ATTRIBUTES_SUPPLIER).stream())
        .collect(toSet());
  }

  @Override
  public void onHistogramRemoved(String name) {
    countingTransformer.remove(name);
  }
}
