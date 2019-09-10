/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.codahale.metrics.newrelic.transformer.interfaces;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;

import com.codahale.metrics.Sampling;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.newrelic.util.TimeTracker;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.metrics.Gauge;
import com.newrelic.telemetry.metrics.Metric;
import com.newrelic.telemetry.metrics.Summary;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class SamplingTransformer implements DropWizardComponentTransformer<Sampling> {

  private final TimeTracker timeTracker;
  private final double scaleFactor;

  public SamplingTransformer(TimeTracker timeTracker, double scaleFactor) {
    this.timeTracker = timeTracker;
    this.scaleFactor = scaleFactor;
  }

  @Override
  public Collection<Metric> transform(
      String name, Sampling sampling, Supplier<Attributes> baseAttributes) {
    Snapshot snapshot = sampling.getSnapshot();

    long now = timeTracker.getCurrentTime();
    long previousTime = timeTracker.getPreviousTime();
    Summary summary =
        new Summary(
            name,
            snapshot.size(),
            calculateSum(snapshot),
            scaleLongValue(snapshot.getMin()),
            scaleLongValue(snapshot.getMax()),
            previousTime,
            now,
            baseAttributes.get());

    Gauge median =
        new Gauge(
            name + ".percentiles",
            scaleDoubleValue(snapshot.getMedian()),
            now,
            buildAttributes(baseAttributes.get(), .50).put("commonName", "median"));
    Stream<Gauge> gauges =
        Stream.of(0.75d, 0.95d, 0.98d, 0.99d, 0.999d)
            .map(
                percentile ->
                    new Gauge(
                        name + ".percentiles",
                        scaleDoubleValue(snapshot.getValue(percentile)),
                        now,
                        buildAttributes(baseAttributes.get(), percentile)));
    return concat(Stream.of(median, summary), gauges).collect(toSet());
  }

  private double scaleLongValue(long value) {
    return value / scaleFactor;
  }

  private double scaleDoubleValue(double value) {
    return value / scaleFactor;
  }

  private Attributes buildAttributes(Attributes baseAttributes, Double percentile) {
    return baseAttributes.put("percentile", percentile * 100d);
  }

  private double calculateSum(Snapshot snapshot) {
    return LongStream.of(snapshot.getValues()).asDoubleStream().sum();
  }
}
