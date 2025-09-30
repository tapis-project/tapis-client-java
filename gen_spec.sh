#!/bin/sh
PrgName=`basename $0`
# Determine absolute path to location from which we are running.
export RUN_DIR=`pwd`
export PRG_RELPATH=`dirname $0`
cd $PRG_RELPATH/.
export PRG_PATH=`pwd`
cd $RUN_DIR

MODULE_NAME=${1}
SPEC_PATH=${2:-"https://raw.githubusercontent.com/tapis-project/authenticator/dev/service/resources/openapi_v3.yml"} 
OUTPUT_FORMAT=${3:-"yaml"} # can be "yaml" or "json"

export OUTPUT_DIR=$PRG_PATH/${MODULE_NAME}-client/target
mkdir -p $OUTPUT_DIR

TMP_DIR=$(mktemp -d)
mkdir -p $TMP_DIR/${MODULE_NAME}

INPUT_FORMAT=${SPEC_PATH##*.}
TMP_SPEC_FILE=$TMP_DIR/${MODULE_NAME}/openapi_v3.${INPUT_FORMAT}
OUTPUT_API_NAME=openapi_v3.${OUTPUT_FORMAT}

# if specPath starts with 'http' then download it, if starts with 'file://' then copy it, else exit with error
if [[ $SPEC_PATH == http* ]]; then
	echo "Spec path is a URL, will download it: $SPEC_PATH"
	# Download latest openapi spec from repo
	# Dev yaml
	curl -o $TMP_SPEC_FILE $SPEC_PATH
elif [[ $SPEC_PATH == file://* ]]; then
	echo "Spec path is a file URL, will copy it: $SPEC_PATH"
	SPEC_PATH=$(echo $SPEC_PATH | sed -e 's|^file://||')
	cp -f $SPEC_PATH $TMP_SPEC_FILE
else
	# exit with error
	echo "Spec path is not a valid URL or file path: $SPEC_PATH"
    echo "Proceed as taking in a bare local file path"
    if [ -f "$SPEC_PATH" ]; then
        cp -f $SPEC_PATH $TMP_SPEC_FILE
        echo "Copied raw local file to $TMP_SPEC_FILE"
    else
        echo "Error: Local file does not exist: $SPEC_PATH"
        exit 1
    fi
fi

# Convert to desired output format if needed
if [[ ($INPUT_FORMAT == "yaml" || $INPUT_FORMAT == "yml") && $OUTPUT_FORMAT == "json" ]]; then
    echo "Converting from $INPUT_FORMAT to $OUTPUT_FORMAT using swagger-cli..."
    set -xv
    docker run --rm -v "$TMP_SPEC_FILE:/swagger-api/input/openapi_v3.$INPUT_FORMAT" \
            tapis/swagger-cli bundle -r "/swagger-api/input/openapi_v3.$INPUT_FORMAT" > "$TMP_DIR/${MODULE_NAME}/$OUTPUT_API_NAME"
    cp "$TMP_DIR/${MODULE_NAME}/$OUTPUT_API_NAME" "$OUTPUT_DIR/$OUTPUT_API_NAME"
    rm -rf "$TMP_DIR"
    exit 0
elif [[ $INPUT_FORMAT == "json" && ($OUTPUT_FORMAT == "yaml" || $OUTPUT_FORMAT == "yml") ]]; then
    echo "Converting from $INPUT_FORMAT to $OUTPUT_FORMAT using swagger-cli..."
    set -xv
    docker run --rm -v "$TMP_SPEC_FILE:/swagger-api/input/openapi_v3.$INPUT_FORMAT" \
            tapis/swagger-cli bundle -r "/swagger-api/input/openapi_v3.$INPUT_FORMAT" --type yaml > "$TMP_DIR/${MODULE_NAME}/$OUTPUT_API_NAME"
    cp "$TMP_DIR/${MODULE_NAME}/$OUTPUT_API_NAME" "$OUTPUT_DIR/$OUTPUT_API_NAME"
    rm -rf "$TMP_DIR"
    exit 0
else
    echo "No conversion needed, just copy it to output dir"
    cp -f $TMP_SPEC_FILE "$OUTPUT_DIR/$OUTPUT_API_NAME"
    rm -rf "$TMP_DIR"
    exit 0
fi

# If we reach here, the conversion is not supported
echo "Error: Unsupported conversion from $INPUT_FORMAT to $OUTPUT_FORMAT"
echo "Supported conversions: yaml<->json, yml<->json"
exit 1

