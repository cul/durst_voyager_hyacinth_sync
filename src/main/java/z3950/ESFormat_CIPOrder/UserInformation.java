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
import asn1.*;
import z3950.v3.InternationalString;

//================================================================
/**
 * Class for representing a <code>UserInformation</code> from <code>CIP-Order-ES</code>
 *
 * <pre>
 * UserInformation ::=
 * SEQUENCE {
 *   userId [1] EXPLICIT InternationalString
 *   userName [2] EXPLICIT InternationalString OPTIONAL
 *   userAddress [3] EXPLICIT PostalAddress OPTIONAL
 *   telNumber [4] EXPLICIT InternationalString OPTIONAL
 *   faxNumber [5] EXPLICIT InternationalString OPTIONAL
 *   emailAddress [6] EXPLICIT InternationalString OPTIONAL
 *   networkAddress [7] EXPLICIT InternationalString OPTIONAL
 *   account [8] EXPLICIT InternationalString OPTIONAL
 *   billing [9] EXPLICIT Billing OPTIONAL
 * }
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class UserInformation extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080320Z";

//----------------------------------------------------------------
/**
 * Default constructor for a UserInformation.
 */

public
UserInformation()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a UserInformation from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
UserInformation(BEREncoding ber, boolean check_tag)
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
  // UserInformation should be encoded by a constructed BER

  BERConstructed ber_cons;
  try {
    ber_cons = (BERConstructed) ber;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun UserInformation: bad BER form\n");
  }

  // Prepare to decode the components

  int num_parts = ber_cons.number_components();
  int part = 0;
  BEREncoding p;
  BERConstructed tagged;

  // Decoding: userId [1] EXPLICIT InternationalString

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun UserInformation: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 1 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun UserInformation: bad tag in s_userId\n");

  try {
    tagged = (BERConstructed) p;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun UserInformation: bad BER encoding: s_userId tag bad\n");
  }
  if (tagged.number_components() != 1) {
    throw new ASN1EncodingException
      ("Zebulun UserInformation: bad BER encoding: s_userId tag bad\n");
  }

  s_userId = new InternationalString(tagged.elementAt(0), true);
  part++;

  // Remaining elements are optional, set variables
  // to null (not present) so can return at end of BER

  s_userName = null;
  s_userAddress = null;
  s_telNumber = null;
  s_faxNumber = null;
  s_emailAddress = null;
  s_networkAddress = null;
  s_account = null;
  s_billing = null;

  // Decoding: userName [2] EXPLICIT InternationalString OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 2 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      tagged = (BERConstructed) p;
    } catch (ClassCastException e) {
      throw new ASN1EncodingException
        ("Zebulun UserInformation: bad BER encoding: s_userName tag bad\n");
    }
    if (tagged.number_components() != 1) {
      throw new ASN1EncodingException
        ("Zebulun UserInformation: bad BER encoding: s_userName tag bad\n");
    }

    s_userName = new InternationalString(tagged.elementAt(0), true);
    part++;
  }

  // Decoding: userAddress [3] EXPLICIT PostalAddress OPTIONAL

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
        ("Zebulun UserInformation: bad BER encoding: s_userAddress tag bad\n");
    }
    if (tagged.number_components() != 1) {
      throw new ASN1EncodingException
        ("Zebulun UserInformation: bad BER encoding: s_userAddress tag bad\n");
    }

    s_userAddress = new PostalAddress(tagged.elementAt(0), true);
    part++;
  }

  // Decoding: telNumber [4] EXPLICIT InternationalString OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 4 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      tagged = (BERConstructed) p;
    } catch (ClassCastException e) {
      throw new ASN1EncodingException
        ("Zebulun UserInformation: bad BER encoding: s_telNumber tag bad\n");
    }
    if (tagged.number_components() != 1) {
      throw new ASN1EncodingException
        ("Zebulun UserInformation: bad BER encoding: s_telNumber tag bad\n");
    }

    s_telNumber = new InternationalString(tagged.elementAt(0), true);
    part++;
  }

  // Decoding: faxNumber [5] EXPLICIT InternationalString OPTIONAL

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
        ("Zebulun UserInformation: bad BER encoding: s_faxNumber tag bad\n");
    }
    if (tagged.number_components() != 1) {
      throw new ASN1EncodingException
        ("Zebulun UserInformation: bad BER encoding: s_faxNumber tag bad\n");
    }

    s_faxNumber = new InternationalString(tagged.elementAt(0), true);
    part++;
  }

  // Decoding: emailAddress [6] EXPLICIT InternationalString OPTIONAL

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
        ("Zebulun UserInformation: bad BER encoding: s_emailAddress tag bad\n");
    }
    if (tagged.number_components() != 1) {
      throw new ASN1EncodingException
        ("Zebulun UserInformation: bad BER encoding: s_emailAddress tag bad\n");
    }

    s_emailAddress = new InternationalString(tagged.elementAt(0), true);
    part++;
  }

  // Decoding: networkAddress [7] EXPLICIT InternationalString OPTIONAL

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
        ("Zebulun UserInformation: bad BER encoding: s_networkAddress tag bad\n");
    }
    if (tagged.number_components() != 1) {
      throw new ASN1EncodingException
        ("Zebulun UserInformation: bad BER encoding: s_networkAddress tag bad\n");
    }

    s_networkAddress = new InternationalString(tagged.elementAt(0), true);
    part++;
  }

  // Decoding: account [8] EXPLICIT InternationalString OPTIONAL

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
        ("Zebulun UserInformation: bad BER encoding: s_account tag bad\n");
    }
    if (tagged.number_components() != 1) {
      throw new ASN1EncodingException
        ("Zebulun UserInformation: bad BER encoding: s_account tag bad\n");
    }

    s_account = new InternationalString(tagged.elementAt(0), true);
    part++;
  }

  // Decoding: billing [9] EXPLICIT Billing OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 9 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      tagged = (BERConstructed) p;
    } catch (ClassCastException e) {
      throw new ASN1EncodingException
        ("Zebulun UserInformation: bad BER encoding: s_billing tag bad\n");
    }
    if (tagged.number_components() != 1) {
      throw new ASN1EncodingException
        ("Zebulun UserInformation: bad BER encoding: s_billing tag bad\n");
    }

    s_billing = new Billing(tagged.elementAt(0), true);
    part++;
  }

  // Should not be any more parts

  if (part < num_parts) {
    throw new ASN1Exception("Zebulun UserInformation: bad BER: extra data " + part + "/" + num_parts + " processed");
  }
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of the UserInformation.
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
 * Returns a BER encoding of UserInformation, implicitly tagged.
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
  if (s_userName != null)
    num_fields++;
  if (s_userAddress != null)
    num_fields++;
  if (s_telNumber != null)
    num_fields++;
  if (s_faxNumber != null)
    num_fields++;
  if (s_emailAddress != null)
    num_fields++;
  if (s_networkAddress != null)
    num_fields++;
  if (s_account != null)
    num_fields++;
  if (s_billing != null)
    num_fields++;

  // Encode it

  BEREncoding fields[] = new BEREncoding[num_fields];
  int x = 0;
  BEREncoding enc[];

  // Encoding s_userId: InternationalString 

  enc = new BEREncoding[1];
  enc[0] = s_userId.ber_encode();
  fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 1, enc);

  // Encoding s_userName: InternationalString OPTIONAL

  if (s_userName != null) {
    enc = new BEREncoding[1];
    enc[0] = s_userName.ber_encode();
    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 2, enc);
  }

  // Encoding s_userAddress: PostalAddress OPTIONAL

  if (s_userAddress != null) {
    enc = new BEREncoding[1];
    enc[0] = s_userAddress.ber_encode();
    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 3, enc);
  }

  // Encoding s_telNumber: InternationalString OPTIONAL

  if (s_telNumber != null) {
    enc = new BEREncoding[1];
    enc[0] = s_telNumber.ber_encode();
    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 4, enc);
  }

  // Encoding s_faxNumber: InternationalString OPTIONAL

  if (s_faxNumber != null) {
    enc = new BEREncoding[1];
    enc[0] = s_faxNumber.ber_encode();
    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 5, enc);
  }

  // Encoding s_emailAddress: InternationalString OPTIONAL

  if (s_emailAddress != null) {
    enc = new BEREncoding[1];
    enc[0] = s_emailAddress.ber_encode();
    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 6, enc);
  }

  // Encoding s_networkAddress: InternationalString OPTIONAL

  if (s_networkAddress != null) {
    enc = new BEREncoding[1];
    enc[0] = s_networkAddress.ber_encode();
    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 7, enc);
  }

  // Encoding s_account: InternationalString OPTIONAL

  if (s_account != null) {
    enc = new BEREncoding[1];
    enc[0] = s_account.ber_encode();
    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 8, enc);
  }

  // Encoding s_billing: Billing OPTIONAL

  if (s_billing != null) {
    enc = new BEREncoding[1];
    enc[0] = s_billing.ber_encode();
    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 9, enc);
  }

  return new BERConstructed(tag_type, tag, fields);
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the UserInformation. 
 */

