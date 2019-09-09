/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.codahale.metrics.newrelic.transformer;

import com.newrelic.telemetry.metrics.Metric;
import java.util.Collection;

public interface DropWizardMetricTransformer<T extends com.codahale.metrics.Metric>
    extends RegistryListener {

  Collection<Metric> transform(String name, T metric);
}
