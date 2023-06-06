numClients=$1
rm generated-docker-compose.yaml
cat >> generated-docker-compose.yaml << EOF
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
for clientIndex in $(seq $numClients)
do
  cat >> generated-docker-compose.yaml << EOF
  client$clientIndex:
    image: registry.mixlab.mondcarion.group/latte-mixxiato-client:latest
    networks:
      - mixnet
    volumes:
      - ./coord.json:/opt/app/coord.json
    command: "$clientIndex"
    depends_on:
      - coordinator
      - gateway
      - relay
      - dead-drop
EOF
done
cat >> generated-docker-compose.yaml << EOF
networks:
  mixnet:
EOF