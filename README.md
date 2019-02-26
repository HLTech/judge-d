# judge-d

[![Build Status](https://travis-ci.org/HLTech/judge-d.svg?branch=master)](https://travis-ci.org/HLTech/judge-d.svg?branch=master)
[![Coverage Status](https://coveralls.io/repos/github/HLTech/judge-d/badge.svg?branch=master)](https://coveralls.io/github/HLTech/judge-d?branch=master)
[![Scrutinizer Code Quality](https://scrutinizer-ci.com/g/HLTech/judge-d/badges/quality-score.png?b=master)](https://scrutinizer-ci.com/g/HLTech/judge-d/?branch=master)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)

Judge Dredd is a tool used to test contracts between microservices.

# Motivation

In a microservice architecture it is of importance to ensure that communication between them is not broken after changes in 
any of participating sides. One of the best approaches to tackle this problem is contract testing. Judge Dredd is 
an open source project which aims at performing contract tests between any microservices deployed within any environment under
 Judge Dredd jurisdiction.

# Idea

Judge Dredd includes two functional roles - agent and server. Agents are deployed within any environment and gather information
about names and versions of any service within this environment. For now Judge Dredd agents support Kubernetes environments, 
however integration with another sources of information about environment (like Consul) is straightforward.
Agents periodically collect information about any service within controlled environment and send it to Judge Dredd server
(called Judge Dredd as well) using server's REST API.

Judge Dredd server maintains information about environments and services (names and versions) and does verification of
contracts for given service (name and version).
Judge Dredd provides REST API. It allows:
* Saving/getting information about environments and updating services' names and versions
* Saving/getting information about contracts for given service name and version
* Verifying contracts for service's name and version

Basing on its knowledge of services (names and versions) belonging to particular environment obtained from an agent and 
its knowledge of contracts obtained from services themselves Dredd is able to judge if deployment of a new version of 
the service to the environment would break communication between this service and any other in this environment.

For now Judge Dredd supports expectations specified in [Pact files](https://github.com/pact-foundation/pact-specification) 
(json) and capabilities specified in [Swagger file](https://swagger.io/specification/) (json).

# Reference implementation
Basically, there are 2 steps - contract publishing and contract verification. They should be added to your CI/CD tool
(like Jenkins). Both publishing and verification should be done before actual deployment into any environment.

## Contract Publishing
Contract publishing should be done for both service expectations (what do I expect from the others? What message format 
do I send?) and capabilities (What am I able to offer to the others? What message format do I accept and return?) for
given service name and version. Publishing means sending a REST request to Judge Dredd's endpoint with information about
expectations, capabilities and the protocol of communication. Currently only REST is supported.

## Contract Verification
Contract verification means checking if newly introduced changes do not break possibility of communication between services.
Verification should be done against all environments where the services are being deployed. Verification is done by
sending a REST request to Judge Dredd's endpoint with information about service name and version. Judge Dredd uses information
about service providers from Pact files generated by the consumers. It allows to link consumers to providers.

Sample Pact file is as follows:

````
{
  "provider": {
    "name": "judge-d-server"
  },
  "consumer": {
    "name": "judge-d-agent"
  },
  "interactions": [
    {
      "description": "publish request; 200 OK response",
      "request": {
        "method": "PUT",
        "path": "environments/El2ukVED6J",
        "headers": {},
        "query": "",
        "body": [
          {
            "name": "ZAEkD_yJJf",
            "version": "cttO3Djh8X"
          }
        ]
      },
      "response": {
        "status": "200",
        "headers": {}
      }
    }
  ],
  "metadata": {
    "pactSpecificationVersion": "1.0.0"
  }
}
````

Sample Swagger file is as follows:

````
{
  "swagger": "2.0",
  "info": {
    "description": "Api Documentation",
    "version": "1.0",
    "title": "Api Documentation",
    "termsOfService": "urn:tos",
    "contact": {},
    "license": {
      "name": "Apache 2.0",
      "url": "http://www.apache.org/licenses/LICENSE-2.0"
    }
  },
  "host": "localhost",
  "basePath": "/",
  "tags": [
    {
      "name": "environment-controller",
      "description": "Environment Controller"
    }
  ],
  "paths": {
    "/environments/{name}": {
      "put": {
        "tags": [
          "environment-controller"
        ],
        "summary": "Update the environment",
        "operationId": "update environment",
        "consumes": [
          "application/json"
        ],
        "produces": [
          "*/*"
        ],
        "parameters": [
          {
            "name": "name",
            "in": "path",
            "description": "name",
            "required": true,
            "type": "string"
          },
          {
            "in": "body",
            "name": "services",
            "description": "services",
            "required": true,
            "schema": {
              "type": "array",
              "items": {
                "$ref": "#/definitions/ServiceForm"
              }
            }
          }
        ],
        "responses": {
          "200": {
            "description": "Success"
          },
          "201": {
            "description": "Created"
          },
          "401": {
            "description": "Unauthorized"
          },
          "403": {
            "description": "Forbidden"
          },
          "404": {
            "description": "Not Found"
          },
          "500": {
            "description": "Failure"
          }
        }
      }
    }
  },
  "definitions": {
    "ServiceForm": {
      "type": "object",
      "properties": {
        "name": {
          "type": "string"
        },
        "version": {
          "type": "string"
        }
      }
    }
  }
}

````

In the examples above a consumer called "judge-dredd-agent" relies on functionality from provider "judge-dredd-server".
Expectations specified in Pact file are compared to capabilities in Swagger file. We can see that Pact file and Swagger file 
are compatible.Judge Dredd internally uses Atlassian library [swagger-request-validator-pact](https://bitbucket.org/atlassian/swagger-request-validator) 
to compare Pact files and Swagger files.

Judge Dredd server should be deployed on one environment. Judge Dredd agent should be deployed on each environment which
is planned to be under Judge Dredd jurisdiction. For now Kubernetes environments are supported.  

# Prerequisites

For development you need:
* [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [PostgreSQL](https://www.postgresql.org/)

You can use your local Maven distribution, but there is maven wrapper included in project. To use it just type `./mvnw {command}` 
(for UNIX users) or `mvnw.cmd {command}` (for Windows users)


# Installing

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

### Running the tests

Tests are part of `mvn clean install` task - but if you'd like to only run tests (without building jar file) use
```
mvn test
```

# Deployment

Easiest way to run Judge D wherever you need is by using Docker - image is available in our 
[Dockerhub](https://hub.docker.com/r/hltech/judge-d/).

Of course you can use jar file generated during installation as well and deploy it on some application server like Tomcat.  

# Validation

Validation behaviour can be modified using one of the options listed [here](https://bitbucket.org/atlassian/swagger-request-validator/src/0dff457f9ea7614d606ae8475d65cfe950570031/swagger-request-validator-core/README.md?fileviewer=file-view-default).
If you want to run the application using docker container, you can pass environment variable `VALIDATION_OPTIONS` with comma-separated validation properties. 

Below setting causes changing default validation level to IGNORE and emission of validation error `validation.schema.required` at ERROR level.
```
VALIDATION_OPTIONS=defaultLevel=IGNORE,validation.schema.required=ERROR,
```

See more about validation behaviours and levels [here](https://bitbucket.org/atlassian/swagger-request-validator/src/0dff457f9ea7614d606ae8475d65cfe950570031/swagger-request-validator-core/README.md?fileviewer=file-view-default).

# Built With

* [Maven](https://maven.apache.org/) - Dependency Management
* [Docker](https://www.docker.com/) - Containerization engine

# Report generation

The result of running validations in Judge D is json containing data result. Reading json files is not very human
friendly so in assets/report-generator there is python script that generates nice HTML report. To use it you need installed
python and jinja2 libs. You can also use Dockerfile located in this directory.

# Contributing

Want to help? Have any problems or questions? Let us know!

* create an issue...
* ... or if it already exists comment on it
* for detailed informations about contributing read our [Contribution guide](../master/CONTRIBUTING.md)


# Versioning

TODO - Gitflow versioning in plans.

# Authors

* **Tomasz Krzyżak** - *Development* - [krzyzy](https://github.com/krzyzy)
* **Filip Łazarski** - *Development* - [Felipe444](https://github.com/Felipe444)
* **Adrian Michalik** - *Development* - [garlicsauce](https://github.com/garlicsauce)

See also the list of [contributors](https://github.com/HLTech/judge-d/contributors) who participated in this project.

# License

judge dredd is [MIT licensed](./LICENSE).
