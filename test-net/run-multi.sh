if [ "$#" -lt 2 ]; then
  echo "You need to pass the amount of containers and clients per container to start"
  exit -1
fi
pushd ..
mvn clean package
export DOCKER_REGISTRY_HOST=registry.mixlab.mondcarion.group
bash .ci/build-containers.sh
popd
sh generate-docker-compose-multi-clients.sh $1 $2
docker compose -f multi-clients.yaml up