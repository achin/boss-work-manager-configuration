# boss-work-manager-configuration

A simple web app that randomly generates Java class names and asks you which
one's real.

The app builds a simple Markov model from a file containing tokens from Java
class names taken from the [Spring
Framework](https://github.com/spring-projects/spring-framework), randomly
generates two fake class names, and presents them along with one real one for
you to choose from.


The app is currently deployed at [http://java.metagno.me](http://java.metagno.me).

## Usage

To run the web application:

    $ lein run -m boss-work-manager-configuration.app 8080

To build the [class name data
file](https://github.com/achin/boss-work-manager-configuration/blob/master/resources/class-names):

* Use [JELDoclet](http://jeldoclet.sourceforge.net/) to generate a big XML
* document Use the tools in the `boss-work-manager-configuration.ingest` and
* `boss-work-manager-configuration.ingest.jeldoclet` namespaces to create
* a token file

Examples are available in the `resources/` directory.

## License

Copyright © 2014 Alex Chin

Distributed under the Eclipse Public License either version 1.0 or (at your
option) any later version.
