[![Community Plus header](https://github.com/newrelic/opensource-website/raw/master/src/images/categories/Community_Plus.png)](https://opensource.newrelic.com/oss-category/#community-plus)

# New Relic Dropwizard reporter
A [Dropwizard metrics](https://metrics.dropwizard.io/4.0.0/) reporter for sending dimensional metrics to New Relic using the New Relic Java Telemetry SDK.

For the juicy details on how Dropwizard metrics are mapped to New Relic dimensional metrics,
please visit [the exporter specs documentation repo](https://github.com/newrelic/exporter-specs/tree/master/dropwizard). 

# How To Use

## gradle

Add required `build.gradle` dependencies to your project:

```
implementation("com.newrelic.telemetry:dropwizard-metrics-newrelic:1.0.0")
implementation("com.newrelic.telemetry:telemetry-core:0.15.0")
implementation("com.newrelic.telemetry:telemetry-http-okhttp:0.15.0")
```

If you do not want to depend on okhttp, you can remove the dependency on `telemetry-http-okhttp`, 
but you will need to construct a `MetricBatchSender` instance using its builder and provide your
own implementation of the `com.newrelic.telemetry.http.HttpPoster` interface.

```
MetricBatchSender sender = MetricBatchSender.builder().httpPoster(<your implementation>);
```

Note: to use the sample code below, you will need the `telemetry-http-okhttp` library mentioned above. It provides
implementations communicating via HTTP using the okhttp libraries, respectively.

## Start the reporter

Early in the lifecycle of your application, you will want to create and
start a `NewRelicReporter`. 

The `YOUR_SECRET_API_KEY` is referring to your New Relic Event API insert key or license key. 
For more information and how to obtain a key, [visit our docs](https://docs.newrelic.com/docs/insights/insights-data-sources/custom-data/send-custom-events-event-api#register).

```
MetricRegistry metricRegistry = new MetricRegistry(); // If you're already using dropwizard-metrics you may already have one of these.
...
String apiKey = System.getenv("YOUR_SECRET_API_KEY");

// For US region: https://metric-api.newrelic.com/metric/v1
// For EU region: https://metric-api.eu.newrelic.com/metric/v1
String ingestURI = System.getenv("METRIC_INGEST_URI");

SenderConfiguration senderConfiguration = MetricBatchSender.configurationBuilder()
        .apiKey(apiKey)
        .useLicenseKey(true) // Optional if using a license key instead of an insert key
        .endpoint(URI.create(ingestURI).toURL())
        .httpPoster(new OkHttpPoster(Duration.ofSeconds(2)))
        .build();

MetricBatchSender metricBatchSender = MetricBatchSender.create(senderConfiguration);

Attributes commonAttributes = new Attributes()
            .put("host", InetAddress.getLocalHost().getHostName())
            .put("appName", "Your Application Name Here")
            .put("other", "any other common attributes you wish");
            
NewRelicReporter reporter = NewRelicReporter.build(metricRegistry, metricBatchSender)
        .commonAttributes(commonAttributes)
        .build();
        
reporter.start(15, TimeUnit.SECONDS);
```
## Customizing Reported Metrics
If you would like to customize the way metric names or attributes are reported to New Relic, you will want to supply
customizers to the `NewRelicReporterBuilder`.

Consider metrics with tags encoded in their names, formatted like so: `"metricName[tag:value,othertag:othervalue]"`,
you might set up a reporter to report the metric name as `metricName` and add merge of the key/value pairs in `name`
with the `commonAttributes`. 

```
MetricRegistry metricRegistry = new MetricRegistry(); // If you're already using dropwizard-metrics you may already have one of these.
...
String apiKey = "<YOUR_SECRET_API_KEY>";
MetricBatchSender metricBatchSender = MetricBatchSenderFactory
                .fromHttpImplementation(OkHttpPoster::new)
                .createBatchSender(apiKey);

Attributes commonAttributes = new Attributes()
            .put("host", InetAddress.getLocalHost().getHostName())
            .put("appName", "Your Application Name Here")
            .put("other", "any other common attributes you wish");

MetricAttributesCustomizer mergeAttributesFromTaggedMetricName =
    (name, metric, attributes) -> {
      Attributes tagsAsAttributes = new Attributes();

      // get a stream of each tag:value pair within the square brackets of name and add
      // each pair to the tagsAsAttributes Attributes object
      Stream.of(name.substring(name.indexOf('['), name.indexOf(']')).split(","))
          .forEach(
              str -> {
                String[] keyValuePair = str.split(":");

                tagsAsAttributes.put(keyValuePair[0], keyValuePair[1]);
              });

      return tagsAsAttributes.putAll(attributes);
    };

NewRelicReporter reporter = NewRelicReporter.build(metricRegistry, metricBatchSender)
        .commonAttributes(commonAttributes)
        // customizer to strip encoded tags from metric name
        .metricNameCustomizer(name -> name.substring(0, name.indexOf('[')))
        .metricAttributeCustomizer(mergeAttributesFromTaggedMetricName)
        .build();

reporter.start(15, TimeUnit.SECONDS);
```

## Dropwizard integration

If you are using the actual Dropwizard REST framework, you can get a reference to the 
`MetricRegistry` from the Dropwizard `Environment` in order to register your reporter.  
It might look something like this:


```$java
public class MyApplication extends Application<MyConfig> {

...
    @Override
    public void run(MyConfig configuration, Environment environment) {
        MetricRegistry registry = environment.metrics();
        MetricBatchSender metricBatchSender = buildMetricBatchSender(); // see above for more complete example
        NewRelicReporter reporter = NewRelicReporter.build(registry, metricBatchSender)
                .commonAttributes(commonAttributes)
                .build();
        reporter.start(15, TimeUnit.SECONDS);

        ...
    }
...
}
``` 

### Dropwizard Metrics Reporter

If you have a dropwizard project and have at least `dropwizard-core` 1.1.0, 
then you can perform the following steps to automatically report metrics to
New Relic.

Add the following to your `dropwizard` YAML config file.

~~~yaml
metrics:
  frequency: 1 minute                       # Default is 1 second.
  reporters:
    - type: newrelic
      apiKey: <YOUR_SECRET_API_KEY>
      overrideUri:                          # Optional. Defaults to https://metric-api.newrelic.com/
      disabledMetricAttributes:             # Optional. Defaults to (none)
      commonAttributes:                     # Optional. Defaults to (none)
      includes:                             # Optional. Defaults to (all).
      excludes:                             # Optional. Defaults to (none).
~~~

Once your `dropwizard` application starts, your metrics should start appearing
in New Relic.

### Javadoc for this project can be found here: [![Javadocs][javadoc-image]][javadoc-url]
[javadoc-image]: https://www.javadoc.io/badge/com.newrelic.telemetry/dropwizard-metrics-newrelic.svg
[javadoc-url]: https://www.javadoc.io/doc/com.newrelic.telemetry/dropwizard-metrics-newrelic

### Building
This project requires Java 11. To compile, run the tests and build the jars:

`$ ./gradlew build`

### Find and use your data

For tips on how to find and query your data, see [Find metric data](https://docs.newrelic.com/docs/data-ingest-apis/get-data-new-relic/metric-api/introduction-metric-api#find-data).

For general querying information, see:
- [Query New Relic data](https://docs.newrelic.com/docs/using-new-relic/data/understand-data/query-new-relic-data)
- [Intro to NRQL](https://docs.newrelic.com/docs/query-data/nrql-new-relic-query-language/getting-started/introduction-nrql)

## Support
New Relic hosts and moderates an online forum where customers can interact with New Relic employees as well as other customers to get help and share best practices. Like all official New Relic open source projects, there's a related Community topic in the New Relic Explorers Hub. You can find this project's topic/threads here:

https://discuss.newrelic.com/tags/javaagent

### Contributing
We encourage your contributions to improve the New Relic Dropwizard reporter! Keep in mind that when you submit your pull request, you'll need to sign the CLA via the click-through using CLA-Assistant. You only have to sign the CLA one time per project.

If you have any questions, or to execute our corporate CLA (which is required if your contribution is on behalf of a company), drop us an email at opensource@newrelic.com.

**A note about vulnerabilities**

As noted in our [security policy](../../security/policy), New Relic is committed to the privacy and security of our customers and their data. We believe that providing coordinated disclosure by security researchers and engaging with the security community are important means to achieve our security goals.

If you believe you have found a security vulnerability in this project or any of New Relic's products or websites, we welcome and greatly appreciate you reporting it to New Relic through [HackerOne](https://hackerone.com/newrelic).

If you would like to contribute to this project, review [these guidelines](./CONTRIBUTING.md).

### Licensing
The New Relic Dropwizard reporter is licensed under the Apache 2.0 License.
