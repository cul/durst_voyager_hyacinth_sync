/*
 * $Source$
 * $Date$
 * $Revision$
 *
 * Copyright (C) 1998, Hoylen Sue.  All Rights Reserved.
 * <h.sue@ieee.org>
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  Refer to
 * the supplied license for more details.
 *
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:15:04 UTC
 */

//----------------------------------------------------------------

package z3950.v3;
import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1External;
import asn1.ASN1OctetString;
import asn1.BERConstructed;
import asn1.BEREncoding;


//================================================================
/**
 * Class for representing a <code>AccessControlResponse_securityChallengeResponse</code> from <code>Z39-50-APDU-1995</code>
 *
 * <pre>
 * AccessControlResponse_securityChallengeResponse ::=
 * CHOICE {
 *   simpleForm [38] IMPLICIT OCTET STRING
 *   externallyDefined [0] EXPLICIT EXTERNAL
 * }
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class AccessControlResponse_securityChallengeResponse extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";

//----------------------------------------------------------------
/**
 * Default constructor for a AccessControlResponse_securityChallengeResponse.
 */

public
AccessControlResponse_securityChallengeResponse()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a AccessControlResponse_securityChallengeResponse from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
AccessControlResponse_securityChallengeResponse(BEREncoding ber, boolean check_tag)
       throws ASN1Exception
{
  super(ber, check_tag);
}

//----------------------------------------------------------------
/**
 * Initializing object from a BER encoding.
 * This method is for internal use only. You should use
 * the constructor that takes a BEREncoding.
 *
 * @param ber the BER to decode.
 * @param check_tag if the tag should be checked.
 * @exception ASN1Exception if the BER encoding is bad.
 */

public void
ber_decode(BEREncoding ber, boolean check_tag)
       throws ASN1Exception
{
  BERConstructed tagwrapper;

  // Null out all choices

  c_simpleForm = null;
  c_externallyDefined = null;

  // Try choice simpleForm
  if (ber.tag_get() == 38 &&
      ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    c_simpleForm = new ASN1OctetString(ber, false);
    return;
  }

  // Try choice externallyDefined
  if (ber.tag_get() == 0 &&
      ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      tagwrapper = (BERConstructed) ber;
    } catch (ClassCastException e) {
      throw new ASN1EncodingException
        ("Zebulun AccessControlResponse_securityChallengeResponse: bad BER form\n");
    }
    if (tagwrapper.number_components() != 1)
      throw new ASN1EncodingException
        ("Zebulun AccessControlResponse_securityChallengeResponse: bad BER form\n");
    c_externallyDefined = new ASN1External(tagwrapper.elementAt(0), true);
    return;
  }

  throw new ASN1Exception("Zebulun AccessControlResponse_securityChallengeResponse: bad BER encoding: choice not matched");
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of AccessControlResponse_securityChallengeResponse.
 *
 * @return	The BER encoding.
 * @exception	ASN1Exception Invalid or cannot be encoded.
 */

public BEREncoding
ber_encode()
       throws ASN1Exception
{
  BEREncoding chosen = null;

  BEREncoding enc[];

  // Encoding choice: c_simpleForm
  if (c_simpleForm != null) {
    chosen = c_simpleForm.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 38);
  }

  // Encoding choice: c_externallyDefined
  if (c_externallyDefined != null) {
    if (chosen != null)
      throw new ASN1Exception("CHOICE multiply set");
    enc = new BEREncoding[1];
    enc[0] = c_externallyDefined.ber_encode();
    chosen = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 0, enc);
  }

  // Check for error of having none of the choices set
  if (chosen == null)
    throw new ASN1Exception("CHOICE not set");

  return chosen;
}

//----------------------------------------------------------------

/**
 * Generating a BER encoding of the object
 * and implicitly tagging it.
 * <p>
 * This method is for internal use only. You should use
 * the ber_encode method that does not take a parameter.
 * <p>
 * This function should never be used, because this
 * production is a CHOICE.
 * It must never have an implicit tag.
 * <p>
 * An exception will be thrown if it is called.
 *
 * @param tag_type the type of the tag.
 * @param tag the tag.
 * @exception ASN1Exception if it cannot be BER encoded.
 */

public BEREncoding
ber_encode(int tag_type, int tag)
       throws ASN1Exception
{
  // This method must not be called!

  // Method is not available because this is a basic CHOICE
  // which does not have an explicit tag on it. So it is not
  // permitted to allow something else to apply an implicit
  // tag on it, otherwise the tag identifying which CHOICE
  // it is will be overwritten and lost.

  throw new ASN1EncodingException("Zebulun AccessControlResponse_securityChallengeResponse: cannot implicitly tag");
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the AccessControlResponse_securityChallengeResponse. 
 */

public String
toString()
{
  StringBuffer str = new StringBuffer("{");

  boolean found = false;

  if (c_simpleForm != null) {
    if (found)
      str.append("<ERROR: multiple CHOICE: simpleForm> ");
    found = true;
    str.append("simpleForm ");
  str.append(c_simpleForm);
  }

  if (c_externallyDefined != null) {
    if (found)
      str.append("<ERROR: multiple CHOICE: externallyDefined> ");
    found = true;
    str.append("externallyDefined ");
  str.append(c_externallyDefined);
  }

  str.append("}");

  return str.toString();
}

//----------------------------------------------------------------
/*
 * Internal variables for class.
 */

public ASN1OctetString c_simpleForm;
public ASN1External c_externallyDefined;

} // AccessControlResponse_securityChallengeResponse

//----------------------------------------------------------------
//EOF
