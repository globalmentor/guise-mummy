package com.guiseframework.validator;

import static java.util.Collections.*;
import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.lang.Objects.*;

import java.util.*;

import com.garretwilson.iso.idcard.*;
import com.garretwilson.util.ArrayUtilities;

/**A validator to validate a Primary Account Number (PAN) of an identification card
	as defined in ISO/IEC 7812-1:2000(E),
	"Identification cards � Identification of issuers � Part 1: Numbering system".
The validator ensures that the PAN represents one of the allowed ID card products.
PANs for unknown products are considered invalid.
@see IDCard
*/
public class PANValidator extends AbstractValidator<PAN>	//TODO add http://www.darkcoding.net/index.php/credit-card-numbers/ reference
{

	/**The valid products bound property.*/
	public final static String VALID_PRODUCTS_PROPERTY=getPropertyName(PANValidator.class, "validProducts");

	/**The read-only set of valid products.*/
	private Set<Product> validProducts;

		/**@return The read-only set of valid products.*/
		public Set<Product> getValidProducts() {return validProducts;} 
		
		/**Sets the PAN products that the validator considers valid.
		This is a bound property.
		@param newValidProducts The set of valid products.
		@exception NullPointerException if the given set of valid products is <code>null</code>.
		@see #VALID_PRODUCTS_PROPERTY
		*/
		public void setValidProducts(final Set<Product> newValidProducts)
		{
			if(!validProducts.equals(checkInstance(newValidProducts, "Valid products cannot be null.")))	//if the value is really changing
			{
				final Set<Product> oldValidProducts=validProducts;	//get the current value
				this.validProducts=unmodifiableSet(new HashSet<Product>(newValidProducts));	//create a new unmodifiable copy set of valid products
				firePropertyChange(VALID_PRODUCTS_PROPERTY, oldValidProducts, validProducts);
			}
		}
	
		/**Sets the PAN products that the validator considers valid.
		This is a bound property.
		This implementation delegates to {@link #setValidProducts(Set)}
		@param validProducts The products that are allowed, if any.
		@exception NullPointerException if the given array of valid products is <code>null</code>.
		@see #VALID_PRODUCTS_PROPERTY
		*/
		public void setValidProducts(final Product... validProducts)
		{
			final Set<Product> validProductSet=new HashSet<Product>();	//create a new set of valid products
			addAll(validProductSet, checkInstance(validProducts, "Valid products cannot be null."));	//add all the specified valid products to our set
			setValidProducts(validProductSet);	//set the valid products
		}
	
	/**Valid products constructor with no value required.
	@param validProducts The products that are allowed, if any.
	@exception NullPointerException if the given array of valid products is <code>null</code>.
	*/
	public PANValidator(final Product... validProducts)
	{
		this(false, validProducts);	//construct the class without requiring a value
	}

	/**Value required constructor accepting all known products.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	*/
	public PANValidator(final boolean valueRequired)
	{
		this(valueRequired, Product.values());	//construct the class, accepting all known products
	}

	/**Value required and valid products constructor.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	@param validProducts The products that are allowed, if any.
	@exception NullPointerException if the given array of valid products is <code>null</code>.
	*/
	public PANValidator(final boolean valueRequired, final Product... validProducts)
	{
		super(valueRequired);	//construct the parent class
		final Set<Product> validProductSet=new HashSet<Product>();	//create a new set of valid products
		addAll(validProductSet, checkInstance(validProducts, "Valid products cannot be null."));	//add all the specified valid products to our set
		this.validProducts=unmodifiableSet(validProductSet);	//save the set of valid products
	}

	/**Checks whether a given value is valid, and throws an exception if not.
	This version determines whether a given PAN represents one of the supported products and is the correct length.
	@param value The value to validate.
	@exception ValidationException if the provided value is not valid.
	*/
	public void validate(final PAN value) throws ValidationException
	{
		super.validate(value);	//do the default validation
		if(value!=null)	//if the value passed default validation checks and isn't null
		{
			final Product product=IDCard.getProduct(value);	//get the product for this PAN
			if(product==null)	//if we don't know the product
			{
				throwInvalidValueValidationException(value);	//the PAN isn't valid to us TODO create specific message
			}
			if(!validProducts.contains(product))	//if this isn't a supported product
			{
				throwInvalidValueValidationException(value);	//this isn't one of the values we accept TODO create specific message
			}
			if(!ArrayUtilities.contains(product.getPANLengths(), value.toString().length()))	//if this product doesn't accept this PAN length
			{
				throwInvalidValueValidationException(value);	//indicate that the PAN is invalid for this product TODO create specific message
			}
		}
	}

}
