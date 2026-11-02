# HDCharts 3.0.1 migration guide

## Internal function visibility

Some internal functions were accidentally declared public and were never intended
for consumer use. Correcting their visibility to `internal` is reported as a
breaking API change.
