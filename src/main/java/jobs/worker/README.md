# worker
This package is responsible for providing a server which handles many jobs.
It provides an interface through which a client can:

* Checking the status of the worker
* Starting new jobs
  * When the job completes, this is reported to the client
* Checking the status of particular jobs

It accesses the wrapper provided in the job package.
