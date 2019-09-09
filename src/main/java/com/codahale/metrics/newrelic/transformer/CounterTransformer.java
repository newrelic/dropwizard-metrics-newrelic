/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.codahale.metrics.newrelic.transformer;

import static java.util.Collections.singleton;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Counter;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.metrics.Gauge;
import com.newrelic.telemetry.metrics.Metric;
import java.util.Collection;

public class CounterTransformer implements DropWizardMetricTransformer<Counter> {

  private final Clock clock;

  public CounterTransformer() {
    this(Clock.defaultClock());
  }

  // exists for testing
  CounterTransformer(Clock clock) {
    this.clock = clock;
  }

  @Override
  public Collection<Metric> transform(String name, Counter counter) {
    return singleton(
        new Gauge(
            name,
            counter.getCount(),
            clock.getTime(),
            new Attributes().put("source.type", "counter")));
  }
}
