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
 * Class for representing a <code>OrderItem</code> from <code>CIP-Order-ES</code>
 *
 * <pre>
 * OrderItem ::=
 * SEQUENCE {
 *   productId [1] EXPLICIT InternationalString
 *   productPrice [2] EXPLICIT PriceInfo OPTIONAL
 *   productDeliveryOptions [3] EXPLICIT ProductDeliveryOptions OPTIONAL
 *   processingOptions [5] EXPLICIT ProcessingOptions OPTIONAL
 *   sceneSelectionOptions [6] EXPLICIT SceneSelectionOptions OPTIONAL
 *   orderStatusInfo [7] EXPLICIT OrderStatusInfo OPTIONAL
 *   otherInfo [8] EXPLICIT OtherInformation OPTIONAL
 * }
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class OrderItem extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080320Z";

//----------------------------------------------------------------
/**
 * Default constructor for a OrderItem.
 */

public
OrderItem()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a OrderItem from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
OrderItem(BEREncoding ber, boolean check_tag)
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
  // OrderItem should be encoded by a constructed BER

  BERConstructed ber_cons;
  try {
    ber_cons = (BERConstructed) ber;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun OrderItem: bad BER form\n");
  }

  // Prepare to decode the components

  int num_parts = ber_cons.number_components();
  int part = 0;
  BEREncoding p;
  BERConstructed tagged;

  // Decoding: productId [1] EXPLICIT InternationalString

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun OrderItem: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 1 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun OrderItem: bad tag in s_productId\n");

  try {
    tagged = (BERConstructed) p;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun OrderItem: bad BER encoding: s_productId tag bad\n");
  }
  if (tagged.number_components() != 1) {
    throw new ASN1EncodingException
      ("Zebulun OrderItem: bad BER encoding: s_productId tag bad\n");
  }

  s_productId = new InternationalString(tagged.elementAt(0), true);
  part++;

  // Decoding: productPrice [2] EXPLICIT PriceInfo OPTIONAL

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun OrderItem: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 2 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      tagged = (BERConstructed) p;
    } catch (ClassCastException e) {
      throw new ASN1EncodingException
        ("Zebulun OrderItem: bad BER encoding: s_productPrice tag bad\n");
    }
    if (tagged.number_components() != 1) {
      throw new ASN1EncodingException
        ("Zebulun OrderItem: bad BER encoding: s_productPrice tag bad\n");
    }

    s_productPrice = new PriceInfo(tagged.elementAt(0), true);
    part++;
  }

  // Decoding: productDeliveryOptions [3] EXPLICIT ProductDeliveryOptions OPTIONAL

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun OrderItem: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 3 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      tagged = (BERConstructed) p;
    } catch (ClassCastException e) {
      throw new ASN1EncodingException
        ("Zebulun OrderItem: bad BER encoding: s_productDeliveryOptions tag bad\n");
    }
    if (tagged.number_components() != 1) {
      throw new ASN1EncodingException
        ("Zebulun OrderItem: bad BER encoding: s_productDeliveryOptions tag bad\n");
    }

    s_productDeliveryOptions = new ProductDeliveryOptions(tagged.elementAt(0), true);
    part++;
  }

  // Remaining elements are optional, set variables
  // to null (not present) so can return at end of BER

  s_processingOptions = null;
  s_sceneSelectionOptions = null;
  s_orderStatusInfo = null;
  s_otherInfo = null;

  // Decoding: processingOptions [5] EXPLICIT ProcessingOptions OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 5 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      tagged = (BERConstructed) p;
    } catch (ClassCastException e) {
      throw new ASN1EncodingException
        ("Zebulun OrderItem: bad BER encoding: s_processingOptions tag bad\n");
    }
    if (tagged.number_components() != 1) {
      throw new ASN1EncodingException
        ("Zebulun OrderItem: bad BER encoding: s_processingOptions tag bad\n");
    }

    s_processingOptions = new ProcessingOptions(tagged.elementAt(0), true);
    part++;
  }

  // Decoding: sceneSelectionOptions [6] EXPLICIT SceneSelectionOptions OPTIONAL

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
        ("Zebulun OrderItem: bad BER encoding: s_sceneSelectionOptions tag bad\n");
    }
    if (tagged.number_components() != 1) {
      throw new ASN1EncodingException
        ("Zebulun OrderItem: bad BER encoding: s_sceneSelectionOptions tag bad\n");
    }

    s_sceneSelectionOptions = new SceneSelectionOptions(tagged.elementAt(0), true);
    part++;
  }

  // Decoding: orderStatusInfo [7] EXPLICIT orderStatusInfo OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 7 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      tagged = (BERConstructed) p;
    } catch (ClassCastException e) {
      throw new ASN1EncodingException
        ("Zebulun OrderItem: bad BER encoding: s_orderStatusInfo tag bad\n");
    }
    if (tagged.number_components() != 1) {
      throw new ASN1EncodingException
        ("Zebulun OrderItem: bad BER encoding: s_orderStatusInfo tag bad\n");
    }

    s_orderStatusInfo = new OrderStatusInfo(tagged.elementAt(0), true);
    part++;
  }

  // Decoding: otherInfo [8] EXPLICIT OtherInformation OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 8 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      tagged = (BERConstructed) p;
    } catch (ClassCastException e) {
      throw new ASN1EncodingException
        ("Zebulun OrderItem: bad BER encoding: s_otherInfo tag bad\n");
    }
    if (tagged.number_components() != 1) {
      throw new ASN1EncodingException
        ("Zebulun OrderItem: bad BER encoding: s_otherInfo tag bad\n");
    }

    s_otherInfo = new OtherInformation(tagged.elementAt(0), true);
    part++;
  }

  // Should not be any more parts

  if (part < num_parts) {
    throw new ASN1Exception("Zebulun OrderItem: bad BER: extra data " + part + "/" + num_parts + " processed");
  }
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of the OrderItem.
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
 * Returns a BER encoding of OrderItem, implicitly tagged.
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

  int num_fields = 1; // number of mandatories
  if (s_productPrice != null)
    num_fields++;
  if (s_productDeliveryOptions != null)
    num_fields++;
  if (s_processingOptions != null)
    num_fields++;
  if (s_sceneSelectionOptions != null)
    num_fields++;
  if (s_orderStatusInfo != null)
    num_fields++;
  if (s_otherInfo != null)
    num_fields++;

  // Encode it

  BEREncoding fields[] = new BEREncoding[num_fields];
  int x = 0;
  BEREncoding enc[];

  // Encoding s_productId: InternationalString 

  enc = new BEREncoding[1];
  enc[0] = s_productId.ber_encode();
  fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 1, enc);

  // Encoding s_productPrice: PriceInfo OPTIONAL

  if (s_productPrice != null) {
    enc = new BEREncoding[1];
    enc[0] = s_productPrice.ber_encode();
    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 2, enc);
  }

  // Encoding s_productDeliveryOptions: ProductDeliveryOptions OPTIONAL

  if (s_productDeliveryOptions != null) {
    enc = new BEREncoding[1];
    enc[0] = s_productDeliveryOptions.ber_encode();
    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 3, enc);
  }

  // Encoding s_processingOptions: ProcessingOptions OPTIONAL

  if (s_processingOptions != null) {
    enc = new BEREncoding[1];
    enc[0] = s_processingOptions.ber_encode();
    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 5, enc);
  }

  // Encoding s_sceneSelectionOptions: SceneSelectionOptions OPTIONAL

  if (s_sceneSelectionOptions != null) {
    enc = new BEREncoding[1];
    enc[0] = s_sceneSelectionOptions.ber_encode();
    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 6, enc);
  }

  // Encoding s_orderStatusInfo: OrderStatusInfo OPTIONAL

  if (s_orderStatusInfo != null) {
    enc = new BEREncoding[1];
    enc[0] = s_orderStatusInfo.ber_encode();
    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 7, enc);
  }

  // Encoding s_otherInfo: OtherInformation OPTIONAL

  if (s_otherInfo != null) {
    enc = new BEREncoding[1];
    enc[0] = s_otherInfo.ber_encode();
    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 8, enc);
  }

  return new BERConstructed(tag_type, tag, fields);
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the OrderItem. 
 */

public String
toString()
{
  StringBuffer str = new StringBuffer("{");
  int outputted = 0;

  str.append("productId ");
  str.append(s_productId);
  outputted++;

  if (s_productPrice != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("productPrice ");
    str.append(s_productPrice);
    outputted++;
  }

  if (s_productDeliveryOptions != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("productDeliveryOptions ");
    str.append(s_productDeliveryOptions);
    outputted++;
  }

  if (s_processingOptions != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("processingOptions ");
    str.append(s_processingOptions);
    outputted++;
  }

  if (s_sceneSelectionOptions != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("sceneSelectionOptions ");
    str.append(s_sceneSelectionOptions);
    outputted++;
  }

  if (s_orderStatusInfo != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("orderStatusInfo ");
    str.append(s_orderStatusInfo);
    outputted++;
  }

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

public InternationalString s_productId;
public PriceInfo s_productPrice; // optional
public ProductDeliveryOptions s_productDeliveryOptions; // optional
public ProcessingOptions s_processingOptions; // optional
public SceneSelectionOptions s_sceneSelectionOptions; // optional
public OrderStatusInfo s_orderStatusInfo; // optional
public OtherInformation s_otherInfo; // optional

} // OrderItem

//----------------------------------------------------------------
//EOF
