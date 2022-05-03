/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.codahale.metrics.newrelic.transformer;

import static java.util.stream.Collectors.toSet;

import com.codahale.metrics.MetricAttribute;
import com.codahale.metrics.Timer;
import com.codahale.metrics.newrelic.transformer.customizer.MetricAttributesCustomizer;
import com.codahale.metrics.newrelic.transformer.customizer.MetricNameCustomizer;
import com.codahale.metrics.newrelic.transformer.interfaces.CountingTransformer;
import com.codahale.metrics.newrelic.transformer.interfaces.MeteredTransformer;
import com.codahale.metrics.newrelic.transformer.interfaces.SamplingTransformer;
import com.codahale.metrics.newrelic.util.TimeTracker;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.metrics.Metric;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class TimerTransformer implements DropWizardMetricTransformer<Timer> {

  static final Supplier<Attributes> ATTRIBUTES_SUPPLIER = Attributes::new;

  private final SamplingTransformer samplingTransformer;
  private final MeteredTransformer meteredTransformer;
  private final CountingTransformer countingTransformer;
  private final MetricNameCustomizer nameCustomizer;
  private final MetricAttributesCustomizer attributeCustomizer;

  public static TimerTransformer build(
      TimeTracker timeTracker,
      long rateFactor,
      double scaleFactor,
      Predicate<MetricAttribute> metricAttributePredicate,
      MetricNameCustomizer nameCustomizer,
      MetricAttributesCustomizer attributeCustomizer) {
    return new TimerTransformer(
        new SamplingTransformer(timeTracker, scaleFactor),
        new MeteredTransformer(rateFactor, metricAttributePredicate),
        new CountingTransformer(timeTracker),
        nameCustomizer,
        attributeCustomizer);
  }

  TimerTransformer(
      SamplingTransformer samplingTransformer,
      MeteredTransformer meteredTransformer,
      CountingTransformer countingTransformer) {
    this(
        samplingTransformer,
        meteredTransformer,
        countingTransformer,
        MetricNameCustomizer.DEFAULT,
        MetricAttributesCustomizer.DEFAULT);
  }

  TimerTransformer(
      SamplingTransformer samplingTransformer,
      MeteredTransformer meteredTransformer,
      CountingTransformer countingTransformer,
      MetricNameCustomizer nameCustomizer,
      MetricAttributesCustomizer attributeCustomizer) {
    this.samplingTransformer = samplingTransformer;
    this.meteredTransformer = meteredTransformer;
    this.countingTransformer = countingTransformer;
    this.nameCustomizer = nameCustomizer;
    this.attributeCustomizer = attributeCustomizer;
  }

  @Override
  public Collection<Metric> transform(String name, Timer timer) {
    String customizedName = nameCustomizer.customizeMetricName(name);
    Supplier<Attributes> customizedAttributeSupplier =
        () -> attributeCustomizer.customizeMetricAttributes(name, timer, ATTRIBUTES_SUPPLIER.get());
    return Stream.of(
            samplingTransformer.transform(customizedName, timer, customizedAttributeSupplier),
            meteredTransformer.transform(customizedName, timer, customizedAttributeSupplier),
            countingTransformer.transform(customizedName, timer, customizedAttributeSupplier))
        .flatMap(Collection::stream)
        .collect(toSet());
  }

  @Override
  public void onTimerRemoved(String name) {
    countingTransformer.remove(nameCustomizer.customizeMetricName(name));
  }
}
