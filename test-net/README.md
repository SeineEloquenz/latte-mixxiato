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