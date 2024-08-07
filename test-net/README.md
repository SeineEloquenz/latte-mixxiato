# test-net

## Getting started
Make sure you have docker and the docker compose plugin installed.

## Running the testnet

### Images from CI
Login to the docker registry via `docker login registry.mixlab.mondcarion.group`

Credentials can be found [here](https://git.scc.kit.edu/groups/ps-chair/mixlab/mixnet/-/settings/ci_cd) under section variables.

Run `docker compose up`

### Locally built images
Run `run-locally.sh`

#### Use an interactive client
Run `run-locally.sh`

In a second shell:
1. Do `sh interactive-client/enter.sh`
2. Do `./run.sh`

### Run with more clients
Run `sh run-many.sh <number of containers>`
### Run with multiple clients per container
Run `sh run-multi.sh <number of containers> <number of clients per container>`