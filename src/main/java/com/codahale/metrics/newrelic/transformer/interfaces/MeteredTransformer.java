/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.codahale.metrics.newrelic.transformer.interfaces;

import static java.util.stream.Collectors.toSet;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Metered;
import com.codahale.metrics.MetricAttribute;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.metrics.Gauge;
import com.newrelic.telemetry.metrics.Metric;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class MeteredTransformer implements DropWizardComponentTransformer<Metered> {

  private final Clock clock;
  private final long rateFactor;
  private final Predicate<MetricAttribute> metricAttributePredicate;

  public MeteredTransformer(long rateFactor, Predicate<MetricAttribute> metricAttributePredicate) {
    this(Clock.defaultClock(), rateFactor, metricAttributePredicate);
  }

  // exists for testing
  MeteredTransformer(
      Clock clock, long rateFactor, Predicate<MetricAttribute> metricAttributePredicate) {
    this.clock = clock;
    this.rateFactor = rateFactor;
    this.metricAttributePredicate = metricAttributePredicate;
  }

  @Override
  public Collection<Metric> transform(
      String name, Metered metered, Supplier<Attributes> baseAttributes) {
    long timestamp = clock.getTime();

    String ratesName = name + ".rates";
    Gauge mean =
        makeGauge(
            ratesName,
            timestamp,
            convertRate(metered.getMeanRate()),
            MetricAttribute.MEAN_RATE,
            baseAttributes);
    Gauge oneMinuteRate =
        makeGauge(
            ratesName,
            timestamp,
            convertRate(metered.getOneMinuteRate()),
            MetricAttribute.M1_RATE,
            baseAttributes);
    Gauge fiveMinuteRate =
        makeGauge(
            ratesName,
            timestamp,
            convertRate(metered.getFiveMinuteRate()),
            MetricAttribute.M5_RATE,
            baseAttributes);
    Gauge fifteenMinuteRate =
        makeGauge(
            ratesName,
            timestamp,
            convertRate(metered.getFifteenMinuteRate()),
            MetricAttribute.M15_RATE,
            baseAttributes);

    return Stream.of(mean, oneMinuteRate, fiveMinuteRate, fifteenMinuteRate)
        .filter(Objects::nonNull)
        .collect(toSet());
  }

  private double convertRate(double meanRate) {
    return rateFactor * meanRate;
  }

  private Gauge makeGauge(
      String name,
      long timestamp,
      double count,
      MetricAttribute attribute,
      Supplier<Attributes> attributes) {
    if (!metricAttributePredicate.test(attribute)) {
      return null;
    }
    return new Gauge(name, count, timestamp, attributes.get().put("rate", attribute.getCode()));
  }
}
