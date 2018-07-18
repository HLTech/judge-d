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

### Prerequisites

For development you need:
* [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Maven](https://maven.apache.org/)

### Installing

To compile code, run tests and build jar files simply use 

```
mvn clean install
```

After that jar files will appear in target directories.

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

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/your/project/tags). 

## Authors

* **Tomasz Krzyżak** - *Development* - [krzyzy](https://github.com/krzyzy)
* **Filip Łazarski** - *Development*
* **Adrian Michalik** - *Development* - [garlicsauce](https://github.com/garlicsauce)

See also the list of [contributors](https://github.com/HLTech/judge-d/contributors) who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Hat tip to anyone whose code was used
* Inspiration
* etc
