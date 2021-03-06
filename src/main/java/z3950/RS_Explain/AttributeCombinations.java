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
import z3950.v3.AttributeSetId;

//================================================================
/**
 * Class for representing a <code>AttributeCombinations</code> from <code>RecordSyntax-explain</code>
 *
 * <pre>
 * AttributeCombinations ::=
 * SEQUENCE {
 *   defaultAttributeSet [0] IMPLICIT AttributeSetId
 *   legalCombinations [1] IMPLICIT SEQUENCE OF AttributeCombination
 * }
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class AttributeCombinations extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";

//----------------------------------------------------------------
/**
 * Default constructor for a AttributeCombinations.
 */

public
AttributeCombinations()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a AttributeCombinations from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
AttributeCombinations(BEREncoding ber, boolean check_tag)
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
  // AttributeCombinations should be encoded by a constructed BER

  BERConstructed ber_cons;
  try {
    ber_cons = (BERConstructed) ber;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun AttributeCombinations: bad BER form\n");
  }

  // Prepare to decode the components

  int num_parts = ber_cons.number_components();
  int part = 0;
  BEREncoding p;

  // Decoding: defaultAttributeSet [0] IMPLICIT AttributeSetId

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun AttributeCombinations: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 0 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun AttributeCombinations: bad tag in s_defaultAttributeSet\n");

  s_defaultAttributeSet = new AttributeSetId(p, false);
  part++;

  // Decoding: legalCombinations [1] IMPLICIT SEQUENCE OF AttributeCombination

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun AttributeCombinations: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 1 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun AttributeCombinations: bad tag in s_legalCombinations\n");

  try {
    BERConstructed cons = (BERConstructed) p;
    int parts = cons.number_components();
    s_legalCombinations = new AttributeCombination[parts];
    int n;
    for (n = 0; n < parts; n++) {
      s_legalCombinations[n] = new AttributeCombination(cons.elementAt(n), true);
    }
  } catch (ClassCastException e) {
    throw new ASN1EncodingException("Bad BER");
  }
  part++;

  // Should not be any more parts

  if (part < num_parts) {
    throw new ASN1Exception("Zebulun AttributeCombinations: bad BER: extra data " + part + "/" + num_parts + " processed");
  }
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of the AttributeCombinations.
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
 * Returns a BER encoding of AttributeCombinations, implicitly tagged.
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
  // Calculate the number of fields in the encoding

  int num_fields = 2; // number of mandatories

  // Encode it

  BEREncoding fields[] = new BEREncoding[num_fields];
  int x = 0;
  BEREncoding f2[];
  int p;

  // Encoding s_defaultAttributeSet: AttributeSetId 

  fields[x++] = s_defaultAttributeSet.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 0);

  // Encoding s_legalCombinations: SEQUENCE OF 

    f2 = new BEREncoding[s_legalCombinations.length];

    for (p = 0; p < s_legalCombinations.length; p++) {
      f2[p] = s_legalCombinations[p].ber_encode();
    }

    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 1, f2);

  return new BERConstructed(tag_type, tag, fields);
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the AttributeCombinations. 
 */

public String
toString()
{
  int p;
  StringBuffer str = new StringBuffer("{");
  int outputted = 0;

  str.append("defaultAttributeSet ");
  str.append(s_defaultAttributeSet);
  outputted++;

  if (0 < outputted)
    str.append(", ");
  str.append("legalCombinations ");
  str.append("{");
  for (p = 0; p < s_legalCombinations.length; p++) {
    if (p != 0)
      str.append(", ");
    str.append(s_legalCombinations[p]);
  }
  str.append("}");
  outputted++;

  str.append("}");

  return str.toString();
}

//----------------------------------------------------------------
/*
 * Internal variables for class.
 */

public AttributeSetId s_defaultAttributeSet;
public AttributeCombination s_legalCombinations[];

} // AttributeCombinations

//----------------------------------------------------------------
//EOF
