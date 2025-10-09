# tapis-client-java
Top level wrapper for all java client SDKs  

## OpenAPI specPath management

Across all sub-projects, we use the following variable to specify the path of a spec file:
`{module_name}SpecPath`
where `module_name` is the exact string before `"-client"` in any sub-project. 

For example, we use `-DjobsSpecPath=...` to pass spec path to jobs sub-project while we use `-DauthSpecPath=...` to pass the spec path for auth-client. 

Internally, we use a bash script named `gen_spec.sh` sitting in the root of this parent project to handle OpenAPI spec downloading, transformation, etc.

The first parameter of `gen_spec.sh` will take the `module_name`.
The second parameter of `gen_spec.sh` takes the raw spec path specified by `-D{module_name}SpecPath`. The value can be of the following two formats: 
    
    1. A valid and authentic HTTP URL that starts with `http`
    2. A file path to a spec file on your local disk, prefixed by `file://`
    3. Any other format will be considered as a local file in the file system where the building process is running, and the developer is responsible for the consequences of using a invalid spec path. 

The third parameter will take `"json"` or `"yaml"`, to determine the output spec format. 
The output of `gen_spec.sh` will be under the `target` directory of the corresponding sub-project, according to the `module_name` we pass as the second parameter of `gen_spec.sh`. 


To quickly compile the whole Java SDK project with different spec path specified for each project, we can do the following: 

```
mvn clean install -DskipTest \
-DappsSpecPath=https://raw.githubusercontent.com/tapis-project/openapi-apps/dev/AppsAPI.yaml \
-DauthSpecPath=https://raw.githubusercontent.com/tapis-project/authenticator/dev/service/resources/openapi_v3.yml \
-DfilesSpecPath=https://raw.githubusercontent.com/tapis-project/openapi-files/dev/FilesAPI.yaml \
-DglobusproxySpecPath=https://raw.githubusercontent.com/tapis-project/globus-proxy/dev/service/resources/openapi_v3.yml \
-DjobsSpecPath=https://raw.githubusercontent.com/tapis-project/openapi-jobs/dev/JobsAPI.yaml \
-DmetaSpecPath='${project.basedir}/src/main/resources/metav3-openapi.yaml' \
-DnotificationsSpecPath=https://raw.githubusercontent.com/tapis-project/openapi-notifications/dev/NotificationsAPI.yaml \
-DsecuritySpecPath=https://raw.githubusercontent.com/tapis-project/openapi-security/dev/SkAPI.yaml \
-DsystemsSpecPath=https://raw.githubusercontent.com/tapis-project/openapi-systems/dev/SystemsAPI.yaml \
-DtenantsSpecPath=https://raw.githubusercontent.com/tapis-project/tenants-api/dev/service/resources/openapi_v3.yml \
-DtokensSpecPath=https://raw.githubusercontent.com/tapis-project/tokens-api/dev/service/resources/openapi_v3.yml
```
If you omit any of the spec path here, the default one written in the sub-project `pom.xml` file will be used. 

If you only need to compile and install a single client or a few clients for testing purpose against a OpenAPI spec file on your disk, you can use `-pl` flag for the mvn command and append a list of submodules you want to compile and install. 

```
mvn clean install -pl jobs-client;files-client -am -DskipTest -DjobsSpecPath=file:///Users/wei.zhang/Developer/git/TAPIS/non-java/Openapi/openapi-jobs/JobsAPI.yaml
```

For details about `-pl` flag and `-am` flag, please refer to manual of `mvn` command. 


Also, for quick reference, the above command contains all the actual specs we use for SDK generation. Feel free to change any of them in the command line when you run the above command, but **DO NOT CHANGE THE DEFAULT ONES in the `pom.xml`**. 

## Build and Push the Java SDK into Maven Repo

After updating and pushing to github run the jenkins job  

> Tapis jobs -> DEV -> Client-Java-Build-Publish  

to build and push the latest snapshot version to the maven repo.
