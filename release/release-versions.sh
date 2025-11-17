#!/bin/bash
# Script to determine specific versions for a release.
# Determine versions for: tapis-bom, tapis-client-java
# The service version should be passed in as the one and only argument

SVC_NAME=tapis-client-java

PrgName=$(basename "$0")

USAGE="Usage: $PrgName <svc_version>"

# Check number of arguments
if [ $# -ne 1 ]; then
  echo "$USAGE"
  exit 1
fi

SVC_VER=$1
  
# Determine absolute path to location from which we are running
#  and change to that directory.
export RUN_DIR=$(pwd)
export PRG_RELPATH=$(dirname "$0")
cd "$PRG_RELPATH"/. || exit
export PRG_PATH=$(pwd)

MVN_CACHE=~/.m2/repository/edu/utexas/tacc/tapis
VER_PREFIX="2.0."
RELEASE_PROP_FILE="release.properties"
BOM_NAME="tapis-bom"

BOM_DIR=${MVN_CACHE}/${BOM_NAME}

# Determine shared code versions
FILES=$(echo "${BOM_DIR}/${VER_PREFIX}*")
BOM_VER=$(ls -1 -d $FILES | tail -n 1 | xargs -n 1 basename)

# NOTE When this script is run, the version in the pom has already been updated to the next SNAPSHOT.
#      So instead we take it in as an argument 
# Determine service version
# SVC_VER=$(cd ..;mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

# Update release.properties file
echo "${SVC_NAME}=${SVC_VER}" > ${RELEASE_PROP_FILE}
echo "-----------------------------------------------------------------"
echo "${BOM_NAME}=${BOM_VER}" >> ${RELEASE_PROP_FILE}

# Make copies of openapi spec files used as part of the build.
# Create function to copy the openapi spec file into place.
# Function takes source spec file path as first argument and destination spec file name as second argument
# Logs warning if source file does not exist
cp_spec_file () {
  SRC_SPEC_PATH=$1
  DST_SPEC_FILE=$2
  if [  -f $SRC_SPEC_PATH ]; then
    cp $SRC_SPEC_PATH ./openapi_specs/${DST_SPEC_FILE}
  else
    echo "WARNING: For destination spec $DST_SPEC_FILE unable to find source openapi spec file at path: $SRC_SPEC_PATH"
  fi
}

# Copy yaml files associated with java based services. These typically come directly from the
# respective git repositories (e.g., tapis-project/openapi-systems/SystemsAPI.yaml)

cp_spec_file "../apps-client/target/openapi_v3.yml"          "AppsAPI.yaml"
cp_spec_file "../files-client/target/openapi_v3.yml"         "FilesAPI.yaml"
cp_spec_file "../jobs-client/target/openapi_v3.yml"          "JobsAPI.yaml"
cp_spec_file "../meta-client/target/openapi_v3.yml"          "MetaAPI.yaml"
cp_spec_file "../notifications-client/target/openapi_v3.yml" "NotificationsAPI.yaml"
cp_spec_file "../security-client/target/openapi_v3.yml"      "SkAPI.yaml"
cp_spec_file "../systems-client/target/openapi_v3.yml"       "SystemsAPI.yaml"

# Copy json files associated with other services. These have been converted from the original yml files
#   in the respective git repositories (e.g. tapis-project/tenants-api/dev/service/resources/openapi_v3.yml)
cp_spec_file "../auth-client/target/openapi_v3.json" "AuthAPI.json"
cp_spec_file "../globusproxy-client/target/openapi_v3.json" "GlobusProxyAPI.json"
cp_spec_file "../tenants-client/target/openapi_v3.json" "TenantsAPI.json"
cp_spec_file "../tokens-client/target/openapi_v3.json" "TokensAPI.json"
