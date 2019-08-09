/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.codahale.metrics.newrelic.transformer.interfaces;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Metric;
import java.util.Collection;
import java.util.function.Supplier;

interface DropWizardComponentTransformer<T> {

  Collection<Metric> transform(
      String name, T dropWizardComponent, Supplier<Attributes> baseAttributes);
}
