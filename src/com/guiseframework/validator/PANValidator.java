package com.guiseframework.validator;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.util.CollectionUtilities.*;

import java.util.HashSet;
import java.util.Set;

import com.garretwilson.iso.idcard.IDCard;
import com.garretwilson.iso.idcard.PAN;
import com.garretwilson.iso.idcard.Product;
import com.garretwilson.util.ArrayUtilities;

/**A validator to validate a Primary Account Number (PAN) of an identification card
	as defined in ISO/IEC 7812-1:2000(E),
	"Identification cards — Identification of issuers — Part 1: Numbering system".
The validator ensures that the PAN represents one of the allowed ID card products.
PANs for unknown products are considered invalid.
@see IDCard
*/
public class PANValidator extends AbstractValidator<PAN>
{

	/**The set of valid products, thread-safe only for reading.*/
	private final Set<Product> validProducts=new HashSet<Product>();

	/**Valid products constructor with no value required.
	@param validProducts The products that are allowed.
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
	@param validProducts The products that are allowed.
	@exception NullPointerException if the given array of valid products is <code>null</code>.
	*/
	public PANValidator(final boolean valueRequired, final Product... validProducts)
	{
		super(valueRequired);	//construct the parent class
		addAll(this.validProducts, checkInstance(validProducts, "Valid products must be specified"));	//add all the specified valid products to our set
	}

	/**Determines whether a given PAN represents one of the supported products and is the correct length.
	This version delgates to the super class version to determine whether <code>null</code> values are allowed.
	@param value The value to validate.
	@return <code>true</code> if the PAN represents an allowed product and has the correct length.
	*/
	public boolean isValid(final PAN value)
	{
		if(!super.isValid(value))	//if the value doesn't pass the default checks
		{
			return false; //the PAN isn't valid
		}
		if(value!=null)	//if the value isn't null
		{
			final Product product=IDCard.getProduct(value);	//get the product for this PAN
			if(product==null)	//if we don't know the product
			{
				return false;	//the PAN isn't valid to us
			}
			if(!validProducts.contains(product))	//if this isn't a supported product
			{
				return false;	//this isn't one of the values we accept
			}
			if(!ArrayUtilities.contains(product.getPANLengths(), value.toString().length()))	//if this product doesn't accept this PAN length
			{
				return false;	//indicate that the PAN is invalid for this product
			}
		}
		return true;	//the PAN passed all tests
	}

}
