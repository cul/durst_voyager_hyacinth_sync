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
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:15:24 UTC
 */

//----------------------------------------------------------------

package z3950.AccessCtrl_prompt;
import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1External;
import asn1.ASN1Integer;
import asn1.ASN1Null;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;
import z3950.v3.InternationalString;

//================================================================
/**
 * Class for representing a <code>Challenge1</code> from <code>AccessControlFormat-prompt-1</code>
 *
 * <pre>
 * Challenge1 ::=
 * SEQUENCE {
 *   promptId [1] EXPLICIT PromptId
 *   defaultResponse [2] IMPLICIT InternationalString OPTIONAL
 *   promptInfo [3] EXPLICIT Challenge_promptInfo OPTIONAL
 *   regExpr [4] IMPLICIT InternationalString OPTIONAL
 *   responseRequired [5] IMPLICIT NULL OPTIONAL
 *   allowedValues [6] IMPLICIT SEQUENCE OF InternationalString OPTIONAL
 *   shouldSave [7] IMPLICIT NULL OPTIONAL
 *   dataType [8] IMPLICIT INTEGER OPTIONAL
 *   diagnostic [9] IMPLICIT EXTERNAL OPTIONAL
 * }
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class Challenge1 extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";

//----------------------------------------------------------------
/**
 * Default constructor for a Challenge1.
 */

public
Challenge1()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a Challenge1 from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
Challenge1(BEREncoding ber, boolean check_tag)
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
  // Challenge1 should be encoded by a constructed BER

  BERConstructed ber_cons;
  try {
    ber_cons = (BERConstructed) ber;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun Challenge1: bad BER form\n");
  }

  // Prepare to decode the components

  int num_parts = ber_cons.number_components();
  int part = 0;
  BEREncoding p;
  BERConstructed tagged;

  // Decoding: promptId [1] EXPLICIT PromptId

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun Challenge1: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 1 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun Challenge1: bad tag in s_promptId\n");

  try {
    tagged = (BERConstructed) p;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun Challenge1: bad BER encoding: s_promptId tag bad\n");
  }
  if (tagged.number_components() != 1) {
    throw new ASN1EncodingException
      ("Zebulun Challenge1: bad BER encoding: s_promptId tag bad\n");
  }

  s_promptId = new PromptId(tagged.elementAt(0), true);
  part++;

  // Remaining elements are optional, set variables
  // to null (not present) so can return at end of BER

  s_defaultResponse = null;
  s_promptInfo = null;
  s_regExpr = null;
  s_responseRequired = null;
  s_allowedValues = null;
  s_shouldSave = null;
  s_dataType = null;
  s_diagnostic = null;

  // Decoding: defaultResponse [2] IMPLICIT InternationalString OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 2 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_defaultResponse = new InternationalString(p, false);
    part++;
  }

  // Decoding: promptInfo [3] EXPLICIT Challenge_promptInfo OPTIONAL

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
        ("Zebulun Challenge1: bad BER encoding: s_promptInfo tag bad\n");
    }
    if (tagged.number_components() != 1) {
      throw new ASN1EncodingException
        ("Zebulun Challenge1: bad BER encoding: s_promptInfo tag bad\n");
    }

    s_promptInfo = new Challenge_promptInfo(tagged.elementAt(0), true);
    part++;
  }

  // Decoding: regExpr [4] IMPLICIT InternationalString OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 4 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_regExpr = new InternationalString(p, false);
    part++;
  }

  // Decoding: responseRequired [5] IMPLICIT NULL OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 5 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_responseRequired = new ASN1Null(p, false);
    part++;
  }

  // Decoding: allowedValues [6] IMPLICIT SEQUENCE OF InternationalString OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 6 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      BERConstructed cons = (BERConstructed) p;
      int parts = cons.number_components();
      s_allowedValues = new InternationalString[parts];
      int n;
      for (n = 0; n < parts; n++) {
        s_allowedValues[n] = new InternationalString(cons.elementAt(n), true);
      }
    } catch (ClassCastException e) {
      throw new ASN1EncodingException("Bad BER");
    }
    part++;
  }

  // Decoding: shouldSave [7] IMPLICIT NULL OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 7 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_shouldSave = new ASN1Null(p, false);
    part++;
  }

  // Decoding: dataType [8] IMPLICIT INTEGER OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 8 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_dataType = new ASN1Integer(p, false);
    part++;
  }

  // Decoding: diagnostic [9] IMPLICIT EXTERNAL OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 9 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_diagnostic = new ASN1External(p, false);
    part++;
  }

  // Should not be any more parts

  if (part < num_parts) {
    throw new ASN1Exception("Zebulun Challenge1: bad BER: extra data " + part + "/" + num_parts + " processed");
  }
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of the Challenge1.
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
 * Returns a BER encoding of Challenge1, implicitly tagged.
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
  if (s_defaultResponse != null)
    num_fields++;
  if (s_promptInfo != null)
    num_fields++;
  if (s_regExpr != null)
    num_fields++;
  if (s_responseRequired != null)
    num_fields++;
  if (s_allowedValues != null)
    num_fields++;
  if (s_shouldSave != null)
    num_fields++;
  if (s_dataType != null)
    num_fields++;
  if (s_diagnostic != null)
    num_fields++;

  // Encode it

  BEREncoding fields[] = new BEREncoding[num_fields];
  int x = 0;
  BEREncoding f2[];
  int p;
  BEREncoding enc[];

  // Encoding s_promptId: PromptId 

  enc = new BEREncoding[1];
  enc[0] = s_promptId.ber_encode();
  fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 1, enc);

  // Encoding s_defaultResponse: InternationalString OPTIONAL

  if (s_defaultResponse != null) {
    fields[x++] = s_defaultResponse.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
  }

  // Encoding s_promptInfo: Challenge_promptInfo OPTIONAL

  if (s_promptInfo != null) {
    enc = new BEREncoding[1];
    enc[0] = s_promptInfo.ber_encode();
    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 3, enc);
  }

  // Encoding s_regExpr: InternationalString OPTIONAL

  if (s_regExpr != null) {
    fields[x++] = s_regExpr.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 4);
  }

  // Encoding s_responseRequired: NULL OPTIONAL

  if (s_responseRequired != null) {
    fields[x++] = s_responseRequired.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 5);
  }

  // Encoding s_allowedValues: SEQUENCE OF OPTIONAL

  if (s_allowedValues != null) {
    f2 = new BEREncoding[s_allowedValues.length];

    for (p = 0; p < s_allowedValues.length; p++) {
      f2[p] = s_allowedValues[p].ber_encode();
    }

    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 6, f2);
  }

  // Encoding s_shouldSave: NULL OPTIONAL

  if (s_shouldSave != null) {
    fields[x++] = s_shouldSave.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 7);
  }

  // Encoding s_dataType: INTEGER OPTIONAL

  if (s_dataType != null) {
    fields[x++] = s_dataType.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 8);
  }

  // Encoding s_diagnostic: EXTERNAL OPTIONAL

  if (s_diagnostic != null) {
    fields[x++] = s_diagnostic.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 9);
  }

  return new BERConstructed(tag_type, tag, fields);
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the Challenge1. 
 */

