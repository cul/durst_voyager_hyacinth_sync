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

package z3950.ESFormat_CIP2_4Order;
import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;
import z3950.v3.InternationalString;
import z3950.v3.OtherInformation;

//================================================================
/**
 * Class for representing a <code>OrderSpecification</code> from <code>CIP-Order-ES</code>
 *
 * <pre>
 * OrderSpecification ::=
 * SEQUENCE {
 *   orderingCentreId [1] EXPLICIT InternationalString
 *   orderPrice [2] EXPLICIT PriceInfo OPTIONAL
 *   orderDeliveryDate [3] EXPLICIT InternationalString OPTIONAL
 *   orderCancellationDate [4] EXPLICIT InternationalString OPTIONAL
 *   deliveryUnits [5] EXPLICIT SEQUENCE OF DeliveryUnitSpec
 *   otherInfo [6] EXPLICIT OtherInformation OPTIONAL
 * }
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class OrderSpecification extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080320Z";

//----------------------------------------------------------------
/**
 * Default constructor for a OrderSpecification.
 */

public
OrderSpecification()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a OrderSpecification from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
OrderSpecification(BEREncoding ber, boolean check_tag)
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
  // OrderSpecification should be encoded by a constructed BER

  BERConstructed ber_cons;
  try {
    ber_cons = (BERConstructed) ber;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun OrderSpecification: bad BER form\n");
  }

  // Prepare to decode the components

  int num_parts = ber_cons.number_components();
  int part = 0;
  BEREncoding p;
  BERConstructed tagged;

  // Decoding: orderingCentreId [1] EXPLICIT InternationalString

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun OrderSpecification: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 1 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun OrderSpecification: bad tag in s_orderingCentreId\n");

  try {
    tagged = (BERConstructed) p;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun OrderSpecification: bad BER encoding: s_orderingCentreId tag bad\n");
  }
  if (tagged.number_components() != 1) {
    throw new ASN1EncodingException
      ("Zebulun OrderSpecification: bad BER encoding: s_orderingCentreId tag bad\n");
  }

  s_orderingCentreId = new InternationalString(tagged.elementAt(0), true);
  part++;

  // Decoding: orderPrice [2] EXPLICIT PriceInfo OPTIONAL

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun OrderSpecification: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 2 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      tagged = (BERConstructed) p;
    } catch (ClassCastException e) {
      throw new ASN1EncodingException
        ("Zebulun OrderSpecification: bad BER encoding: s_orderPrice tag bad\n");
    }
    if (tagged.number_components() != 1) {
      throw new ASN1EncodingException
        ("Zebulun OrderSpecification: bad BER encoding: s_orderPrice tag bad\n");
    }

    s_orderPrice = new PriceInfo(tagged.elementAt(0), true);
    part++;
  }

  // Decoding: orderDeliveryDate [3] EXPLICIT InternationalString OPTIONAL

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun OrderSpecification: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 3 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      tagged = (BERConstructed) p;
    } catch (ClassCastException e) {
      throw new ASN1EncodingException
        ("Zebulun OrderSpecification: bad BER encoding: s_orderDeliveryDate tag bad\n");
    }
    if (tagged.number_components() != 1) {
      throw new ASN1EncodingException
        ("Zebulun OrderSpecification: bad BER encoding: s_orderDeliveryDate tag bad\n");
    }

    s_orderDeliveryDate = new InternationalString(tagged.elementAt(0), true);
    part++;
  }

  // Decoding: orderCancellationDate [4] EXPLICIT InternationalString OPTIONAL

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun OrderSpecification: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 4 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      tagged = (BERConstructed) p;
    } catch (ClassCastException e) {
      throw new ASN1EncodingException
        ("Zebulun OrderSpecification: bad BER encoding: s_orderCancellationDate tag bad\n");
    }
    if (tagged.number_components() != 1) {
      throw new ASN1EncodingException
        ("Zebulun OrderSpecification: bad BER encoding: s_orderCancellationDate tag bad\n");
    }

    s_orderCancellationDate = new InternationalString(tagged.elementAt(0), true);
    part++;
  }

  // Decoding: deliveryUnits [5] EXPLICIT SEQUENCE OF DeliveryUnitSpec

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun OrderSpecification: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 5 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun OrderSpecification: bad tag in s_deliveryUnits\n");

  try {
    tagged = (BERConstructed) p;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun OrderSpecification: bad BER encoding: s_deliveryUnits tag bad\n");
  }
  if (tagged.number_components() != 1) {
    throw new ASN1EncodingException
      ("Zebulun OrderSpecification: bad BER encoding: s_deliveryUnits tag bad\n");
  }

  try {
    BERConstructed cons = (BERConstructed) tagged.elementAt(0);
    int parts = cons.number_components();
    s_deliveryUnits = new DeliveryUnitSpec[parts];
    int n;
    for (n = 0; n < parts; n++) {
      s_deliveryUnits[n] = new DeliveryUnitSpec(cons.elementAt(n), true);
    }
  } catch (ClassCastException e) {
    throw new ASN1EncodingException("Bad BER");
  }
  part++;

  // Remaining elements are optional, set variables
  // to null (not present) so can return at end of BER

  s_otherInfo = null;

  // Decoding: otherInfo [6] EXPLICIT OtherInformation OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 6 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      tagged = (BERConstructed) p;
    } catch (ClassCastException e) {
      throw new ASN1EncodingException
        ("Zebulun OrderSpecification: bad BER encoding: s_otherInfo tag bad\n");
    }
    if (tagged.number_components() != 1) {
      throw new ASN1EncodingException
        ("Zebulun OrderSpecification: bad BER encoding: s_otherInfo tag bad\n");
    }

    s_otherInfo = new OtherInformation(tagged.elementAt(0), true);
    part++;
  }

  // Should not be any more parts

  if (part < num_parts) {
    throw new ASN1Exception("Zebulun OrderSpecification: bad BER: extra data " + part + "/" + num_parts + " processed");
  }
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of the OrderSpecification.
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
 * Returns a BER encoding of OrderSpecification, implicitly tagged.
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
  if (s_orderPrice != null)
    num_fields++;
  if (s_orderDeliveryDate != null)
    num_fields++;
  if (s_orderCancellationDate != null)
    num_fields++;
  if (s_otherInfo != null)
    num_fields++;

  // Encode it

  BEREncoding fields[] = new BEREncoding[num_fields];
  int x = 0;
  BEREncoding f2[];
  int p;
  BEREncoding enc[];

  // Encoding s_orderingCentreId: InternationalString 

  enc = new BEREncoding[1];
  enc[0] = s_orderingCentreId.ber_encode();
  fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 1, enc);

  // Encoding s_orderPrice: PriceInfo OPTIONAL

  if (s_orderPrice != null) {
    enc = new BEREncoding[1];
    enc[0] = s_orderPrice.ber_encode();
    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 2, enc);
  }

  // Encoding s_orderDeliveryDate: InternationalString OPTIONAL

  if (s_orderDeliveryDate != null) {
    enc = new BEREncoding[1];
    enc[0] = s_orderDeliveryDate.ber_encode();
    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 3, enc);
  }

  // Encoding s_orderCancellationDate: InternationalString OPTIONAL

  if (s_orderCancellationDate != null) {
    enc = new BEREncoding[1];
    enc[0] = s_orderCancellationDate.ber_encode();
    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 4, enc);
  }

  // Encoding s_deliveryUnits: SEQUENCE OF 

  enc = new BEREncoding[1];
  f2 = new BEREncoding[s_deliveryUnits.length];

  for (p = 0; p < s_deliveryUnits.length; p++) {
    f2[p] = s_deliveryUnits[p].ber_encode();
  }

  enc[0] = new BERConstructed(BEREncoding.UNIVERSAL_TAG, ASN1Sequence.TAG, f2);
  fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 5, enc);

  // Encoding s_otherInfo: OtherInformation OPTIONAL

  if (s_otherInfo != null) {
    enc = new BEREncoding[1];
    enc[0] = s_otherInfo.ber_encode();
    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 6, enc);
  }

  return new BERConstructed(tag_type, tag, fields);
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the OrderSpecification. 
 */

