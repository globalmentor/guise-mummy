package com.garretwilson.guise;

import java.net.URI;

import static com.garretwilson.text.CharacterConstants.*;

/**The singleton Guise class.
There will only be one instance of Guise per JVM.
@author Garret Wilson
*/
public class Guise
{

	/**The name of Guise.*/
	public final static String GUISE_NAME="Guise"+TRADE_MARK_SIGN_CHAR;

	/**The web address of Guise.*/
	public final static URI GUISE_WEB_URI=URI.create("http://www.javaguise.com/");

	/**The singleton instance of Guise.*/
	private static Guise instance=null;

	/**@return The singleton instance of Guise.*/
	public static Guise getInstance()
	{
		if(instance==null)	//if Guise has not yet been created
		{
			instance=new Guise();	//create a new Guise
		}
		return instance;
	}

	/**Whether this deployment of Guise is licensed.*/
	private final boolean licensed;

		/**@return Whether this deployment of Guise is licensed.*/
		public boolean isLicensed() {return licensed;}

	/**Private default constructor.
	@see #getInstance()
	*/
	private Guise()
	{
		licensed=getClass().getResource("license.properties")!=null;	//determine if Guise is licensed; for now we simply see if the license.properties file exists
	}
}
