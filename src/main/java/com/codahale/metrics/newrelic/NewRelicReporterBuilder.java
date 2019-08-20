/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.codahale.metrics.newrelic;

import com.codahale.metrics.Clock;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.newrelic.transformer.CounterTransformer;
import com.codahale.metrics.newrelic.transformer.GaugeTransformer;
import com.codahale.metrics.newrelic.transformer.HistogramTransformer;
import com.codahale.metrics.newrelic.transformer.MeterTransformer;
import com.codahale.metrics.newrelic.transformer.TimerTransformer;
import com.codahale.metrics.newrelic.util.TimeTracker;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.MetricBatchSender;
import com.newrelic.telemetry.TelemetryClient;
import java.util.concurrent.TimeUnit;

public class NewRelicReporterBuilder {

  private final MetricRegistry registry;
  private final MetricBatchSender metricBatchSender;
  private String name = "newRelicReporter";
  private MetricFilter filter = MetricFilter.ALL;
  private TimeUnit rateUnit = TimeUnit.SECONDS;
  private TimeUnit durationUnit = TimeUnit.MILLISECONDS;
  private Attributes commonAttributes = new Attributes();

  public static NewRelicReporterBuilder forRegistry(
      MetricRegistry registry, MetricBatchSender metricBatchSender) {
    return new NewRelicReporterBuilder(registry, metricBatchSender);
  }

  private NewRelicReporterBuilder(MetricRegistry registry, MetricBatchSender newRelicSender) {
    this.registry = registry;
    this.metricBatchSender = newRelicSender;
  }

  public NewRelicReporterBuilder name(String name) {
    this.name = name;
    return this;
  }

  public NewRelicReporterBuilder filter(MetricFilter filter) {
    this.filter = filter;
    return this;
  }

  public NewRelicReporterBuilder rateUnit(TimeUnit rateUnit) {
    this.rateUnit = rateUnit;
    return this;
  }

  public NewRelicReporterBuilder durationUnit(TimeUnit durationUnit) {
    this.durationUnit = durationUnit;
    return this;
  }

  public NewRelicReporterBuilder commonAttributes(Attributes commonAttributes) {
    this.commonAttributes = commonAttributes;
    return this;
  }

  public NewRelicReporter build() {
    long rateFactor = rateUnit.toSeconds(1);
    double durationFactor = durationUnit.toNanos(1);

    TimeTracker timeTracker = new TimeTracker(Clock.defaultClock());
    MeterTransformer meterTransformer = MeterTransformer.build(timeTracker, rateFactor);
    TimerTransformer timerTransformer =
        TimerTransformer.build(timeTracker, rateFactor, durationFactor);
    GaugeTransformer gaugeTransformer = new GaugeTransformer();
    CounterTransformer counterTransformer = new CounterTransformer();
    HistogramTransformer histogramTransformer = HistogramTransformer.build(timeTracker);

    return new NewRelicReporter(
        timeTracker,
        registry,
        name,
        filter,
        rateUnit,
        durationUnit,
        new TelemetryClient(metricBatchSender),
        commonAttributes,
        histogramTransformer,
        gaugeTransformer,
        counterTransformer,
        meterTransformer,
        timerTransformer);
  }
}
