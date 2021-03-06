#!/bin/sh
PrgName=`basename $0`
# Determine absolute path to location from which we are running.
export RUN_DIR=`pwd`
export PRG_RELPATH=`dirname $0`
cd $PRG_RELPATH/.
export PRG_PATH=`pwd`
cd $RUN_DIR

# Create target dir in case not yet created by maven
mkdir -p $PRG_PATH/target

# Create unique directory in tmp for storing generated json file
TMP_DIR=$(mktemp -d)

# Download latest openapi spec from repo
# Dev yaml
curl -o target/openapi_v3.yml https://raw.githubusercontent.com/tapis-project/tokens-api/dev/service/resources/openapi_v3.yml

# Run swagger-cli from docker image to generate bundled json file from openapi yaml file
set -xv
export REPO=$PRG_PATH/target
export API_NAME=tokens.json
mkdir -p $REPO/swagger-api/out
# docker run -v $REPO/openapi_v3.yml:/swagger-api/yaml/openapi_v3.yml -v $REPO:/swagger-api/out \
#        	tapis/swagger-cli bundle -r /swagger-api/yaml/openapi_v3.yml -o /swagger-api/out/$API_NAME
docker run --rm -v $REPO/openapi_v3.yml:/swagger-api/yaml/openapi_v3.yml \
       	tapis/swagger-cli bundle -r /swagger-api/yaml/openapi_v3.yml > $TMP_DIR/$API_NAME
cp $TMP_DIR/$API_NAME $REPO/$API_NAME
rm -f $TMP_DIR/$API_NAME
rmdir $TMP_DIR
