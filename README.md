# judge-d

[![Build Status](https://travis-ci.org/HLTech/judge-d.svg?branch=master)](https://travis-ci.org/HLTech/judge-d.svg?branch=master)
[![Coverage Status](https://coveralls.io/repos/github/HLTech/judge-d/badge.svg?branch=master)](https://coveralls.io/github/HLTech/judge-d?branch=master)
[![Scrutinizer Code Quality](https://scrutinizer-ci.com/g/HLTech/judge-d/badges/quality-score.png?b=master)](https://scrutinizer-ci.com/g/HLTech/judge-d/?branch=master)

Living in a microservices world is not that easy. APIs change often and when it happens it is not always easy to find out what happened.
This is where Judge D comes in and lends a hand. Judge D is an open-source project focused on painless contract testing between
microservices.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. 
See deployment for notes on how to deploy the project on a live system.

### Idea

Judge D accepts REST calls with expectations of consumers of API and capabilities of providers of API. At the moment Judge D 
is able to perform contract testing just for REST, in our roadmap there is space for adding other ways of communicating like
SOAP or JMS. Judge D will know how to match expectations with capabilities and will generate report with results of contract test.

### Prerequisites

For development you need:
* [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [PostgreSQL](https://www.postgresql.org/)

You can use your local Maven distribution, but there is maven wrapper included in project. To use it just type `./mvnw {command}` 
(for UNIX users) or `mvnw.cmd {command}` (for Windows users)


### Installing

To compile code, run tests and build jar files simply use 

```
mvn clean install
```

After that jar files will appear in target directories.

To run application on your local machine you have to go to judge-d-server package and run it using mvn task:
```
cd judge-d-server
mvn spring-boot:run
```

or (preferably) use docker-compose:
```
docker-compose -f judge-d-server/compose-dependencies.yml up -d
```
to run postgres db in background, then
```
docker-compose -f judge-d-server/compose-judge-d.yml
```
to run app. 

By default you have to create new database called judge_d for the application to start, but jdbc connection properties 
can be defined in judge-d-server/compose-judge-d.yml file.

## Running the tests

Tests are part of `mvn clean install` task - but if you'd like to only run tests (without building jar file) use
```
mvn test
```

## Deployment

Easiest way to run Judge D wherever you need is by using Docker - image is available in our 
[Dockerhub](https://hub.docker.com/r/hltech/judge-d/).

Of course you can use jar file generated during installation as well and deploy it on some application server like Tomcat.  

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management
* [Docker](https://www.docker.com/) - Containerization engine

## Contributing

Want to help? Have any problems or questions? Let us know!

* create an issue...
* ... or if it already exists comment on it
* for detailed informations about contributing read our [Contribution guide](../blob/master/CONTRIBUTING.md)


## Versioning

TODO - Gitflow versioning in plans.

## Authors

* **Tomasz Krzyżak** - *Development* - [krzyzy](https://github.com/krzyzy)
* **Filip Łazarski** - *Development*
* **Adrian Michalik** - *Development* - [garlicsauce](https://github.com/garlicsauce)

See also the list of [contributors](https://github.com/HLTech/judge-d/contributors) who participated in this project.

## License

TODO
