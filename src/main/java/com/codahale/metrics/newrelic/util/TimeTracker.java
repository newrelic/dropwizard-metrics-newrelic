/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.codahale.metrics.newrelic.util;

import com.codahale.metrics.Clock;
import java.util.concurrent.atomic.AtomicLong;

public class TimeTracker {

  private final Clock clock;
  private final AtomicLong previousTime;

  public TimeTracker(Clock clock) {
    this.clock = clock;
    this.previousTime = new AtomicLong(clock.getTime());
  }

  // call this at the end of the harvest/report
  public void tick() {
    previousTime.set(clock.getTime());
  }

  public long getCurrentTime() {
    return clock.getTime();
  }

  public long getPreviousTime() {
    return previousTime.get();
  }
}
