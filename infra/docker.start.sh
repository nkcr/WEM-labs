#!/usr/bin/env bash
docker run --name my_solr -d -p 8983:8983 -t --rm solr
sleep 5
docker exec my_solr bin/solr create -c localch