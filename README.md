# Breakpoint Frontend

[![Build Status](https://secure.travis-ci.org/breakpoint-eval/frontend.png?branch=master)](http://travis-ci.org/breakpoint-eval/frontend)

This is the Frontend to the Breakpoint Eval system.

This means:
* Users register, log in, create apps, etc, via this frontend.
* API requests are handeled by this frontend (the frontend passes it off to BCS)

# Hacking

To set up a development environment, you need a few things:

* postgresql-server (at least a database that Frontend can connect to and
  maintain)
* A working SBT and Java environment
* A few minutes of time.

1) To start, clone the repository.
2) Change into the `conf/` directory and copy `application.conf.dist` to
   `application.conf` and tweak application.conf to suit your needs.
3) Go back up a directory into the main frontend directory.
4) Run `sbt run` and wait for SBT to download the internet.
5) Go to `http://localhost:9000/` in your browser, and be in awe.

# License

Apache 2.0.
