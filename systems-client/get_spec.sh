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
# Download latest openapi spec from repo
# TODO: generalize for dev, staging and prod branches
# Dev yaml
curl -o target/SystemsAPI.yaml https://raw.githubusercontent.com/tapis-project/tapis-java/dev/tapis-systemsapi/src/main/resources/SystemsAPI.yaml
