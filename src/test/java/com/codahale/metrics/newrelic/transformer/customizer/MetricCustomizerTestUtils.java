package com.codahale.metrics.newrelic.transformer.customizer;

import com.newrelic.telemetry.Attributes;
import java.util.stream.Stream;

public class MetricCustomizerTestUtils {
  public static final MetricAttributesCustomizer ATTRIBUTES_FROM_TAGGED_NAME =
      (name, metric, baseAttributes) -> {
        Attributes modifiedAttributes = new Attributes();
        // insert every "key:value" pair within square brackets of name into modifiedAttributes
        Stream.of(name.substring(name.indexOf('['), name.indexOf(']')).split(","))
            .forEach(
                str -> {
                  String[] tuple = str.split(":");
                  modifiedAttributes.put(tuple[0], tuple[1]);
                });

        return modifiedAttributes.putAll(baseAttributes);
      };

  public static final MetricNameCustomizer NAME_TAG_STRIPPER =
      name -> name.substring(0, name.indexOf('['));
}
