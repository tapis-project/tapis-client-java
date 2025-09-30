#!/bin/sh
PrgName=`basename $0`
# Determine absolute path to location from which we are running.
export RUN_DIR=`pwd`
export PRG_RELPATH=`dirname $0`
cd $PRG_RELPATH/.
export PRG_PATH=`pwd`
cd $RUN_DIR

AUTH_SPEC_PATH=${1:-"https://raw.githubusercontent.com/tapis-project/authenticator/dev/service/resources/openapi_v3.yml"}	

# Create target dir in case not yet created by maven
mkdir -p $PRG_PATH/target

# Create unique directory in tmp for storing generated json file
TMP_DIR=$(mktemp -d)


# if specPath starts with 'http' then download it, if starts with 'file' then copy it, else exit with error
if [[ $AUTH_SPEC_PATH == http* ]]; then
	echo "Spec path is a URL, will download it: $AUTH_SPEC_PATH"
	# Download latest openapi spec from repo
	# Dev yaml
	curl -o target/openapi_v3.yml $AUTH_SPEC_PATH
elif [[ $AUTH_SPEC_PATH == file* ]]; then
	echo "Spec path is a file URL, will copy it: $AUTH_SPEC_PATH"
	AUTH_SPEC_PATH=$(echo $AUTH_SPEC_PATH | sed -e 's|^file://||')
	cp $AUTH_SPEC_PATH target/openapi_v3.yml
else
	# exit with error
	echo "Spec path is not a valid URL or file path: $AUTH_SPEC_PATH"
	exit 1
fi


# Run swagger-cli from docker image to generate bundled json file from openapi yaml file
set -xv
export REPO=$PRG_PATH/target
export API_NAME=auth.json
mkdir -p $REPO/swagger-api/out
docker run --rm -v $REPO/openapi_v3.yml:/swagger-api/yaml/openapi_v3.yml \
       	tapis/swagger-cli bundle -r /swagger-api/yaml/openapi_v3.yml > $TMP_DIR/$API_NAME
cp $TMP_DIR/$API_NAME $REPO/$API_NAME
rm -f $TMP_DIR/$API_NAME
rmdir $TMP_DIR
