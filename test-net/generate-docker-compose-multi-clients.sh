numContainers=$1
clientsInContainer=$2
rm -f -- multi-clients.yaml
cat >> multi-clients.yaml << EOF
services:
  coordinator:
    image: registry.mixlab.mondcarion.group/latte-mixxiato-coordinator:latest
    networks:
    - mixnet
  gateway:
    image: registry.mixlab.mondcarion.group/latte-mixxiato-gateway:latest
    ports:
    - "8888:8888"
    networks:
    - mixnet
    volumes:
    - ./coord.json:/opt/app/coord.json
    depends_on:
    - coordinator
    command: "gateway 8000 8001"
  relay:
    image: registry.mixlab.mondcarion.group/latte-mixxiato-relay:latest
    networks:
    - mixnet
    volumes:
    - ./coord.json:/opt/app/coord.json
    depends_on:
    - coordinator
    command: "relay 8001 8002"
  dead-drop:
    image: registry.mixlab.mondcarion.group/latte-mixxiato-dead-drop:latest
    networks:
    - mixnet
    volumes:
    - ./coord.json:/opt/app/coord.json
    depends_on:
    - coordinator
    command: "dead-drop 8003"
EOF
for clientIndex in $(seq $numContainers)
do
  cat >> multi-clients.yaml << EOF
  client$clientIndex:
    image: registry.mixlab.mondcarion.group/latte-mixxiato-client:latest
    networks:
      - mixnet
    volumes:
      - ./coord.json:/opt/app/coord.json
      - ./scripts/run-multiple.sh:/opt/app/run-multiple.sh
    entrypoint: ./run-multiple.sh $clientsInContainer
    depends_on:
      - coordinator
      - gateway
      - relay
      - dead-drop
EOF
done
cat >> multi-clients.yaml << EOF
networks:
  mixnet:
EOF
