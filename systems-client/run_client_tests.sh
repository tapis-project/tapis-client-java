#!/bin/sh
# Use mvn to run tests for the systems client
#   against the systems service running either locally or in k8s
#   and other services (tenants, sk, auth, tokens) running in DEV, STAGING, or PROD
# The systems service must already be running.
# NOTE: For safety, for now do not allow running against PROD
# TODO If requested use docker to start up the systems service locally using an image
# TODO   - image used is based on TAPIS_RUN_ENV, for example tapis/systems:dev
#
# Use mvn to run the systems-client integration tests.
#
# The Tapis environment we are running in must be set to one of: dev, staging, prod
# TODO It can be set using env var TAPIS_RUN_ENV or by passing in as first arg, but not both.
#
# TODO To start up the systems service locally the following env variables must be set:
# TODO     TAPIS_DB_PASSWORD
# TODO     TAPIS_DB_JDBC_URL
# TODO     TAPIS_SERVICE_PASSWORD
#
# By default service listens on port 8080. To change it set
#     TAPIS_SERVICE_PORT
#
# To run the client integration tests the following env variables must be set:
#   TAPIS_FILES_SERVICE_PASSWORD - used for testing credential retrieval

PrgName=$(basename "$0")

USAGE1="Usage: $PrgName { local | k8s } { dev | staging }"
USAGE2="Usage: Run systems client tests against service running locally or in K8S."
#USAGE2="Usage: Set tapis run env by passing in or set using TAPIS_RUN_ENV, but not both"

