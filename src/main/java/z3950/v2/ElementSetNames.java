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
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:14:26 UTC
 */

//----------------------------------------------------------------

package z3950.v2;
import asn1.*;


//================================================================
/**
 * Class for representing a <code>ElementSetNames</code> from <code>IR</code>
 *
 * <pre>
 * ElementSetNames ::=
 * [19] EXPLICIT CHOICE {
 *   generic [0] IMPLICIT ElementSetName
 *   databaseSpecific [1] IMPLICIT SEQUENCE OF ElementSetNames_databaseSpecific
 * }
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class ElementSetNames extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080314Z";

//----------------------------------------------------------------
/**
 * Default constructor for a ElementSetNames.
 */

public
ElementSetNames()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a ElementSetNames from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
ElementSetNames(BEREncoding ber, boolean check_tag)
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
  // Check tag matches

  if (check_tag) {
    if (ber.tag_get() != 19 ||
        ber.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
      throw new ASN1EncodingException
        ("Zebulun: ElementSetNames: bad BER: tag=" + ber.tag_get() + " expected 19\n");
  }

  // Unwrap explicit tag

  BERConstructed tagwrapper;
  try {
    tagwrapper = (BERConstructed) ber;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun ElementSetNames: bad BER tag form\n");
  }
  if (tagwrapper.number_components() != 1) 
    throw new ASN1EncodingException
      ("Zebulun ElementSetNames: bad BER tag form\n");
  ber = tagwrapper.elementAt(0);

  // Null out all choices

  c_generic = null;
  c_databaseSpecific = null;

  // Try choice generic
  if (ber.tag_get() == 0 &&
      ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    c_generic = new ElementSetName(ber, false);
    return;
  }

  // Try choice databaseSpecific
  if (ber.tag_get() == 1 &&
      ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    BEREncoding ber_data;
    ber_data = ber;
    BERConstructed ber_cons;
    try {
      ber_cons = (BERConstructed) ber_data;
    } catch (ClassCastException e) {
      throw new ASN1EncodingException
        ("Zebulun ElementSetNames: bad BER form\n");
    }

    int num_parts = ber_cons.number_components();
    int p;

    c_databaseSpecific = new ElementSetNames_databaseSpecific[num_parts];

    for (p = 0; p < num_parts; p++) {
      c_databaseSpecific[p] = new ElementSetNames_databaseSpecific(ber_cons.elementAt(p), true);
    }
    return;
  }

  throw new ASN1Exception("Zebulun ElementSetNames: bad BER encoding: choice not matched");
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of ElementSetNames.
 *
 * @return	The BER encoding.
 * @exception	ASN1Exception Invalid or cannot be encoded.
 */

public BEREncoding
ber_encode()
       throws ASN1Exception
{
  return ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 19);
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of ElementSetNames, implicitly tagged.
 *
 * @return	The BER encoding of the object.
 * @exception	ASN1Exception When invalid or cannot be encoded.
 */

public BEREncoding
ber_encode(int tag_type, int tag)
       throws ASN1Exception
{
  BEREncoding chosen = null;

  BEREncoding f2[];
  int p;
  // Encoding choice: c_generic
  if (c_generic != null) {
    chosen = c_generic.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 0);
  }

  // Encoding choice: c_databaseSpecific
  if (c_databaseSpecific != null) {
    if (chosen != null)
      throw new ASN1Exception("CHOICE multiply set");
    f2 = new BEREncoding[c_databaseSpecific.length];

    for (p = 0; p < c_databaseSpecific.length; p++) {
      f2[p] = c_databaseSpecific[p].ber_encode();
    }

    chosen = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 1, f2);
  }

  // Check for error of having none of the choices set
  if (chosen == null)
    throw new ASN1Exception("CHOICE not set");

  // Return chosen element wrapped in its explicit tag

  BEREncoding exp_tag_data[] = new BEREncoding[1];
  exp_tag_data[0] = chosen;
  return new BERConstructed(tag_type, tag, exp_tag_data);
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the ElementSetNames. 
 */

public String
toString()
{
  int p;
  StringBuffer str = new StringBuffer("{");

  boolean found = false;

  if (c_generic != null) {
    if (found)
      str.append("<ERROR: multiple CHOICE: generic> ");
    found = true;
    str.append("generic ");
  str.append(c_generic);
  }

  if (c_databaseSpecific != null) {
    if (found)
      str.append("<ERROR: multiple CHOICE: databaseSpecific> ");
    found = true;
    str.append("databaseSpecific ");
  str.append("{");
  for (p = 0; p < c_databaseSpecific.length; p++) {
    str.append(c_databaseSpecific[p]);
  }
  str.append("}");
  }

  str.append("}");

  return str.toString();
}

//----------------------------------------------------------------
/*
 * Internal variables for class.
 */

public ElementSetName c_generic;
public ElementSetNames_databaseSpecific c_databaseSpecific[];

} // ElementSetNames

//----------------------------------------------------------------
//EOF
