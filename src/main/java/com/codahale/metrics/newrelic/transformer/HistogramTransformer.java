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
import com.codahale.metrics.newrelic.transformer.customizer.MetricAttributesCustomizer;
import com.codahale.metrics.newrelic.transformer.customizer.MetricNameCustomizer;
import com.codahale.metrics.newrelic.transformer.interfaces.CountingTransformer;
import com.codahale.metrics.newrelic.transformer.interfaces.SamplingTransformer;
import com.codahale.metrics.newrelic.util.TimeTracker;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.metrics.Metric;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;

public class HistogramTransformer implements DropWizardMetricTransformer<Histogram> {

  static final Supplier<Attributes> ATTRIBUTES_SUPPLIER = Attributes::new;

  private final CountingTransformer countingTransformer;
  private final SamplingTransformer samplingTransformer;
  private final MetricNameCustomizer nameCustomizer;
  private final MetricAttributesCustomizer attributeCustomizer;

  public static HistogramTransformer build(TimeTracker timeTracker) {
    return build(timeTracker, MetricNameCustomizer.DEFAULT, MetricAttributesCustomizer.DEFAULT);
  }

  public static HistogramTransformer build(
      TimeTracker timeTracker,
      MetricNameCustomizer nameCustomizer,
      MetricAttributesCustomizer attributeCustomizer) {
    return new HistogramTransformer(
        new CountingTransformer(timeTracker),
        new SamplingTransformer(timeTracker, 1L),
        nameCustomizer,
        attributeCustomizer);
  }

  HistogramTransformer(
      CountingTransformer countingTransformer,
      SamplingTransformer samplingTransformer,
      MetricNameCustomizer nameCustomizer,
      MetricAttributesCustomizer attributeCustomizer) {
    this.countingTransformer = countingTransformer;
    this.samplingTransformer = samplingTransformer;
    this.nameCustomizer = Objects.requireNonNull(nameCustomizer);
    this.attributeCustomizer = Objects.requireNonNull(attributeCustomizer);
  }

  HistogramTransformer(
      CountingTransformer countingTransformer, SamplingTransformer samplingTransformer) {
    this(
        countingTransformer,
        samplingTransformer,
        MetricNameCustomizer.DEFAULT,
        MetricAttributesCustomizer.DEFAULT);
  }

  @Override
  public Collection<Metric> transform(String name, Histogram histogram) {
    String customizedName = nameCustomizer.customizeMetricName(name);
    Supplier<Attributes> customizedAttributeSupplier =
        () ->
            attributeCustomizer.customizeMetricAttributes(
                name, histogram, ATTRIBUTES_SUPPLIER.get());

    Collection<Metric> counts =
        countingTransformer.transform(customizedName, histogram, customizedAttributeSupplier);

    return concat(
            counts.stream(),
            samplingTransformer
                .transform(customizedName, histogram, customizedAttributeSupplier)
                .stream())
        .collect(toSet());
  }

  @Override
  public void onHistogramRemoved(String name) {
    countingTransformer.remove(nameCustomizer.customizeMetricName(name));
  }
}
