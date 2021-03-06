package edu.utexas.tacc.tapis.client.shared.exceptions;

public class TException
 extends Exception 
{
	private static final long serialVersionUID = -2996383526058549742L;

	// TapisUtils.tapisify() requires that all subclasses implement constructors
	// with these exact signatures or guarantee that tapisify() is never called 
	// with subclasses that don't implement these two constructors.  See 
	// JobUtils.tapisify() for an example of how to implement non-conforming
	// exceptions that still can be tapisified.
	public TException(String message) {super(message);}
	public TException(String message, Throwable cause) {super(message, cause);}
}
