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
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:20:30 UTC
 */

//----------------------------------------------------------------

package z3950.ESFormat_CIPOrder;
import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;
import z3950.v3.InternationalString;

//================================================================
/**
 * Class for representing a <code>CreditCardInfo</code> from <code>CIP-Order-ES</code>
 *
 * <pre>
 * CreditCardInfo ::=
 * SEQUENCE {
 *   nameOnCard [1] IMPLICIT InternationalString
 *   expirationDate [2] IMPLICIT InternationalString
 *   cardNumber [3] IMPLICIT InternationalString
 * }
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class CreditCardInfo extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080320Z";

//----------------------------------------------------------------
/**
 * Default constructor for a CreditCardInfo.
 */

public
CreditCardInfo()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a CreditCardInfo from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
CreditCardInfo(BEREncoding ber, boolean check_tag)
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
  // CreditCardInfo should be encoded by a constructed BER

  BERConstructed ber_cons;
  try {
    ber_cons = (BERConstructed) ber;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun CreditCardInfo: bad BER form\n");
  }

  // Prepare to decode the components

  int num_parts = ber_cons.number_components();
  int part = 0;
  BEREncoding p;

  // Decoding: nameOnCard [1] IMPLICIT InternationalString

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun CreditCardInfo: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 1 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun CreditCardInfo: bad tag in s_nameOnCard\n");

  s_nameOnCard = new InternationalString(p, false);
  part++;

  // Decoding: expirationDate [2] IMPLICIT InternationalString

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun CreditCardInfo: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 2 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun CreditCardInfo: bad tag in s_expirationDate\n");

  s_expirationDate = new InternationalString(p, false);
  part++;

  // Decoding: cardNumber [3] IMPLICIT InternationalString

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun CreditCardInfo: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 3 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun CreditCardInfo: bad tag in s_cardNumber\n");

  s_cardNumber = new InternationalString(p, false);
  part++;

  // Should not be any more parts

  if (part < num_parts) {
    throw new ASN1Exception("Zebulun CreditCardInfo: bad BER: extra data " + part + "/" + num_parts + " processed");
  }
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of the CreditCardInfo.
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
 * Returns a BER encoding of CreditCardInfo, implicitly tagged.
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

  int num_fields = 3; // number of mandatories

  // Encode it

  BEREncoding fields[] = new BEREncoding[num_fields];
  int x = 0;

  // Encoding s_nameOnCard: InternationalString 

  fields[x++] = s_nameOnCard.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);

  // Encoding s_expirationDate: InternationalString 

  fields[x++] = s_expirationDate.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);

  // Encoding s_cardNumber: InternationalString 

  fields[x++] = s_cardNumber.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);

  return new BERConstructed(tag_type, tag, fields);
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the CreditCardInfo. 
 */

public String
toString()
{
  StringBuffer str = new StringBuffer("{");
  int outputted = 0;

  str.append("nameOnCard ");
  str.append(s_nameOnCard);
  outputted++;

  if (0 < outputted)
    str.append(", ");
  str.append("expirationDate ");
  str.append(s_expirationDate);
  outputted++;

  if (0 < outputted)
    str.append(", ");
  str.append("cardNumber ");
  str.append(s_cardNumber);
  outputted++;

  str.append("}");

  return str.toString();
}

//----------------------------------------------------------------
/*
 * Internal variables for class.
 */

public InternationalString s_nameOnCard;
public InternationalString s_expirationDate;
public InternationalString s_cardNumber;

} // CreditCardInfo

//----------------------------------------------------------------
//EOF
