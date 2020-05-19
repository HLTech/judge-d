# Judge-d agent

Judge-d Agent is used by Judge-d Server to get information about services deployed on each managed environment.

## Overview

Agents are deployed within any environment and gather information about names and versions of any service within this environment.
Agents periodically collect information about any service within controlled environment and send it to Judge-d server
(called Judge-d as well) using server's REST API.

## Prerequisites

Judge-d Aggent requires following environment variables to be set:
- `JUDGE_D_BASE_URL` - Judge Server URL
- `JUDGE_D_ENV` - environment name on which agent is deployed
- `JUDGE_D_SPACE` - judge agent space name

## Environments support

For now Judge-d agents support Kubernetes and Consul environments.

**Kubernetes**

To use Judge-d agent on kubernetes environment run run it with `kubernetes` profile.

Agent uses K8s client to communicate with K8s master to fetch all required data.
Docker images fetched from all pods are source of application names and versions for agent.

By default all namespaces are scanned, but you can explicitly exclude or include some namespaces by set following environment variables:
- `EXCLUDED_NAMESPACES` - namespaces to be excluded
- `INCLUDED_NAMESPACES` - namespaces to be included

You can also customize label that has to be present in scanned pods.
By default `app` label is required. You can change it by set `requiredLabel` property. Example:

`hltech.contracts.judge-d.requiredLabel=customLabel`

**Consul**

To use Judge-d agent on environment managed by consul run it with `consul` profile.
`CONSUL_HOST` environment variable has to be set on environment to provide information about consul agent host.
Judge Agent uses consul agent API to fetch information about all services registered in the consul agent.

To be scanned by Judge Agent all services need to set a tag with current deployment version as below:

`version=current-app-version`

Services without `version` tag will be skipped by the agent.
Service names provided by Consul are used as application names by the Judge-d agent.

