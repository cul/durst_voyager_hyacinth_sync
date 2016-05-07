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
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:15:06 UTC
 */

//----------------------------------------------------------------

package z3950.v3;
import asn1.ASN1Any;
import asn1.ASN1Exception;
import asn1.ASN1Integer;
import asn1.BEREncoding;


//================================================================
/**
 * Class for representing a <code>KnownProximityUnit</code> from <code>Z39-50-APDU-1995</code>
 *
 * <pre>
 * KnownProximityUnit ::=
 * INTEGER
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class KnownProximityUnit extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";

//----------------------------------------------------------------
/**
 * Default constructor for a KnownProximityUnit.
 */

public
KnownProximityUnit()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a KnownProximityUnit from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
KnownProximityUnit(BEREncoding ber, boolean check_tag)
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
  value = new ASN1Integer(ber, check_tag);
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of the KnownProximityUnit.
 *
 * @exception	ASN1Exception Invalid or cannot be encoded.
 * @return	The BER encoding.
 */

public BEREncoding
ber_encode()
       throws ASN1Exception
{
  return value.ber_encode();
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of KnownProximityUnit, implicitly tagged.
 *
 * @param tag_type	The type of the implicit tag.
 * @param tag	The implicit tag.
 * @return	The BER encoding of the object.
 * @exception	ASN1Exception When invalid or cannot be encoded.
 * @see asn1.BEREncoding#UNIVERSAL_TAG
 * @see asn1.BEREncoding#APPLICATION_TAG
 * @see asn1.BEREncoding#CONTEXT_SPECIFIC_TAG
 * @see asn1.BEREncoding#PRIVATE_TAG
 */

public BEREncoding
ber_encode(int tag_type, int tag)
       throws ASN1Exception
{
  return value.ber_encode(tag_type, tag);
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the KnownProximityUnit. 
 */

public String
toString()
{
  return value.toString();
}

//----------------------------------------------------------------
/*
 * Internal variables for class.
 */

public ASN1Integer value;

//----------------------------------------------------------------
/*
 * Enumerated constants for class.
 */

public static final int E_character = 1;
public static final int E_word = 2;
public static final int E_sentence = 3;
public static final int E_paragraph = 4;
public static final int E_section = 5;
public static final int E_chapter = 6;
public static final int E_document = 7;
public static final int E_element = 8;
public static final int E_subelement = 9;
public static final int E_elementType = 10;
public static final int E_byte = 11;

} // KnownProximityUnit

//----------------------------------------------------------------
//EOF
