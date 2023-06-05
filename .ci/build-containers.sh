export IMAGE_TAG=$( if [[ -n "$CI_COMMIT_TAG" ]]; then echo $CI_COMMIT_TAG; elif [[ $CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH ]]; then echo "latest"; else echo latest-$CI_COMMIT_REF_NAME; fi)
#docker login $DOCKER_REGISTRY_HOST -u $DOCKER_REGISTRY_USER -p $DOCKER_REGISTRY_PASSWORD
for module in "client" "coordinator" "gateway" "relay" "dead-drop"
do
  IMAGE_NAME=latte-mixxiato-$module
  docker build -t $DOCKER_REGISTRY_HOST/$IMAGE_NAME:latest ./$module
#  docker push $DOCKER_REGISTRY_HOST/$IMAGE_NAME:latest
done
#docker logout