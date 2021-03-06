/*
 * Tapis Meta V3 API
 * The Tapis Meta API provides access to a MongoDB databas. A standalone service which connects to a MongoDB database and immediately exposes all of MongoDB’s capabilities through a complete REST API, which allows the user to read and write JSON messages and binary data via HTTP.
 *
 * The version of the OpenAPI document: 0.1
 * Contact: cicsupport@tacc.utexas.edu
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package edu.utexas.tacc.tapis.meta.client.gen.api;

import org.junit.Ignore;
import org.junit.Test;
import edu.utexas.tacc.tapis.meta.client.gen.ApiException;

import java.util.List;

/**
 * API tests for DbApi
 */
@Ignore
public class DbApiTest {

    private final DbApi api = new DbApi();

    
    /**
     * createDB
     *
     * Create the database.
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void createDBTest() throws ApiException {
        String db = null;
        Boolean np = null;
        api.createDB(db);

        // TODO: test validations
    }
    
    /**
     * deleteDB
     *
     * Delete a database.
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void deleteDBTest() throws ApiException {
        String db = null;
        Boolean np = null;
        // api.deleteDB(db, np);

        // TODO: test validations
    }
    
    /**
     * listCollectionNames
     *
     * List the names of all collections in the database.
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void listCollectionNamesTest() throws ApiException {
        String db = null;
        Boolean np = null;
        List<String> response = api.listCollectionNames(db);

        // TODO: test validations
    }
    
}
