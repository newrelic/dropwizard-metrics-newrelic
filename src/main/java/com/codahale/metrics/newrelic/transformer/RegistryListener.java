/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.codahale.metrics.newrelic.transformer;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistryListener;
import com.codahale.metrics.Timer;

public interface RegistryListener extends MetricRegistryListener {

  @Override
  default void onGaugeAdded(String name, Gauge<?> gauge) {}

  @Override
  default void onGaugeRemoved(String name) {}

  @Override
  default void onCounterAdded(String name, Counter counter) {}

  @Override
  default void onCounterRemoved(String name) {}

  @Override
  default void onHistogramAdded(String name, Histogram histogram) {}

  @Override
  default void onHistogramRemoved(String name) {}

  @Override
  default void onMeterAdded(String name, Meter meter) {}

  @Override
  default void onMeterRemoved(String name) {}

  @Override
  default void onTimerAdded(String name, Timer timer) {}

  @Override
  default void onTimerRemoved(String name) {}
}
