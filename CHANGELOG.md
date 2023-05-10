# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.9.0] - 2022-05-09
-Update to io.dropwizard:dropwizard-metrics 2.1.6.
-Update to io.dropwizard.metrics:metrics-core 4.2.18.

## [0.8.0] - 2022-09-21
- Update to telemetry SDK 0.15.0.

## [0.7.0] - 2022-06-01
- Add customizable Transformers for modifying metric data.

## [0.6.0] - 2022-05-31
- Update to telemetry SDK 0.13.2 and fix API breakage.
- Clean up readme examples.

## [0.5.0] - 2020-06-30
- Upgrade to Telemetry SDK 0.6.1
- Allow URI override to include or omit the full endpoint path component.
- Close TelemetryClient upon NewRelicReporter.close()

## [0.4.0] - 2020-04-01
- Contributed: `NewRelicReporterFactory` by [Steven Schwell](https://github.com/sschwell)
- Changed: update dropwizard-core version to 4.1.5
- Changed: update to NewRelic telemetry SDK 0.4.0

## [0.3.0] - 2020-03-30
- Changed: update to NewRelic telemetry SDK 0.3.3

## [0.2.1] - 2019-09-13
- Changed: The `source.type` attribute is no longer added to generated metrics.

## [0.2.0] - 2019-09-10
- Added : The capability to filter metric attributes
- Changed: Sightly tweaked the metric and attribute naming schemes
- Added : The reporter logs a message when it starts up
- Added : Generated metrics include metadata about the reporter in attributes.

## [0.1.1] - 2019-08-26
- Initial public release of the New Relic dimensional-metrics-based Dropwizard Reporter
