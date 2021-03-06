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
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:20:31 UTC
 */

//----------------------------------------------------------------

package z3950.ESFormat_CIPOrder;
import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;
import z3950.v3.IntUnit;
import z3950.v3.InternationalString;

//================================================================
/**
 * Class for representing a <code>PriceInfo</code> from <code>CIP-Order-ES</code>
 *
 * <pre>
 * PriceInfo ::=
 * SEQUENCE {
 *   price [1] EXPLICIT IntUnit
 *   priceExpirationDate [2] EXPLICIT InternationalString
 *   additionalPriceInfo [3] EXPLICIT InternationalString OPTIONAL
 * }
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class PriceInfo extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080320Z";

//----------------------------------------------------------------
/**
 * Default constructor for a PriceInfo.
 */

public
PriceInfo()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a PriceInfo from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
PriceInfo(BEREncoding ber, boolean check_tag)
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
  // PriceInfo should be encoded by a constructed BER

  BERConstructed ber_cons;
  try {
    ber_cons = (BERConstructed) ber;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun PriceInfo: bad BER form\n");
  }

  // Prepare to decode the components

  int num_parts = ber_cons.number_components();
  int part = 0;
  BEREncoding p;
  BERConstructed tagged;

  // Decoding: price [1] EXPLICIT IntUnit

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun PriceInfo: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 1 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun PriceInfo: bad tag in s_price\n");

  try {
    tagged = (BERConstructed) p;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun PriceInfo: bad BER encoding: s_price tag bad\n");
  }
  if (tagged.number_components() != 1) {
    throw new ASN1EncodingException
      ("Zebulun PriceInfo: bad BER encoding: s_price tag bad\n");
  }

  s_price = new IntUnit(tagged.elementAt(0), true);
  part++;

  // Decoding: priceExpirationDate [2] EXPLICIT InternationalString

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun PriceInfo: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 2 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun PriceInfo: bad tag in s_priceExpirationDate\n");

  try {
    tagged = (BERConstructed) p;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun PriceInfo: bad BER encoding: s_priceExpirationDate tag bad\n");
  }
  if (tagged.number_components() != 1) {
    throw new ASN1EncodingException
      ("Zebulun PriceInfo: bad BER encoding: s_priceExpirationDate tag bad\n");
  }

  s_priceExpirationDate = new InternationalString(tagged.elementAt(0), true);
  part++;

  // Remaining elements are optional, set variables
  // to null (not present) so can return at end of BER

  s_additionalPriceInfo = null;

  // Decoding: additionalPriceInfo [3] EXPLICIT InternationalString OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 3 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      tagged = (BERConstructed) p;
    } catch (ClassCastException e) {
      throw new ASN1EncodingException
        ("Zebulun PriceInfo: bad BER encoding: s_additionalPriceInfo tag bad\n");
    }
    if (tagged.number_components() != 1) {
      throw new ASN1EncodingException
        ("Zebulun PriceInfo: bad BER encoding: s_additionalPriceInfo tag bad\n");
    }

    s_additionalPriceInfo = new InternationalString(tagged.elementAt(0), true);
    part++;
  }

  // Should not be any more parts

  if (part < num_parts) {
    throw new ASN1Exception("Zebulun PriceInfo: bad BER: extra data " + part + "/" + num_parts + " processed");
  }
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of the PriceInfo.
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
 * Returns a BER encoding of PriceInfo, implicitly tagged.
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
  if (s_additionalPriceInfo != null)
    num_fields++;

  // Encode it

  BEREncoding fields[] = new BEREncoding[num_fields];
  int x = 0;
  BEREncoding enc[];

  // Encoding s_price: IntUnit 

  enc = new BEREncoding[1];
  enc[0] = s_price.ber_encode();
  fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 1, enc);

  // Encoding s_priceExpirationDate: InternationalString 

  enc = new BEREncoding[1];
  enc[0] = s_priceExpirationDate.ber_encode();
  fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 2, enc);

  // Encoding s_additionalPriceInfo: InternationalString OPTIONAL

  if (s_additionalPriceInfo != null) {
    enc = new BEREncoding[1];
    enc[0] = s_additionalPriceInfo.ber_encode();
    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 3, enc);
  }

  return new BERConstructed(tag_type, tag, fields);
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the PriceInfo. 
 */

public String
toString()
{
  StringBuffer str = new StringBuffer("{");
  int outputted = 0;

  str.append("price ");
  str.append(s_price);
  outputted++;

  if (0 < outputted)
    str.append(", ");
  str.append("priceExpirationDate ");
  str.append(s_priceExpirationDate);
  outputted++;

  if (s_additionalPriceInfo != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("additionalPriceInfo ");
    str.append(s_additionalPriceInfo);
    outputted++;
  }

  str.append("}");

  return str.toString();
}

//----------------------------------------------------------------
/*
 * Internal variables for class.
 */

public IntUnit s_price;
public InternationalString s_priceExpirationDate;
public InternationalString s_additionalPriceInfo; // optional

} // PriceInfo

//----------------------------------------------------------------
//EOF
