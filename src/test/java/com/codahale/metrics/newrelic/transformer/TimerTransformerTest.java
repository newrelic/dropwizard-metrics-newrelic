/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.codahale.metrics.newrelic.transformer;

import static com.codahale.metrics.newrelic.transformer.TimerTransformer.ATTRIBUTES_SUPPLIER;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codahale.metrics.Timer;
import com.codahale.metrics.newrelic.transformer.interfaces.CountingTransformer;
import com.codahale.metrics.newrelic.transformer.interfaces.MeteredTransformer;
import com.codahale.metrics.newrelic.transformer.interfaces.SamplingTransformer;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Count;
import com.newrelic.telemetry.Gauge;
import com.newrelic.telemetry.Metric;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.collections.Sets;

class TimerTransformerTest {

  @Test
  void testConvert() {
    long now = System.currentTimeMillis();
    Gauge result1 = new Gauge("fast", 55d, now, new Attributes());
    Gauge result2 = new Gauge("fast", 999d, now, new Attributes());
    Count expectedCount =
        new Count(
            "fast",
            99999.6666,
            System.currentTimeMillis(),
            System.currentTimeMillis() + 100,
            new Attributes());

    Timer timer = mock(Timer.class);
    SamplingTransformer samplingTransformer = mock(SamplingTransformer.class);
    MeteredTransformer meteredTransformer = mock(MeteredTransformer.class);
    CountingTransformer countingTransformer = mock(CountingTransformer.class);

    when(samplingTransformer.transform("fast", timer, ATTRIBUTES_SUPPLIER))
        .thenReturn(singleton(result1));
    when(meteredTransformer.transform("fast", timer, ATTRIBUTES_SUPPLIER))
        .thenReturn(singleton(result2));
    when(countingTransformer.transform("fast", timer, ATTRIBUTES_SUPPLIER))
        .thenReturn(singleton(expectedCount));

    TimerTransformer timerTransformer =
        new TimerTransformer(samplingTransformer, meteredTransformer, countingTransformer);

    Collection<Metric> results = timerTransformer.transform("fast", timer);
    Collection expected = Sets.newSet(result1, result2, expectedCount);
    assertEquals(expected, results);
  }

  @Test
  void testRemove() throws Exception {
    CountingTransformer counting = mock(CountingTransformer.class);
    TimerTransformer testClass = new TimerTransformer(null, null, counting);
    testClass.onTimerRemoved("money");
    verify(counting).remove("money");
  }
}
