# New Relic Dropwizard reporter
A [Dropwizard metrics](https://metrics.dropwizard.io/4.0.0/) reporter for sending dimensional metrics to New Relic using the New Relic Java Telemetry SDK.
For the juicy details on how Dropwizard metrics are mapped to New Relic dimensional metrics,
please visit [the exporter specs documentation repo](https://github.com/newrelic/exporter-specs/tree/master/dropwizard). 

# How To Use

## gradle

`build.gradle:`
```
compile("com.newrelic.telemetry:dropwizard-metrics-newrelic:0.4.0")
compile("com.newrelic.telemetry:telemetry-http-okhttp:0.4.0")
```

or if you're using kotlin build gradle...

`build.gradle.kts:`
```
implementation("com.newrelic.telemetry:dropwizard-metrics-newrelic:0.4.0")
implementation("com.newrelic.telemetry:telemetry-http-okhttp:0.4.0")
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

The `YOUR_SECRET_API_KEY` is referring to your New Relic Event API insert key. For more information and how to obtain a key, [visit our docs](https://docs.newrelic.com/docs/insights/insights-data-sources/custom-data/send-custom-events-event-api#register).  

```
MetricRegistry metricRegistry = new MetricRegistry(); // If you're already using dropwizard-metrics you may already have one of these.
...
String apiKey = "<YOUR_SECRET_API_KEY>";
MetricBatchSender metricBatchSender = SimpleMetricBatchSender.builder(apiKey).build();

Attributes commonAttributes = new Attributes()
            .put("host", InetAddress.getLocalHost().getHostName())
            .put("appName", "Your Application Name Here")
            .put("other", "any other common attributes you wish");
            
NewRelicReporter reporter = NewRelicReporter.build(metricRegistry, metricBatchSender)
        .commonAttributes(commonAttributes)
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

If you have a dropwizard project and have at least `dropwizard-core` 0.7.X, 
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

### [![Javadocs][javadoc-image]][javadoc-url]

### Building
CI builds are run on Azure Pipelines: 
[![Build Status](https://dev.azure.com/NRAzurePipelines/Java%20CI/_apis/build/status/PR%20build%20for%20dropwizard%20metrics?branchName=master)](https://dev.azure.com/NRAzurePipelines/Java%20CI/_build/latest?definitionId=4&branchName=master)
The project uses gradle 5 for building, and the gradle wrapper is provided.

To compile, run the tests and build the jars:

`$ ./gradlew build`

### Find and use your data

For tips on how to find and query your data, see [Find metric data](https://docs.newrelic.com/docs/data-ingest-apis/get-data-new-relic/metric-api/introduction-metric-api#find-data).

For general querying information, see:
- [Query New Relic data](https://docs.newrelic.com/docs/using-new-relic/data/understand-data/query-new-relic-data)
- [Intro to NRQL](https://docs.newrelic.com/docs/query-data/nrql-new-relic-query-language/getting-started/introduction-nrql)

### Release Process

#### Publish to Staging Repo

To stage the release simply submit and merge a PR to update the [build.gradle.kts](build.gradle.kts) file with the version to be released (e.g. `version := "0.4.0"`).

Results of the job can be viewed here: https://dev.azure.com/NRAzurePipelines/Java%20CI/_build
After the staging release job has run successfully it will publish the new artifact to a staging repository on Sonatype at: https://oss.sonatype.org/#stagingRepositories.

#### Manually Release Staging Repo

1. Find the staging repo on Sonatype, which should be named similar to `comnewrelic-nnnn`, and validate that the contents and version look correct.
2. If the contents look correct, select the staging repo and choose `close`, leaving a comment such as `releasing 0.4.0`.
3. When the staging repo is finished closing, select the staging repo and choose `release`, keeping the `Automatically Drop` checkbox checked, and leave a comment such as `releasing 0.4.0`.
4. Verify that the artifact was published on Maven Central at: https://repo1.maven.org/maven2/com/newrelic/telemetry/dropwizard-metrics-newrelic/

#### Post Release

Submit and merge a PR with the following:
* Update the [build.gradle.kts](build.gradle.kts) file with to a snapshot version of a potential future release (e.g. `version  := "0.4.1-SNAPSHOT"`).
* Update the [CHANGELOG](CHANGELOG.md) with details of the new release:
  ```markdown
  ## [0.4.0]
  - Miscellaneous bug fixes and tweaks
  ```
* Update the [Usage](#usage) example in the [README](README.md) with the newly released version (e.g. `implementation("com.newrelic.telemetry:dropwizard-metrics-newrelic:0.4.0")`).

[javadoc-image]: https://www.javadoc.io/badge/com.newrelic.telemetry/dropwizard-metrics-newrelic.svg
[javadoc-url]: https://www.javadoc.io/doc/com.newrelic.telemetry/dropwizard-metrics-newrelic
