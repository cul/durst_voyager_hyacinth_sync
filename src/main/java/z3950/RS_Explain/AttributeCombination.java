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
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:15:13 UTC
 */

//----------------------------------------------------------------

package z3950.RS_Explain;
import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;

//================================================================
/**
 * Class for representing a <code>AttributeCombination</code> from <code>RecordSyntax-explain</code>
 *
 * <pre>
 * AttributeCombination ::=
 * SEQUENCE OF AttributeOccurrence
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class AttributeCombination extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";

//----------------------------------------------------------------
/**
 * Default constructor for a AttributeCombination.
 */

public
AttributeCombination()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a AttributeCombination from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
AttributeCombination(BEREncoding ber, boolean check_tag)
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
  // AttributeCombination should be encoded by a constructed BER

  BERConstructed ber_cons;
  try {
    ber_cons = (BERConstructed) ber;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun AttributeCombination: bad BER form\n");
  }

  // Prepare to decode the components

  int num_parts = ber_cons.number_components();
  value = new AttributeOccurrence[num_parts];
  int p;
  for (p = 0; p < num_parts; p++) {
    value[p] = new AttributeOccurrence(ber_cons.elementAt(p), true);
  }
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of the AttributeCombination.
 *
 * @exception	ASN1Exception Invalid or cannot be encoded.
 * @return	The BER encoding.
 */

public BEREncoding
ber_encode()
       throws ASN1Exception
{
  return ber_encode(BEREncoding.UNIVERSAL_TAG, ASN1Sequence.TAG);
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of AttributeCombination, implicitly tagged.
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
  BEREncoding fields[] = new BERConstructed[value.length];
  int p;

  for (p = 0; p < value.length; p++) {
    fields[p] = value[p].ber_encode();
  }

  return new BERConstructed(tag_type, tag, fields);
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the AttributeCombination. 
 */

public String
toString()
{
  StringBuffer str = new StringBuffer("{");
  int p;

  for (p = 0; p < value.length; p++) {
    str.append(value[p]);
  }

  str.append("}");

  return str.toString();
}

//----------------------------------------------------------------
/*
 * Internal variables for class.
 */

public AttributeOccurrence value[];

} // AttributeCombination

//----------------------------------------------------------------
//EOF
