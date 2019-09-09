/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.codahale.metrics.newrelic.transformer.interfaces;

import static java.util.Collections.singleton;

import com.codahale.metrics.Counting;
import com.codahale.metrics.newrelic.util.TimeTracker;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.metrics.Count;
import com.newrelic.telemetry.metrics.Metric;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class CountingTransformer implements DropWizardComponentTransformer<Counting> {

  private final TimeTracker timeTracker;
  private final Map<String, Long> previousValues = new ConcurrentHashMap<>();

  public CountingTransformer(TimeTracker timeTracker) {
    this.timeTracker = timeTracker;
  }

  @Override
  public Collection<Metric> transform(
      String name, Counting counting, Supplier<Attributes> baseAttributes) {
    long count = counting.getCount();
    Long previousValue = previousValues.put(name, count);
    // if the previous value is higher than the current one, then we assume the counter has been
    // reset, and we send the current as the delta.
    if ((previousValue != null) && (previousValue <= count)) {
      count = count - previousValue;
    }
    return singleton(
        new Count(
            name,
            count,
            timeTracker.getPreviousTime(),
            timeTracker.getCurrentTime(),
            baseAttributes.get()));
  }

  public void remove(String metricName) {
    previousValues.remove(metricName);
  }
}