# Check number of arguments
if [ $# -ne 2 ]; then
  echo "ERROR: Incorrect number of arguments"
  echo "$USAGE1"
  echo "$USAGE2"
  exit 1
fi
RUN_SVC=$1
RUN_ENV=$2
if [ -z "$RUN_ENV" ] && [ -z "$TAPIS_RUN_ENV" ]; then
  echo "ERROR: Unable to determine Tapis run env"
  echo "$USAGE1"
  echo "$USAGE2"
  exit 1
fi
if [ -n "$RUN_ENV" ] && [ -n "$TAPIS_RUN_ENV" ]; then
  echo "ERROR: Tapis run env set in TAPIS_RUN_ENV and passed in."
  echo "$USAGE1"
  echo "$USAGE2"
  exit 1
fi

if [ -n "$TAPIS_RUN_ENV" ]; then
  RUN_ENV=$TAPIS_RUN_ENV
fi

# Make target svc is valid
if [ "$RUN_SVC" != "local" ] && [ "$RUN_SVC" != "k8s" ]; then
  echo "ERROR: Invalid Tapis target svc location = $RUN_SVC"
  echo "$USAGE1"
  echo "$USAGE2"
  exit 1
fi

# Make sure run env is valid
if [ "$RUN_ENV" != "dev" ] && [ "$RUN_ENV" != "staging" ]; then
  echo "ERROR: Invalid Tapis run env = $RUN_ENV"
  echo "$USAGE1"
  echo "$USAGE2"
  exit 1
fi

# Make sure we have the files service password
# This is used for testing credential retrieval
if [ -z "$TAPIS_FILES_SERVICE_PASSWORD" ]; then
  echo "Please set env variable TAPIS_FILES_SERVICE_PASSWORD to the files service password"
  echo "$USAGE1"
  echo "$USAGE2"
  exit 1
fi

# Set base url for services we depend on (auth, tokens)
# NOTE: client test uses hard coded tenant name "dev"
if [ "$RUN_ENV" = "dev" ]; then
 export TAPIS_BASE_URL="https://dev.develop.tapis.io"
elif [ "$RUN_ENV" = "staging" ]; then
 export TAPIS_BASE_URL="https://dev.staging.tapis.io"
# elif [ "$RUN_ENV" = "prod" ]; then
# export TAPIS_BASE_URL="https://dev.tapis.io"
else
  echo "ERROR: Invalid Tapis run env = $RUN_ENV"
  echo "$USAGE1"
  echo "$USAGE2"
  exit 1
fi

# TODO If running against local service make sure we have env vars required
# TODO Make sure we have the service password, db password and db URL
# TODO if [ "$RUN_SVC" = "local" ]; then
# TODO  if [ -z "$TAPIS_SERVICE_PASSWORD" ]; then
# TODO    echo "ERROR: Please set env variable TAPIS_SERVICE_PASSWORD to the systems service password"
# TODO    echo $USAGE1
# TODO    exit 1
# TODO  fi
# TODO  if [ -z "$TAPIS_DB_PASSWORD" ]; then
# TODO    echo "ERROR: Please set env variable TAPIS_DB_PASSWORD"
# TODO    echo $USAGE1
# TODO    exit 1
# TODO  fi
# TODO  if [ -z "$TAPIS_DB_JDBC_URL" ]; then
# TODO    echo "ERROR: Please set env variable TAPIS_DB_JDBC_URL"
# TODO    echo $USAGE1
# TODO    exit 1
# TODO  fi
# TODO fi

# If running against k8s and not locally set an env
#   var to let the client test program know. Default is
#   to look for systems service locally.
if [ "$RUN_SVC" = "k8s" ]; then
  export TAPIS_SVC_URL_SYSTEMS="$TAPIS_BASE_URL"
fi

# Determine absolute path to location from which we are running
#  and change to that directory
export RUN_DIR=$(pwd)
export PRG_RELPATH=$(dirname "$0")
cd "$PRG_RELPATH"/. || exit
export PRG_PATH=$(pwd)

echo "****** Running client tests for Systmes service. Target service = $RUN_SVC, TAPIS_RUN_ENV = $RUN_ENV"

# if requested start up the systems service locally
# TODO if [ "$RUN_SVC" = "local" ]; then
# TODO  echo "Staring systems service locally"
# TODO  DOCK_RUN_ID=$(./docker_run_svc.sh "${RUN_ENV}")
# TODO  RET_CODE=$?
# TODO  if [ $RET_CODE -ne 0 ]; then
# TODO    echo "======================================================================"
# TODO    echo "Error starting Systems service locally."
# TODO    echo "Exiting ..."
# TODO    echo "======================================================================"
# TODO    exit $RET_CODE
# TODO  fi
# TODO 
# TODO  echo "++++++++++++++++++++++++++++++++++++++++++++++++"
# TODO  echo "Docker container ID: $DOCK_RUN_ID"
# TODO  echo "++++++++++++++++++++++++++++++++++++++++++++++++"
# TODO  echo "Pausing 5 seconds to allow container to start ... "
# TODO  sleep 5
# TODO  echo "++++++++++++++++++++++++++++++++++++++++++++++++"
# TODO  echo "DOCKER PS"
# TODO  echo "++++++++++++++++++++++++++++++++++++++++++++++++"
# TODO  docker ps --format "table {{.ID}}\t{{.Names}}\t{{.Image}}\t{{.RunningFor}}\t{{.Status}}\t{{.Ports}}"
# TODO  echo "++++++++++++++++++++++++++++++++++++++++++++++++"
# TODO  docker logs "$DOCK_RUN_ID"
# TODO  echo "++++++++++++++++++++++++++++++++++++++++++++++++"
# TODO  echo "Pausing 5 seconds to allow local service to start ... "
# TODO  sleep 5
# TODO  echo "++++++++++++++++++++++++++++++++++++++++++++++++"
# TODO  docker logs "$DOCK_RUN_ID"
# TODO  echo "++++++++++++++++++++++++++++++++++++++++++++++++"
# TODO fi

# Run the integration tests
echo "Running client integration tests"
mvn verify -DskipIntegrationTests=false
RET_CODE=$?

# If local then stop local systems service
# TODO if [ "$RUN_SVC" = "local" ]; then
# TODO  echo "Stopping local systems service using docker container ID: $DOCK_RUN_ID"
# TODO  docker stop "$DOCK_RUN_ID"
# TODO fi

if [ $RET_CODE -ne 0 ]; then
  echo "======================================================================"
  echo "ERROR: Test failures"
  echo "Exiting ..."
  echo "======================================================================"
  exit $RET_CODE
fi

# If it is a local run and we have required variables then cleanup DB artifacts
if [ "$RUN_SVC" = "local" -a  -n "$TAPIS_DB_PASSWORD" -a -n "$TAPIS_DB_JDBC_URL" ]; then
 echo "Removing test artifacts from DB"
 ./delete_client_test_data.sh
fi

cd $RUN_DIR
