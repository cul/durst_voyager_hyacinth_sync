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
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:14:28 UTC
 */

//----------------------------------------------------------------

package z3950.v2;
import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;


//================================================================
/**
 * Class for representing a <code>ResourceReportRequest</code> from <code>IR</code>
 *
 * <pre>
 * ResourceReportRequest ::=
 * SEQUENCE {
 *   referenceId ReferenceId OPTIONAL
 *   preferredResourceReportFormat [49] IMPLICIT ResourceReportId OPTIONAL
 * }
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class ResourceReportRequest extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080314Z";

//----------------------------------------------------------------
/**
 * Default constructor for a ResourceReportRequest.
 */

public
ResourceReportRequest()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a ResourceReportRequest from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
ResourceReportRequest(BEREncoding ber, boolean check_tag)
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
  // ResourceReportRequest should be encoded by a constructed BER

  BERConstructed ber_cons;
  try {
    ber_cons = (BERConstructed) ber;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun ResourceReportRequest: bad BER form\n");
  }

  // Prepare to decode the components

  int num_parts = ber_cons.number_components();
  int part = 0;
  BEREncoding p;

  // Remaining elements are optional, set variables
  // to null (not present) so can return at end of BER

  s_referenceId = null;
  s_preferredResourceReportFormat = null;

  // Decoding: referenceId ReferenceId OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  try {
    s_referenceId = new ReferenceId(p, true);
    part++; // yes, consumed
  } catch (ASN1Exception e) {
    s_referenceId = null; // no, not present
  }

  // Decoding: preferredResourceReportFormat [49] IMPLICIT ResourceReportId OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 49 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_preferredResourceReportFormat = new ResourceReportId(p, false);
    part++;
  }

  // Should not be any more parts

  if (part < num_parts) {
    throw new ASN1Exception("Zebulun ResourceReportRequest: bad BER: extra data " + part + "/" + num_parts + " processed");
  }
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of the ResourceReportRequest.
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
 * Returns a BER encoding of ResourceReportRequest, implicitly tagged.
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
  if (s_referenceId != null)
    num_fields++;
  if (s_preferredResourceReportFormat != null)
    num_fields++;

  // Encode it

  BEREncoding fields[] = new BEREncoding[num_fields];
  int x = 0;

  // Encoding s_referenceId: ReferenceId OPTIONAL

  if (s_referenceId != null) {
    fields[x++] = s_referenceId.ber_encode();
  }

  // Encoding s_preferredResourceReportFormat: ResourceReportId OPTIONAL

  if (s_preferredResourceReportFormat != null) {
    fields[x++] = s_preferredResourceReportFormat.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 49);
  }

  return new BERConstructed(tag_type, tag, fields);
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the ResourceReportRequest. 
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

  if (s_preferredResourceReportFormat != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("preferredResourceReportFormat ");
    str.append(s_preferredResourceReportFormat);
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
public ResourceReportId s_preferredResourceReportFormat; // optional

} // ResourceReportRequest

//----------------------------------------------------------------
//EOF
