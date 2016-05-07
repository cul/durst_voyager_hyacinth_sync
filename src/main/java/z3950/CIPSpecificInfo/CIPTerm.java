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
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:20:29 UTC
 */

//----------------------------------------------------------------

package z3950.CIPSpecificInfo;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.BEREncoding;

//================================================================
/**
 * Class for representing a <code>CIPTerm</code> from <code>CIP-Release-B-APDU</code>
 *
 * <pre>
 * CIPTerm ::=
 * CHOICE {
 *   wrsgrs [1] IMPLICIT WRSGRSSpatialCoverage
 *   circle [2] IMPLICIT Circle
 *   temporalPeriod [3] IMPLICIT TemporalPeriod
 * }
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class CIPTerm extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080320Z";

//----------------------------------------------------------------
/**
 * Default constructor for a CIPTerm.
 */

public
CIPTerm()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a CIPTerm from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
CIPTerm(BEREncoding ber, boolean check_tag)
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
  // Null out all choices

  c_wrsgrs = null;
  c_circle = null;
  c_temporalPeriod = null;

  // Try choice wrsgrs
  if (ber.tag_get() == 1 &&
      ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    c_wrsgrs = new WRSGRSSpatialCoverage(ber, false);
    return;
  }

  // Try choice circle
  if (ber.tag_get() == 2 &&
      ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    c_circle = new Circle(ber, false);
    return;
  }

  // Try choice temporalPeriod
  if (ber.tag_get() == 3 &&
      ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    c_temporalPeriod = new TemporalPeriod(ber, false);
    return;
  }

  throw new ASN1Exception("Zebulun CIPTerm: bad BER encoding: choice not matched");
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of CIPTerm.
 *
 * @return	The BER encoding.
 * @exception	ASN1Exception Invalid or cannot be encoded.
 */

public BEREncoding
ber_encode()
       throws ASN1Exception
{
  BEREncoding chosen = null;

  // Encoding choice: c_wrsgrs
  if (c_wrsgrs != null) {
    chosen = c_wrsgrs.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);
  }

  // Encoding choice: c_circle
  if (c_circle != null) {
    if (chosen != null)
      throw new ASN1Exception("CHOICE multiply set");
    chosen = c_circle.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
  }

  // Encoding choice: c_temporalPeriod
  if (c_temporalPeriod != null) {
    if (chosen != null)
      throw new ASN1Exception("CHOICE multiply set");
    chosen = c_temporalPeriod.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);
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

  throw new ASN1EncodingException("Zebulun CIPTerm: cannot implicitly tag");
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the CIPTerm. 
 */

public String
toString()
{
  StringBuffer str = new StringBuffer("{");

  boolean found = false;

  if (c_wrsgrs != null) {
    if (found)
      str.append("<ERROR: multiple CHOICE: wrsgrs> ");
    found = true;
    str.append("wrsgrs ");
  str.append(c_wrsgrs);
  }

  if (c_circle != null) {
    if (found)
      str.append("<ERROR: multiple CHOICE: circle> ");
    found = true;
    str.append("circle ");
  str.append(c_circle);
  }

  if (c_temporalPeriod != null) {
    if (found)
      str.append("<ERROR: multiple CHOICE: temporalPeriod> ");
    found = true;
    str.append("temporalPeriod ");
  str.append(c_temporalPeriod);
  }

  str.append("}");

  return str.toString();
}

//----------------------------------------------------------------
/*
 * Internal variables for class.
 */

public WRSGRSSpatialCoverage c_wrsgrs;
public Circle c_circle;
public TemporalPeriod c_temporalPeriod;

} // CIPTerm

//----------------------------------------------------------------
//EOF
