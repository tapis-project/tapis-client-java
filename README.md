# tapis-client-java
Top level wrapper for all java client SDKs  

## OpenAPI specPath management

Across all sub-projects, we use the following variable to specify the path of a spec file:
`{module_name}SpecPath`
where `module_name` is the exact string before `"-client"` in any sub-project. 

For example, we use `-DjobsSpecPath=...` to pass spec path to jobs sub-project while we use `-DauthSpecPath=...` to pass the spec path for auth-client. 

Internally, we use a bash script named gen_spec.sh sitting in the root of this parent project to handle OpenAPI spec downloading, transformation, etc. 
The first parameter of `gen_spec.sh` takes the raw spec path specified by `-D{module_name}SpecPath`. The value can be of the following two formats: 
    
    1. A valid and authentic HTTP URL that starts with `http`
    2. A file path to a spec file on your local disk, prefixed by `files://`
    3. Any other format will be considered as a local file in the file system where the building process is running, and the developer is responsible for the consequences of using a invalid spec path. 

The second parameter of `gen_spec.sh` will take the `module_name`, and the third parameter will take `"json"` or `"yaml"`, to determine the output spec format. 
The output of `gen_spec.sh` will be under the `target` directory of the corresponding sub-project, according to the `module_name` we pass as the second parameter of `gen_spec.sh`. 

## Build and Push the Java SDK into Maven Repo

After updating and pushing to github run the jenkins job  

> Tapis jobs -> DEV -> Client-Java-Build-Publish  

to build and push the latest snapshot version to the maven repo.
