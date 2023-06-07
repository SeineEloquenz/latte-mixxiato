if [ "$#" -lt 1 ]; then
  echo "You need to pass the amount of containers to start"
  exit -1
fi
pushd ..
mvn clean package
export DOCKER_REGISTRY_HOST=registry.mixlab.mondcarion.group
bash .ci/build-containers.sh
popd
sh generate-docker-compose.sh $1
docker compose -f many-clients.yaml up