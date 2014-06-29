akka-router-scala-spring
========================

Example of using akka router with reaper when Spring injection is included

The motivation behind this project is to set up the necessary preconditions to build other more meaningful use-case of
taking data from a source and to use multiple sinks for the data.   One example would be taking live data from a site
and writing it to a distributed database. Akka espouses a "let it fail" approach which is somewhat at odds with a
no-fail persistence of data, but coupled to writing to the log and then reading the log into HDFS for later analysis
you can dance at two weddings.

About the code
==============

The organization has four levels:  A Main program which creates an Actor that collects the results, creates a router,
and assigns a set of worker Actors as the routees.

Each worker Actor has it's business code, in this case the calculation of a fibonacci number, injected by Spring
configuration.  When the last list of numbers to be calculated finishes the collector sets a watch on the router,
sends a broadcast message to the router.  The PoisonPill goes to each worker actor as it's last message.  When consumed
the worker actor then terminates.   When all of the routees have terminated the the router itself shuts down and the
collector receives notification and shuts itself down.  The Main is periodically checking the collector and the entire
program terminates when the collector is no longer alive.

One of the nice things about having the business code as a injected service is that the logic can be tested very simply.

  I hope to continue this project with writing to MongoDB as the service instead.  Come
back to visit.

rt
