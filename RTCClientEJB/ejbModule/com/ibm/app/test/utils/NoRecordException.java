package com.ibm.app.test.utils;

/**
 * Customized NoResultException used when DB gives no result 
 * 
 * Reason: javax.persistence.NoResultException extends java.lang.RuntimeException hence it can't be captured by EJB client; 
 *         A runtime exception does not have to be declared using a throws clause on a method signature hence not requiring 
 *         handling by the calling client. 
 *
 * #### The Java EE 6 Tutorial ####
 * https://docs.oracle.com/javaee/6/tutorial/doc/bnbpj.html
 * >>> Handling Exceptions
 * >>> The exceptions thrown by enterprise beans fall into two categories: system and application.
 * >>> 
 * >>> A system exception indicates a problem with the services that support an application. 
 * >>> For example, a connection to an external resource cannot be obtained, or an injected resource cannot be found. 
 * >>> If it encounters a system-level problem, your enterprise bean should throw a javax.ejb.EJBException. 
 * >>> Because the EJBException is a subclass of the RuntimeException, you do not have to specify it in the throws clause of the method declaration. 
 * >>> If a system exception is thrown, the EJB container might destroy the bean instance. 
 * >>> Therefore, a system exception cannot be handled by the bean�s client program, but instead requires intervention by a system administrator.
 * >>> 
 * >>> An application exception signals an error in the business logic of an enterprise bean. 
 * >>> Application exceptions are typically exceptions that you�ve coded yourself, such as the BookException thrown by the business methods of the CartBean example. 
 * >>> When an enterprise bean throws an application exception, the container does not wrap it in another exception. 
 * >>> The client should be able to handle any application exception it receives.
 * >>> If a system exception occurs within a transaction, the EJB container rolls back the transaction. 
 * >>> However, if an application exception is thrown within a transaction, the container does not roll back the transaction.
 * @author ruifengm
 * @since 2015-Dec-18
 */
public class NoRecordException extends Exception {
	private static final long serialVersionUID = 1L;
	
	private String message;

	public NoRecordException(String message) {
		super();
		this.message = message;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	} 
}
