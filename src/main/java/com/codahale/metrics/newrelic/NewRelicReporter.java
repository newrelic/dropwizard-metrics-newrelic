/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.codahale.metrics.newrelic;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricAttribute;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import com.codahale.metrics.newrelic.transformer.CounterTransformer;
import com.codahale.metrics.newrelic.transformer.DropWizardMetricTransformer;
import com.codahale.metrics.newrelic.transformer.GaugeTransformer;
import com.codahale.metrics.newrelic.transformer.HistogramTransformer;
import com.codahale.metrics.newrelic.transformer.MeterTransformer;
import com.codahale.metrics.newrelic.transformer.TimerTransformer;
import com.codahale.metrics.newrelic.util.TimeTracker;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.TelemetryClient;
import com.newrelic.telemetry.metrics.Metric;
import com.newrelic.telemetry.metrics.MetricBatch;
import com.newrelic.telemetry.metrics.MetricBatchSender;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewRelicReporter extends ScheduledReporter {

  private static final Logger LOG = LoggerFactory.getLogger(NewRelicReporter.class);
  private static final String implementationVersion;

  private final TimeTracker timeTracker;
  private final TelemetryClient sender;
  private final Attributes commonAttributes;
  private final HistogramTransformer histogramTransformer;
  private final GaugeTransformer gaugeTransformer;
  private final CounterTransformer counterTransformer;
  private final MeterTransformer meterTransformer;
  private final TimerTransformer timerTransformer;
  private final MetricRegistry registry;

  static {
    Package thisPackage = NewRelicReporter.class.getPackage();
    implementationVersion =
        Optional.ofNullable(thisPackage.getImplementationVersion()).orElse("Unknown Version");
  }

  NewRelicReporter(
      TimeTracker timeTracker,
      MetricRegistry registry,
      String name,
      MetricFilter filter,
      TimeUnit rateUnit,
      TimeUnit durationUnit,
      TelemetryClient sender,
      Attributes commonAttributes,
      HistogramTransformer histogramTransformer,
      GaugeTransformer gaugeTransformer,
      CounterTransformer counterTransformer,
      MeterTransformer meterTransformer,
      TimerTransformer timerTransformer,
      Set<MetricAttribute> disabledMetricAttributes) {
    super(registry, name, filter, rateUnit, durationUnit, null, true, disabledMetricAttributes);
    this.registry = registry;
    this.timeTracker = timeTracker;
    this.sender = sender;

    this.commonAttributes = commonAttributes == null ? new Attributes() : commonAttributes.copy();
    this.commonAttributes.put("instrumentation.provider", "dropwizard");
    this.commonAttributes.put("collector.name", "dropwizard-metrics-newrelic");

    this.histogramTransformer = histogramTransformer;
    this.gaugeTransformer = gaugeTransformer;
    this.counterTransformer = counterTransformer;
    this.meterTransformer = meterTransformer;
    this.timerTransformer = timerTransformer;
  }

  @Override
  public synchronized void start(long initialDelay, long period, TimeUnit unit) {
    allMetricTransformers().forEach(registry::addListener);
    LOG.info("New Relic Reporter: Version " + implementationVersion + " is starting");
    super.start(initialDelay, period, unit);
  }

  @Override
  public void stop() {
    allMetricTransformers().forEach(registry::removeListener);
    super.stop();
  }

  private Stream<DropWizardMetricTransformer<? extends com.codahale.metrics.Metric>>
      allMetricTransformers() {
    return Stream.of(
        histogramTransformer,
        meterTransformer,
        timerTransformer,
        gaugeTransformer,
        counterTransformer);
  }

  @Override
  public void report(
      SortedMap<String, Gauge> gauges,
      SortedMap<String, Counter> counters,
      SortedMap<String, Histogram> histograms,
      SortedMap<String, Meter> meters,
      SortedMap<String, Timer> timers) {

    List<Metric> metrics =
        Stream.of(
                transform(gauges, gaugeTransformer::transform),
                transform(histograms, histogramTransformer::transform),
                transform(counters, counterTransformer::transform),
                transform(meters, meterTransformer::transform),
                transform(timers, timerTransformer::transform))
            .flatMap(identity())
            .collect(toList());

    sender.sendBatch(new MetricBatch(metrics, commonAttributes));
    // set the previous harvest time in the tracker.
    timeTracker.tick();
  }

  private <T> Stream<Metric> transform(
      Map<String, T> metrics, BiFunction<String, T, Collection<Metric>> supplier) {
    return metrics
        .entrySet()
        .stream()
        .flatMap(entry -> supplier.apply(entry.getKey(), entry.getValue()).stream());
  }

  public static NewRelicReporterBuilder build(MetricRegistry registry, MetricBatchSender sender) {
    return NewRelicReporterBuilder.forRegistry(registry, sender);
  }
}
