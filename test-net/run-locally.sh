pushd ..
mvn clean package
export DOCKER_REGISTRY_HOST=registry.mixlab.mondcarion.group
bash .ci/build-containers.sh
popd
docker compose up