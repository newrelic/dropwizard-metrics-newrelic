/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.codahale.metrics.newrelic.transformer.interfaces;

import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.codahale.metrics.Counting;
import com.codahale.metrics.newrelic.util.TimeTracker;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Count;
import com.newrelic.telemetry.Metric;
import java.util.Collection;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CountingTransformerTest {

  private TimeTracker timeTracker;
  private Counting counting;
  private Supplier<Attributes> baseAttributes;

  @BeforeEach
  void setUp() {
    timeTracker = mock(TimeTracker.class);
    counting = mock(Counting.class);
    baseAttributes = Attributes::new;
  }

  @Test
  void testFirstConversion() throws Exception {
    long now = System.currentTimeMillis();

    when(counting.getCount()).thenReturn(37L);
    when(timeTracker.getCurrentTime()).thenReturn(now);
    when(timeTracker.getPreviousTime()).thenReturn(now - 5000);

    Count expected = new Count("chocula", 37d, now - 5000, now, new Attributes());

    CountingTransformer countingTransformer = new CountingTransformer(timeTracker);
    assertEquals(
        singleton(expected), countingTransformer.transform("chocula", counting, baseAttributes));
  }

  @Test
  void testNextConversion() throws Exception {
    long now = System.currentTimeMillis();

    when(counting.getCount()).thenReturn(37L, 49L);
    when(timeTracker.getCurrentTime()).thenReturn(now, now + 5000);
    when(timeTracker.getPreviousTime()).thenReturn(now - 5000, now);

    Count firstExpected = new Count("chocula", 37d, now - 5000, now, new Attributes());
    Count secondExpected = new Count("chocula", 12d, now, now + 5000, new Attributes());

    CountingTransformer countingTransformer = new CountingTransformer(timeTracker);
    assertEquals(
        singleton(firstExpected),
        countingTransformer.transform("chocula", counting, baseAttributes));
    assertEquals(
        singleton(secondExpected),
        countingTransformer.transform("chocula", counting, baseAttributes));
  }

  @Test
  void testBackwardsCount() throws Exception {
    long now = System.currentTimeMillis();

    when(counting.getCount()).thenReturn(37L, 12L);
    when(timeTracker.getCurrentTime()).thenReturn(now, now + 5000);
    when(timeTracker.getPreviousTime()).thenReturn(now - 5000, now);

    Count firstExpected = new Count("chocula", 37d, now - 5000, now, new Attributes());
    Count secondExpected = new Count("chocula", 12d, now, now + 5000, new Attributes());

    CountingTransformer countingTransformer = new CountingTransformer(timeTracker);
    assertEquals(
        singleton(firstExpected),
        countingTransformer.transform("chocula", counting, baseAttributes));
    assertEquals(
        singleton(secondExpected),
        countingTransformer.transform("chocula", counting, baseAttributes));
  }

  @Test
  public void testAfterRemoval() throws Exception {
    long now = System.currentTimeMillis();

    when(counting.getCount()).thenReturn(37L);
    when(timeTracker.getCurrentTime()).thenReturn(now);
    when(timeTracker.getPreviousTime()).thenReturn(now - 5000);

    Count expected = new Count("chocula", 37d, now - 5000, now, new Attributes());

    CountingTransformer countingTransformer = new CountingTransformer(timeTracker);
    Collection<Metric> firstResult =
        countingTransformer.transform("chocula", counting, baseAttributes);
    countingTransformer.remove("chocula");
    Collection<Metric> resultAfterClear =
        countingTransformer.transform("chocula", counting, baseAttributes);

    assertEquals(singleton(expected), firstResult);
    assertEquals(singleton(expected), resultAfterClear);
  }
}
