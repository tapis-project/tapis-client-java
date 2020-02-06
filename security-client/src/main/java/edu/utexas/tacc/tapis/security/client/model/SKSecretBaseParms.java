package edu.utexas.tacc.tapis.security.client.model;

import edu.utexas.tacc.tapis.shared.exceptions.TapisException;

/** The type parameter should be the type of the concrete child class
 * so that the fluent-style setters return the proper type.  
 * 
 * @author rcardone
 *
 * @param <T> the concrete child class
 */
@SuppressWarnings("unchecked")
public abstract class SKSecretBaseParms<T extends SKSecretBaseParms<T>> 
{
    // The first two parameters are typically URL 
    // path parameters, the rest are typically
    // query parameters.
    private final SecretType secretType;
    private String           secretName;
    private String           sysId;
    private String           sysOwner;
    private KeyType          keyType = KeyType.sshkey; // never null
    private String           dbHost;
    private String           dbName;
    private String           service;
        
    // Constructor.
    public SKSecretBaseParms(SecretType secretType) 
     throws TapisException
    {
        // Assign the secret type.
        if (secretType == null) {
            String msg =  "The secretType parameter cannot be null when creating "
                          + "an " + getClass().getSimpleName() + " object.";
            throw new TapisException(msg);
        }
        this.secretType = secretType;
    }
        
    // Accessors.  Fluent unchecked cast warnings are suppressed.
    public SecretType getSecretType() {return secretType;}
    public String getSecretName() {return secretName;}
    public T setSecretName(String secretName) 
        {this.secretName = secretName; return (T) this;}
    public String getSysId() {return sysId;}
    public T setSysId(String sysId) 
        {this.sysId = sysId; return (T) this;}
    public String getSysOwner() {return sysOwner;}
    public T setSysOwner(String sysOwner) 
        {this.sysOwner = sysOwner; return (T) this;}
    public KeyType getKeyType() {return keyType;}
    public T setKeyType(KeyType keyType) 
        {if (keyType != null) this.keyType = keyType; return (T) this;}
    public String getDbHost() {return dbHost;}
    public T setDbHost(String dbHost) 
        {this.dbHost = dbHost; return (T) this;}
    public String getDbName() {return dbName;}
    public T setDbName(String dbName) 
        {this.dbName = dbName; return (T) this;}
    public String getService() {return service;}
    public T setService(String service) 
        {this.service = service; return (T) this;}
}
