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
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:14:27 UTC
 */

//----------------------------------------------------------------

package z3950.v2;
import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Integer;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;


//================================================================
/**
 * Class for representing a <code>PresentRequest</code> from <code>IR</code>
 *
 * <pre>
 * PresentRequest ::=
 * SEQUENCE {
 *   referenceId ReferenceId OPTIONAL
 *   resultSetId ResultSetId
 *   resultSetStartPoint [30] IMPLICIT INTEGER
 *   numberOfRecordsRequested [29] IMPLICIT INTEGER
 *   ElementSetNames OPTIONAL
 *   preferredRecordSyntax PreferredRecordSyntax OPTIONAL
 * }
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class PresentRequest extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080314Z";

//----------------------------------------------------------------
/**
 * Default constructor for a PresentRequest.
 */

public
PresentRequest()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a PresentRequest from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
PresentRequest(BEREncoding ber, boolean check_tag)
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
  // PresentRequest should be encoded by a constructed BER

  BERConstructed ber_cons;
  try {
    ber_cons = (BERConstructed) ber;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun PresentRequest: bad BER form\n");
  }

  // Prepare to decode the components

  int num_parts = ber_cons.number_components();
  int part = 0;
  BEREncoding p;

  // Decoding: referenceId ReferenceId OPTIONAL

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun PresentRequest: incomplete");
  }
  p = ber_cons.elementAt(part);

  try {
    s_referenceId = new ReferenceId(p, true);
    part++; // yes, consumed
  } catch (ASN1Exception e) {
    s_referenceId = null; // no, not present
  }

  // Decoding: resultSetId ResultSetId

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun PresentRequest: incomplete");
  }
  p = ber_cons.elementAt(part);

  s_resultSetId = new ResultSetId(p, true);
  part++;

  // Decoding: resultSetStartPoint [30] IMPLICIT INTEGER

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun PresentRequest: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 30 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun PresentRequest: bad tag in s_resultSetStartPoint\n");

  s_resultSetStartPoint = new ASN1Integer(p, false);
  part++;

  // Decoding: numberOfRecordsRequested [29] IMPLICIT INTEGER

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun PresentRequest: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 29 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun PresentRequest: bad tag in s_numberOfRecordsRequested\n");

  s_numberOfRecordsRequested = new ASN1Integer(p, false);
  part++;

  // Remaining elements are optional, set variables
  // to null (not present) so can return at end of BER

  s_5 = null;
  s_preferredRecordSyntax = null;

  // Decoding: ElementSetNames OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  try {
    s_5 = new ElementSetNames(p, true);
    part++; // yes, consumed
  } catch (ASN1Exception e) {
    s_5 = null; // no, not present
  }

  // Decoding: preferredRecordSyntax PreferredRecordSyntax OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  try {
    s_preferredRecordSyntax = new PreferredRecordSyntax(p, true);
    part++; // yes, consumed
  } catch (ASN1Exception e) {
    s_preferredRecordSyntax = null; // no, not present
  }

  // Should not be any more parts

  if (part < num_parts) {
    throw new ASN1Exception("Zebulun PresentRequest: bad BER: extra data " + part + "/" + num_parts + " processed");
  }
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of the PresentRequest.
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
 * Returns a BER encoding of PresentRequest, implicitly tagged.
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
  if (s_referenceId != null)
    num_fields++;
  if (s_5 != null)
    num_fields++;
  if (s_preferredRecordSyntax != null)
    num_fields++;

  // Encode it

  BEREncoding fields[] = new BEREncoding[num_fields];
  int x = 0;

  // Encoding s_referenceId: ReferenceId OPTIONAL

  if (s_referenceId != null) {
    fields[x++] = s_referenceId.ber_encode();
  }

  // Encoding s_resultSetId: ResultSetId 

  fields[x++] = s_resultSetId.ber_encode();

  // Encoding s_resultSetStartPoint: INTEGER 

  fields[x++] = s_resultSetStartPoint.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 30);

  // Encoding s_numberOfRecordsRequested: INTEGER 

  fields[x++] = s_numberOfRecordsRequested.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 29);

  // Encoding s_5: ElementSetNames OPTIONAL

  if (s_5 != null) {
    fields[x++] = s_5.ber_encode();
  }

  // Encoding s_preferredRecordSyntax: PreferredRecordSyntax OPTIONAL

  if (s_preferredRecordSyntax != null) {
    fields[x++] = s_preferredRecordSyntax.ber_encode();
  }

  return new BERConstructed(tag_type, tag, fields);
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the PresentRequest. 
 */

public String
toString()
{
  StringBuffer str = new StringBuffer("{");
  int outputted = 0;

  if (s_referenceId != null) {
    str.append("referenceId ");
    str.append(s_referenceId);
    outputted++;
  }

  if (0 < outputted)
    str.append(", ");
  str.append("resultSetId ");
  str.append(s_resultSetId);
  outputted++;

  if (0 < outputted)
    str.append(", ");
  str.append("resultSetStartPoint ");
  str.append(s_resultSetStartPoint);
  outputted++;

  if (0 < outputted)
    str.append(", ");
  str.append("numberOfRecordsRequested ");
  str.append(s_numberOfRecordsRequested);
  outputted++;

  if (s_5 != null) {
    if (0 < outputted)
    str.append(", ");
    str.append(s_5);
    outputted++;
  }

  if (s_preferredRecordSyntax != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("preferredRecordSyntax ");
    str.append(s_preferredRecordSyntax);
    outputted++;
  }

  str.append("}");

  return str.toString();
}

//----------------------------------------------------------------
/*
 * Internal variables for class.
 */

public ReferenceId s_referenceId; // optional
public ResultSetId s_resultSetId;
public ASN1Integer s_resultSetStartPoint;
public ASN1Integer s_numberOfRecordsRequested;
public ElementSetNames s_5; // optional
public PreferredRecordSyntax s_preferredRecordSyntax; // optional

} // PresentRequest

//----------------------------------------------------------------
//EOF
