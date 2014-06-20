akka-router-scala-spring
========================

Example of using akka router with reaper when Spring injection is included

The motivation behind this project is to set up the necessary preconditions to build other more meaningful use-case of
taking data from a source and to use multiple sinks for the data.   One example would be taking live data from a site
and writing it to a distributed database. Akka espouses a "let it fail" approach which is somewhat at odds with a
no-fail persistence of data, but coupled to writing to the log and then reading the log into HDFS for later analysis
you can dance at two weddings.   I hope to continue this project with writing to MongoDB as the service instead.  Come
back to visit.

rt
