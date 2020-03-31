/*
 *  ---------------------------------------------------------------------------------------------
 *   Copyright (c) 2020 New Relic Corporation. All rights reserved.
 *   Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 *  --------------------------------------------------------------------------------------------
 */

package com.codahale.metrics.newrelic;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.metrics.MetricsFactory;
import org.junit.jupiter.api.Test;

class NewRelicReporterFactoryTest {

  @Test
  void testLoadingFromYamlConfig() throws Exception {
    System.out.println("NewRelicReporterFactory.class = " + NewRelicReporterFactory.class);
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    YamlConfigurationFactory<MetricsFactory> configFactory =
        new YamlConfigurationFactory<>(MetricsFactory.class, Validators.newValidator(), mapper, "");
    MetricsFactory factory =
        configFactory.build(new ResourceConfigurationSourceProvider(), "dropwizard.yaml");
    System.out.println("factory = " + factory);
  }
}