public String
toString()
{
  StringBuffer str = new StringBuffer("{");
  int outputted = 0;

  str.append("userId ");
  str.append(s_userId);
  outputted++;

  if (s_userName != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("userName ");
    str.append(s_userName);
    outputted++;
  }

  if (s_userAddress != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("userAddress ");
    str.append(s_userAddress);
    outputted++;
  }

  if (s_telNumber != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("telNumber ");
    str.append(s_telNumber);
    outputted++;
  }

  if (s_faxNumber != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("faxNumber ");
    str.append(s_faxNumber);
    outputted++;
  }

  if (s_emailAddress != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("emailAddress ");
    str.append(s_emailAddress);
    outputted++;
  }

  if (s_networkAddress != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("networkAddress ");
    str.append(s_networkAddress);
    outputted++;
  }

  if (s_account != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("account ");
    str.append(s_account);
    outputted++;
  }

  if (s_billing != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("billing ");
    str.append(s_billing);
    outputted++;
  }

  str.append("}");

  return str.toString();
}

//----------------------------------------------------------------
/*
 * Internal variables for class.
 */

public InternationalString s_userId;
public InternationalString s_userName; // optional
public PostalAddress s_userAddress; // optional
public InternationalString s_telNumber; // optional
public InternationalString s_faxNumber; // optional
public InternationalString s_emailAddress; // optional
public InternationalString s_networkAddress; // optional
public InternationalString s_account; // optional
public Billing s_billing; // optional

} // UserInformation

//----------------------------------------------------------------
//EOF
