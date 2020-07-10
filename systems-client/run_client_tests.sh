#!/bin/sh
# Use docker and mvn to run tests for the systems client
#   against the systems service running locally and the other
#   services (tenants, sk, auth, tokens) running in DEV, STAGING, or PROD
# NOTE: For safety, for now do not allow running against PROD
# Use docker to start up the systems service locally using an image
#   Image used is based on TAPIS_RUN_ENV
# Use mvn to run the systems-client integration tests.
#
# The Tapis environment we are running in must be set to one of: dev, staging, prod
# It can be set using env var TAPIS_RUN_ENV or by passing in as first arg, but not both.
#
# To start up the systems service locally the following env variables must be set:
#     TAPIS_DB_PASSWORD
#     TAPIS_DB_JDBC_URL
#     TAPIS_SERVICE_PASSWORD
#
# To run the client integration tests the following env variables must be set:
#   TAPIS_FILES_SVC_PASSWORD - used for testing credential retrieval

PrgName=$(basename "$0")

USAGE1="Usage: $PrgName [ { dev staging } ]"
USAGE2="Usage: Set tapis run env by passing in or set using TAPIS_RUN_ENV, but not both"

# Check number of arguments
if [ $# -gt 1 ]; then
  echo "ERROR: Incorrect number of arguments"
  echo $USAGE1
  echo $USAGE2
  exit 1
fi

if [ $# -eq 0 -a -z "$TAPIS_RUN_ENV" ]; then
  echo "ERROR: Unable to determine Tapis run env"
  echo $USAGE1
  echo $USAGE2
  exit 1
fi
if [ $# -eq 1 -a -n "$TAPIS_RUN_ENV" ]; then
  echo "ERROR: Tapis run env set in TAPIS_RUN_ENV and passed in."
  echo $USAGE1
  echo $USAGE2
  exit 1
fi

RUN_ENV=$1
if [ -n "$TAPIS_RUN_ENV" ]; then
  RUN_ENV=$TAPIS_RUN_ENV
fi

# Make sure run env is valid
if [ "$RUN_ENV" != "dev" -a "$RUN_ENV" != "staging" ]; then
  echo "ERROR: Invalid Tapis run env = $RUN_ENV"
  echo $USAGE1
  echo $USAGE2
  exit 1
fi

# Make sure we have the files service password
# This is used for testing credential retrieval
if [ -z "$TAPIS_FILES_SVC_PASSWORD" ]; then
  echo "Please set env variable TAPIS_FILES_SVC_PASSWORD to the files service password"
  echo $USAGE1
  echo $USAGE2
  exit 1
fi

# Set base url for services we depend on (auth, tokens)
# NOTE: client test uses hard coded tenant name "dev"
if [ "$RUN_ENV" = "dev" ]; then
 TAPIS_BASE_URL="https://dev.develop.tapis.io"
elif [ "$RUN_ENV" = "staging" ]; then
 TAPIS_BASE_URL="https://dev.staging.tapis.io"
# elif [ "$RUN_ENV" = "prod" ]; then
#  TAPIS_BASE_URL="https://dev.tapis.io"
else
  echo "ERROR: Invalid Tapis run env = $RUN_ENV"
  echo $USAGE1
  echo $USAGE2
  exit 1
fi


# Determine absolute path to location from which we are running
#  and change to that directory
export RUN_DIR=$(pwd)
export PRG_RELPATH=$(dirname "$0")
cd "$PRG_RELPATH"/. || exit
export PRG_PATH=$(pwd)

# Start up the systems service locally
echo "Staring systems service locally"
DOCK_RUN_ID=`./docker_run_sys_svc.sh ${RUN_ENV}`
RET_CODE=$?
if [ $RET_CODE -ne 0 ]; then
  echo "======================================================================"
  echo "Error starting Systems service locally."
  echo "Exiting ..."
  echo "======================================================================"
  exit $RET_CODE
fi

# Run the integration tests
echo "Running client integration tests"
mvn verify -DskipIntegrationTests=false
RET_CODE=$?
if [ $RET_CODE -ne 0 ]; then
  echo "======================================================================"
  echo "ERROR: Test failures"
  echo "Exiting ..."
  echo "======================================================================"
  exit $RET_CODE
fi

# Cleanup DB artifacts
echo "Removing test artifacts from DB"
./delete_client_test_data.sh

# Stop local systems service
echo "Stopping local systems service using docker container ID: $DOCK_RUN_ID"
docker stop $DOCK_RUN_ID
