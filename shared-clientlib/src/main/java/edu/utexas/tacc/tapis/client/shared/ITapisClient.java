package edu.utexas.tacc.tapis.client.shared;

public interface ITapisClient 
{
    ITapisClient setBasePath(String path);
    ITapisClient addDefaultHeader(String key, String value);
    void close();
}
