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
import asn1.*;
import z3950.v3.AttributeElement;
import z3950.v3.AttributeList;
import z3950.v3.AttributeSetId;
import z3950.v3.DatabaseName;
import z3950.v3.ElementSetName;
import z3950.v3.IntUnit;
import z3950.v3.InternationalString;
import z3950.v3.OtherInformation;
import z3950.v3.Specification;
import z3950.v3.StringOrNumeric;
import z3950.v3.Term;
import z3950.v3.Unit;

//================================================================
/**
 * Class for representing a <code>AttributeValue</code> from <code>RecordSyntax-explain</code>
 *
 * <pre>
 * AttributeValue ::=
 * SEQUENCE {
 *   value [0] EXPLICIT StringOrNumeric
 *   description [1] IMPLICIT HumanString OPTIONAL
 *   subAttributes [2] IMPLICIT SEQUENCE OF StringOrNumeric OPTIONAL
 *   superAttributes [3] IMPLICIT SEQUENCE OF StringOrNumeric OPTIONAL
 *   partialSupport [4] IMPLICIT NULL OPTIONAL
 * }
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class AttributeValue extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";

//----------------------------------------------------------------
/**
 * Default constructor for a AttributeValue.
 */

public
AttributeValue()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a AttributeValue from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
AttributeValue(BEREncoding ber, boolean check_tag)
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
  // AttributeValue should be encoded by a constructed BER

  BERConstructed ber_cons;
  try {
    ber_cons = (BERConstructed) ber;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun AttributeValue: bad BER form\n");
  }

  // Prepare to decode the components

  int num_parts = ber_cons.number_components();
  int part = 0;
  BEREncoding p;
  BERConstructed tagged;

  // Decoding: value [0] EXPLICIT StringOrNumeric

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun AttributeValue: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 0 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun AttributeValue: bad tag in s_value\n");

  try {
    tagged = (BERConstructed) p;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun AttributeValue: bad BER encoding: s_value tag bad\n");
  }
  if (tagged.number_components() != 1) {
    throw new ASN1EncodingException
      ("Zebulun AttributeValue: bad BER encoding: s_value tag bad\n");
  }

  s_value = new StringOrNumeric(tagged.elementAt(0), true);
  part++;

  // Remaining elements are optional, set variables
  // to null (not present) so can return at end of BER

  s_description = null;
  s_subAttributes = null;
  s_superAttributes = null;
  s_partialSupport = null;

  // Decoding: description [1] IMPLICIT HumanString OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 1 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_description = new HumanString(p, false);
    part++;
  }

  // Decoding: subAttributes [2] IMPLICIT SEQUENCE OF StringOrNumeric OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 2 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      BERConstructed cons = (BERConstructed) p;
      int parts = cons.number_components();
      s_subAttributes = new StringOrNumeric[parts];
      int n;
      for (n = 0; n < parts; n++) {
        s_subAttributes[n] = new StringOrNumeric(cons.elementAt(n), true);
      }
    } catch (ClassCastException e) {
      throw new ASN1EncodingException("Bad BER");
    }
    part++;
  }

  // Decoding: superAttributes [3] IMPLICIT SEQUENCE OF StringOrNumeric OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 3 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      BERConstructed cons = (BERConstructed) p;
      int parts = cons.number_components();
      s_superAttributes = new StringOrNumeric[parts];
      int n;
      for (n = 0; n < parts; n++) {
        s_superAttributes[n] = new StringOrNumeric(cons.elementAt(n), true);
      }
    } catch (ClassCastException e) {
      throw new ASN1EncodingException("Bad BER");
    }
    part++;
  }

  // Decoding: partialSupport [4] IMPLICIT NULL OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 4 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_partialSupport = new ASN1Null(p, false);
    part++;
  }

  // Should not be any more parts

  if (part < num_parts) {
    throw new ASN1Exception("Zebulun AttributeValue: bad BER: extra data " + part + "/" + num_parts + " processed");
  }
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of the AttributeValue.
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
 * Returns a BER encoding of AttributeValue, implicitly tagged.
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
  if (s_description != null)
    num_fields++;
  if (s_subAttributes != null)
    num_fields++;
  if (s_superAttributes != null)
    num_fields++;
  if (s_partialSupport != null)
    num_fields++;

  // Encode it

  BEREncoding fields[] = new BEREncoding[num_fields];
  int x = 0;
  BEREncoding f2[];
  int p;
  BEREncoding enc[];

  // Encoding s_value: StringOrNumeric 

  enc = new BEREncoding[1];
  enc[0] = s_value.ber_encode();
  fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 0, enc);

  // Encoding s_description: HumanString OPTIONAL

  if (s_description != null) {
    fields[x++] = s_description.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);
  }

  // Encoding s_subAttributes: SEQUENCE OF OPTIONAL

  if (s_subAttributes != null) {
    f2 = new BEREncoding[s_subAttributes.length];

    for (p = 0; p < s_subAttributes.length; p++) {
      f2[p] = s_subAttributes[p].ber_encode();
    }

    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 2, f2);
  }

  // Encoding s_superAttributes: SEQUENCE OF OPTIONAL

  if (s_superAttributes != null) {
    f2 = new BEREncoding[s_superAttributes.length];

    for (p = 0; p < s_superAttributes.length; p++) {
      f2[p] = s_superAttributes[p].ber_encode();
    }

    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 3, f2);
  }

  // Encoding s_partialSupport: NULL OPTIONAL

  if (s_partialSupport != null) {
    fields[x++] = s_partialSupport.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 4);
  }

  return new BERConstructed(tag_type, tag, fields);
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the AttributeValue. 
 */

public String
toString()
{
  int p;
  StringBuffer str = new StringBuffer("{");
  int outputted = 0;

  str.append("value ");
  str.append(s_value);
  outputted++;

  if (s_description != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("description ");
    str.append(s_description);
    outputted++;
  }

  if (s_subAttributes != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("subAttributes ");
    str.append("{");
    for (p = 0; p < s_subAttributes.length; p++) {
      if (p != 0)
        str.append(", ");
      str.append(s_subAttributes[p]);
    }
    str.append("}");
    outputted++;
  }

  if (s_superAttributes != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("superAttributes ");
    str.append("{");
    for (p = 0; p < s_superAttributes.length; p++) {
      if (p != 0)
        str.append(", ");
      str.append(s_superAttributes[p]);
    }
    str.append("}");
    outputted++;
  }

  if (s_partialSupport != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("partialSupport ");
    str.append(s_partialSupport);
    outputted++;
  }

  str.append("}");

  return str.toString();
}

//----------------------------------------------------------------
/*
 * Internal variables for class.
 */

public StringOrNumeric s_value;
public HumanString s_description; // optional
public StringOrNumeric s_subAttributes[]; // optional
public StringOrNumeric s_superAttributes[]; // optional
public ASN1Null s_partialSupport; // optional

} // AttributeValue

//----------------------------------------------------------------
//EOF