public String
toString()
{
  int p;
  StringBuffer str = new StringBuffer("{");
  int outputted = 0;

  str.append("promptId ");
  str.append(s_promptId);
  outputted++;

  if (s_defaultResponse != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("defaultResponse ");
    str.append(s_defaultResponse);
    outputted++;
  }

  if (s_promptInfo != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("promptInfo ");
    str.append(s_promptInfo);
    outputted++;
  }

  if (s_regExpr != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("regExpr ");
    str.append(s_regExpr);
    outputted++;
  }

  if (s_responseRequired != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("responseRequired ");
    str.append(s_responseRequired);
    outputted++;
  }

  if (s_allowedValues != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("allowedValues ");
    str.append("{");
    for (p = 0; p < s_allowedValues.length; p++) {
      if (p != 0)
        str.append(", ");
      str.append(s_allowedValues[p]);
    }
    str.append("}");
    outputted++;
  }

  if (s_shouldSave != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("shouldSave ");
    str.append(s_shouldSave);
    outputted++;
  }

  if (s_dataType != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("dataType ");
    str.append(s_dataType);
    outputted++;
  }

  if (s_diagnostic != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("diagnostic ");
    str.append(s_diagnostic);
    outputted++;
  }

  str.append("}");

  return str.toString();
}

//----------------------------------------------------------------
/*
 * Internal variables for class.
 */

public PromptId s_promptId;
public InternationalString s_defaultResponse; // optional
public Challenge_promptInfo s_promptInfo; // optional
public InternationalString s_regExpr; // optional
public ASN1Null s_responseRequired; // optional
public InternationalString s_allowedValues[]; // optional
public ASN1Null s_shouldSave; // optional
public ASN1Integer s_dataType; // optional
public ASN1External s_diagnostic; // optional

//----------------------------------------------------------------
/*
 * Enumerated constants for class.
 */

// Enumerated constants for dataType
public static final int E_integer = 1;
public static final int E_date = 2;
public static final int E_float = 3;
public static final int E_alphaNumeric = 4;
public static final int E_url_urn = 5;
public static final int E_boolean = 6;

} // Challenge1

//----------------------------------------------------------------
//EOF
