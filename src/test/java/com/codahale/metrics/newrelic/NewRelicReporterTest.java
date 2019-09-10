/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.codahale.metrics.newrelic;

import static java.util.Collections.singleton;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.newrelic.transformer.CounterTransformer;
import com.codahale.metrics.newrelic.transformer.GaugeTransformer;
import com.codahale.metrics.newrelic.transformer.HistogramTransformer;
import com.codahale.metrics.newrelic.transformer.MeterTransformer;
import com.codahale.metrics.newrelic.transformer.TimerTransformer;
import com.codahale.metrics.newrelic.util.TimeTracker;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.TelemetryClient;
import com.newrelic.telemetry.metrics.Gauge;
import com.newrelic.telemetry.metrics.Metric;
import com.newrelic.telemetry.metrics.MetricBatch;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NewRelicReporterTest {

  private Attributes commonAttributes;
  private TimeTracker timeTracker;
  private TelemetryClient sender;
  private GaugeTransformer gaugeTransformer;
  private HistogramTransformer histogramTransformer;
  private CounterTransformer counterTransformer;
  private MeterTransformer meterTransformer;
  private TimerTransformer timerTransformer;

  @BeforeEach
  void setup() {
    commonAttributes = new Attributes().put("name", "the best").put("foo", false);

    gaugeTransformer = mock(GaugeTransformer.class);
    histogramTransformer = mock(HistogramTransformer.class);
    counterTransformer = mock(CounterTransformer.class);
    meterTransformer = mock(MeterTransformer.class);
    timerTransformer = mock(TimerTransformer.class);
    sender = mock(TelemetryClient.class);
    timeTracker = mock(TimeTracker.class);
  }

  @Test
  void testReport() {

    InputTestData testData = InputTestData.build();
    ExpectedMetrics expected = ExpectedMetrics.build();

    MetricBatch expectedBatch =
        new MetricBatch(
            expected.toList(),
            new Attributes()
                .put("name", "the best")
                .put("foo", false)
                .put("instrumentation.provider", "dropwizard")
                .put("collector.name", "dropwizard-metrics-newrelic")
                .put("collector.version", "Unknown Version"));

    when(gaugeTransformer.transform("gauge", testData.gauge)).thenReturn(singleton(expected.gauge));
    when(histogramTransformer.transform("histogram", testData.histogram))
        .thenReturn(singleton(expected.histogram));
    when(counterTransformer.transform("counter", testData.counter))
        .thenReturn(singleton(expected.counter));
    when(meterTransformer.transform("meter", testData.meter)).thenReturn(singleton(expected.meter));
    when(timerTransformer.transform("timer", testData.timer)).thenReturn(singleton(expected.timer));

    NewRelicReporter testClass =
        new NewRelicReporter(
            timeTracker,
            null,
            "reporter",
            null,
            TimeUnit.DAYS,
            TimeUnit.SECONDS,
            sender,
            commonAttributes,
            histogramTransformer,
            gaugeTransformer,
            counterTransformer,
            meterTransformer,
            timerTransformer);

    testClass.report(
        testData.gauges(),
        testData.counters(),
        testData.histograms(),
        testData.meters(),
        testData.timers());

    verify(sender).sendBatch(expectedBatch);
    verify(timeTracker).tick();
  }

  static class ExpectedMetrics {

    final Gauge gauge;
    final Gauge histogram;
    final Gauge counter;
    final Gauge meter;
    final Gauge timer;

    ExpectedMetrics(Gauge gauge, Gauge histogram, Gauge counter, Gauge meter, Gauge timer) {
      this.gauge = gauge;
      this.histogram = histogram;
      this.counter = counter;
      this.meter = meter;
      this.timer = timer;
    }

    Collection<Metric> toList() {
      return Arrays.asList(gauge, histogram, counter, meter, timer);
    }

    static ExpectedMetrics build() {
      long now = System.currentTimeMillis();
      return new ExpectedMetrics(
          new Gauge("gauge", 1.0, now, new Attributes()),
          new Gauge("histogram", 2.0, now, new Attributes()),
          new Gauge("counter", 3.0, now, new Attributes()),
          new Gauge("meter", 4.0, now, new Attributes()),
          new Gauge("timer", 5.0, now, new Attributes()));
    }
  }

  @Test
  void testStartRegistersListener() {
    MetricRegistry registry = mock(MetricRegistry.class);

    NewRelicReporter testClass =
        new NewRelicReporter(
            null,
            registry,
            null,
            null,
            TimeUnit.SECONDS,
            TimeUnit.MILLISECONDS,
            null,
            null,
            histogramTransformer,
            gaugeTransformer,
            counterTransformer,
            meterTransformer,
            timerTransformer);
    testClass.start(100, TimeUnit.MILLISECONDS);
    verify(registry).addListener(histogramTransformer);
    verify(registry).addListener(gaugeTransformer);
    verify(registry).addListener(meterTransformer);
    verify(registry).addListener(timerTransformer);
    verify(registry).addListener(counterTransformer);
  }

  @Test
  void testStopRemovesListener() {
    MetricRegistry registry = mock(MetricRegistry.class);
    NewRelicReporter testClass =
        new NewRelicReporter(
            null,
            registry,
            null,
            null,
            TimeUnit.SECONDS,
            TimeUnit.MILLISECONDS,
            null,
            null,
            histogramTransformer,
            gaugeTransformer,
            counterTransformer,
            meterTransformer,
            timerTransformer);
    testClass.stop();
    verify(registry).removeListener(histogramTransformer);
    verify(registry).removeListener(gaugeTransformer);
    verify(registry).removeListener(meterTransformer);
    verify(registry).removeListener(timerTransformer);
    verify(registry).removeListener(counterTransformer);
  }
}
