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

# Figure out which branch we are on so we can find the correct spec file
BRANCH_NAME=$(git rev-parse --abbrev-ref HEAD)
# If branch name is HEAD or empty as might be the case in a jenkins job then
#   set it to GIT_BRANCH. Jenkins jobs should have this set in the env.
if [ -z "$BRANCH_NAME" -o "x$BRANCH_NAME" = "xHEAD" ]; then
  BRANCH_NAME=$(echo "$GIT_BRANCH" | awk -F"/" '{print $2}')
fi
# Download latest openapi spec from repo
SPEC_FILE=https://raw.githubusercontent.com/tapis-project/tapis-java/${BRANCH_NAME}/tapis-systemsapi/src/main/resources/SystemsAPI.yaml
echo "Downloading openapi spec file: ${SPEC_FILE}"
curl -o target/SystemsAPI.yaml ${SPEC_FILE}
