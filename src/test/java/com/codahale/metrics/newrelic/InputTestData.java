/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.codahale.metrics.newrelic;

import com.codahale.metrics.Counter;
import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.SlidingWindowReservoir;
import com.codahale.metrics.Timer;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class InputTestData {

  final Gauge gauge;
  final Histogram histogram;
  final Counter counter;
  final Meter meter;
  final Timer timer;

  public InputTestData(
      Gauge gauge, Histogram histogram, Counter counter, Meter meter, Timer timer) {
    this.gauge = gauge;
    this.histogram = histogram;
    this.counter = counter;
    this.meter = meter;
    this.timer = timer;
  }

  public SortedMap<String, Gauge> gauges() {
    return mapOf("gauge", gauge);
  }

  public SortedMap<String, Histogram> histograms() {
    return mapOf("histogram", histogram);
  }

  public SortedMap<String, Counter> counters() {
    return mapOf("counter", counter);
  }

  public SortedMap<String, Meter> meters() {
    return mapOf("meter", meter);
  }

  public SortedMap<String, Timer> timers() {
    return mapOf("timer", timer);
  }

  private <T extends Metric> SortedMap<String, T> mapOf(String key, T item) {
    SortedMap<String, T> result = new TreeMap<>();
    result.put(key, item);
    return result;
  }

  static InputTestData build() {
    Reservoir reservoir = new ExponentiallyDecayingReservoir();
    Histogram histogram = new Histogram(reservoir);
    Counter counter = new Counter();
    counter.inc(44);
    Meter meter = new Meter();
    meter.mark(55);
    Timer timer = new Timer(new SlidingWindowReservoir(23));
    timer.time(
        () -> {
          try {
            TimeUnit.MICROSECONDS.sleep(11);
          } catch (InterruptedException e) {
            // ignored
          }
        });
    return new InputTestData(() -> 71, histogram, counter, meter, timer);
  }
}