public String
toString()
{
  int p;
  StringBuffer str = new StringBuffer("{");
  int outputted = 0;

  str.append("orderingCentreId ");
  str.append(s_orderingCentreId);
  outputted++;

  if (s_orderPrice != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("orderPrice ");
    str.append(s_orderPrice);
    outputted++;
  }

  if (s_orderDeliveryDate != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("orderDeliveryDate ");
    str.append(s_orderDeliveryDate);
    outputted++;
  }

  if (s_orderCancellationDate != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("orderCancellationDate ");
    str.append(s_orderCancellationDate);
    outputted++;
  }

  if (0 < outputted)
    str.append(", ");
  str.append("deliveryUnits ");
  str.append("{");
  for (p = 0; p < s_deliveryUnits.length; p++) {
    if (p != 0)
      str.append(", ");
    str.append(s_deliveryUnits[p]);
  }
  str.append("}");
  outputted++;

  if (s_otherInfo != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("otherInfo ");
    str.append(s_otherInfo);
    outputted++;
  }

  str.append("}");

  return str.toString();
}

//----------------------------------------------------------------
/*
 * Internal variables for class.
 */

public InternationalString s_orderingCentreId;
public PriceInfo s_orderPrice; // optional
public InternationalString s_orderDeliveryDate; // optional
public InternationalString s_orderCancellationDate; // optional
public DeliveryUnitSpec s_deliveryUnits[];
public OtherInformation s_otherInfo; // optional

} // OrderSpecification

//----------------------------------------------------------------
//EOF
