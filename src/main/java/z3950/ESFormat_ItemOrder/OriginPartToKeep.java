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
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:15:26 UTC
 */

//----------------------------------------------------------------

package z3950.ESFormat_ItemOrder;
import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1External;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;

//================================================================
/**
 * Class for representing a <code>OriginPartToKeep</code> from <code>ESFormat-ItemOrder</code>
 *
 * <pre>
 * OriginPartToKeep ::=
 * SEQUENCE {
 *   supplDescription [1] IMPLICIT EXTERNAL OPTIONAL
 *   contact [2] IMPLICIT OriginPartToKeep_contact OPTIONAL
 *   addlBilling [3] IMPLICIT OriginPartToKeep_addlBilling OPTIONAL
 * }
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class OriginPartToKeep extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";

//----------------------------------------------------------------
/**
 * Default constructor for a OriginPartToKeep.
 */

public
OriginPartToKeep()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a OriginPartToKeep from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
OriginPartToKeep(BEREncoding ber, boolean check_tag)
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
  // OriginPartToKeep should be encoded by a constructed BER

  BERConstructed ber_cons;
  try {
    ber_cons = (BERConstructed) ber;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun OriginPartToKeep: bad BER form\n");
  }

  // Prepare to decode the components

  int num_parts = ber_cons.number_components();
  int part = 0;
  BEREncoding p;

  // Remaining elements are optional, set variables
  // to null (not present) so can return at end of BER

  s_supplDescription = null;
  s_contact = null;
  s_addlBilling = null;

  // Decoding: supplDescription [1] IMPLICIT EXTERNAL OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 1 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_supplDescription = new ASN1External(p, false);
    part++;
  }

  // Decoding: contact [2] IMPLICIT OriginPartToKeep_contact OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 2 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_contact = new OriginPartToKeep_contact(p, false);
    part++;
  }

  // Decoding: addlBilling [3] IMPLICIT OriginPartToKeep_addlBilling OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 3 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_addlBilling = new OriginPartToKeep_addlBilling(p, false);
    part++;
  }

  // Should not be any more parts

  if (part < num_parts) {
    throw new ASN1Exception("Zebulun OriginPartToKeep: bad BER: extra data " + part + "/" + num_parts + " processed");
  }
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of the OriginPartToKeep.
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
 * Returns a BER encoding of OriginPartToKeep, implicitly tagged.
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

  int num_fields = 0; // number of mandatories
  if (s_supplDescription != null)
    num_fields++;
  if (s_contact != null)
    num_fields++;
  if (s_addlBilling != null)
    num_fields++;

  // Encode it

  BEREncoding fields[] = new BEREncoding[num_fields];
  int x = 0;

  // Encoding s_supplDescription: EXTERNAL OPTIONAL

  if (s_supplDescription != null) {
    fields[x++] = s_supplDescription.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);
  }

  // Encoding s_contact: OriginPartToKeep_contact OPTIONAL

  if (s_contact != null) {
    fields[x++] = s_contact.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
  }

  // Encoding s_addlBilling: OriginPartToKeep_addlBilling OPTIONAL

  if (s_addlBilling != null) {
    fields[x++] = s_addlBilling.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);
  }

  return new BERConstructed(tag_type, tag, fields);
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the OriginPartToKeep. 
 */

public String
toString()
{
  StringBuffer str = new StringBuffer("{");
  int outputted = 0;

  if (s_supplDescription != null) {
    str.append("supplDescription ");
    str.append(s_supplDescription);
    outputted++;
  }

  if (s_contact != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("contact ");
    str.append(s_contact);
    outputted++;
  }

  if (s_addlBilling != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("addlBilling ");
    str.append(s_addlBilling);
    outputted++;
  }

  str.append("}");

  return str.toString();
}

//----------------------------------------------------------------
/*
 * Internal variables for class.
 */

public ASN1External s_supplDescription; // optional
public OriginPartToKeep_contact s_contact; // optional
public OriginPartToKeep_addlBilling s_addlBilling; // optional

} // OriginPartToKeep

//----------------------------------------------------------------
//EOF
