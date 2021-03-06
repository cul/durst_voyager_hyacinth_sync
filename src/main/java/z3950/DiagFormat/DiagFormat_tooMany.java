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
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:15:12 UTC
 */

//----------------------------------------------------------------

package z3950.DiagFormat;
import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Integer;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;

//================================================================
/**
 * Class for representing a <code>DiagFormat_tooMany</code> from <code>DiagnosticFormatDiag1</code>
 *
 * <pre>
 * DiagFormat_tooMany ::=
 * SEQUENCE {
 *   tooManyWhat [1] IMPLICIT INTEGER
 *   max [2] IMPLICIT INTEGER OPTIONAL
 * }
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class DiagFormat_tooMany extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";

//----------------------------------------------------------------
/**
 * Default constructor for a DiagFormat_tooMany.
 */

public
DiagFormat_tooMany()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a DiagFormat_tooMany from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
DiagFormat_tooMany(BEREncoding ber, boolean check_tag)
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
  // DiagFormat_tooMany should be encoded by a constructed BER

  BERConstructed ber_cons;
  try {
    ber_cons = (BERConstructed) ber;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun DiagFormat_tooMany: bad BER form\n");
  }

  // Prepare to decode the components

  int num_parts = ber_cons.number_components();
  int part = 0;
  BEREncoding p;

  // Decoding: tooManyWhat [1] IMPLICIT INTEGER

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun DiagFormat_tooMany: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 1 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun DiagFormat_tooMany: bad tag in s_tooManyWhat\n");

  s_tooManyWhat = new ASN1Integer(p, false);
  part++;

  // Remaining elements are optional, set variables
  // to null (not present) so can return at end of BER

  s_max = null;

  // Decoding: max [2] IMPLICIT INTEGER OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 2 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_max = new ASN1Integer(p, false);
    part++;
  }

  // Should not be any more parts

  if (part < num_parts) {
    throw new ASN1Exception("Zebulun DiagFormat_tooMany: bad BER: extra data " + part + "/" + num_parts + " processed");
  }
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of the DiagFormat_tooMany.
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
 * Returns a BER encoding of DiagFormat_tooMany, implicitly tagged.
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
  if (s_max != null)
    num_fields++;

  // Encode it

  BEREncoding fields[] = new BEREncoding[num_fields];
  int x = 0;

  // Encoding s_tooManyWhat: INTEGER 

  fields[x++] = s_tooManyWhat.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);

  // Encoding s_max: INTEGER OPTIONAL

  if (s_max != null) {
    fields[x++] = s_max.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
  }

  return new BERConstructed(tag_type, tag, fields);
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the DiagFormat_tooMany. 
 */

public String
toString()
{
  StringBuffer str = new StringBuffer("{");
  int outputted = 0;

  str.append("tooManyWhat ");
  str.append(s_tooManyWhat);
  outputted++;

  if (s_max != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("max ");
    str.append(s_max);
    outputted++;
  }

  str.append("}");

  return str.toString();
}

//----------------------------------------------------------------
/*
 * Internal variables for class.
 */

public ASN1Integer s_tooManyWhat;
public ASN1Integer s_max; // optional

//----------------------------------------------------------------
/*
 * Enumerated constants for class.
 */

// Enumerated constants for tooManyWhat
public static final int E_argumentWords = 1;
public static final int E_truncatedWords = 2;
public static final int E_booleanOperators = 3;
public static final int E_incompleteSubfields = 4;
public static final int E_characters = 5;
public static final int E_recordsRetrieved = 6;
public static final int E_dataBasesSpecified = 7;
public static final int E_resultSetsCreated = 8;
public static final int E_indexTermsProcessed = 9;

} // DiagFormat_tooMany

//----------------------------------------------------------------
//EOF
