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
import asn1.BERConstructed;
import asn1.BEREncoding;


//================================================================
/**
 * Class for representing a <code>Records</code> from <code>IR</code>
 *
 * <pre>
 * Records ::=
 * CHOICE {
 *   dataBaseOrSurDiagnostics [28] IMPLICIT SEQUENCE OF NamePlusRecord
 *   nonSurrogateDiagnostic [130] IMPLICIT DiagRec
 * }
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class Records extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080314Z";

//----------------------------------------------------------------
/**
 * Default constructor for a Records.
 */

public
Records()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a Records from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
Records(BEREncoding ber, boolean check_tag)
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
  // Null out all choices

  c_dataBaseOrSurDiagnostics = null;
  c_nonSurrogateDiagnostic = null;

  // Try choice dataBaseOrSurDiagnostics
  if (ber.tag_get() == 28 &&
      ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    BEREncoding ber_data;
    ber_data = ber;
    BERConstructed ber_cons;
    try {
      ber_cons = (BERConstructed) ber_data;
    } catch (ClassCastException e) {
      throw new ASN1EncodingException
        ("Zebulun Records: bad BER form\n");
    }

    int num_parts = ber_cons.number_components();
    int p;

    c_dataBaseOrSurDiagnostics = new NamePlusRecord[num_parts];

    for (p = 0; p < num_parts; p++) {
      c_dataBaseOrSurDiagnostics[p] = new NamePlusRecord(ber_cons.elementAt(p), true);
    }
    return;
  }

  // Try choice nonSurrogateDiagnostic
  if (ber.tag_get() == 130 &&
      ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    c_nonSurrogateDiagnostic = new DiagRec(ber, false);
    return;
  }

  throw new ASN1Exception("Zebulun Records: bad BER encoding: choice not matched");
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of Records.
 *
 * @return	The BER encoding.
 * @exception	ASN1Exception Invalid or cannot be encoded.
 */

public BEREncoding
ber_encode()
       throws ASN1Exception
{
  BEREncoding chosen = null;

  BEREncoding f2[];
  int p;
  // Encoding choice: c_dataBaseOrSurDiagnostics
  if (c_dataBaseOrSurDiagnostics != null) {
    f2 = new BEREncoding[c_dataBaseOrSurDiagnostics.length];

    for (p = 0; p < c_dataBaseOrSurDiagnostics.length; p++) {
      f2[p] = c_dataBaseOrSurDiagnostics[p].ber_encode();
    }

    chosen = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 28, f2);
  }

  // Encoding choice: c_nonSurrogateDiagnostic
  if (c_nonSurrogateDiagnostic != null) {
    if (chosen != null)
      throw new ASN1Exception("CHOICE multiply set");
    chosen = c_nonSurrogateDiagnostic.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 130);
  }

  // Check for error of having none of the choices set
  if (chosen == null)
    throw new ASN1Exception("CHOICE not set");

  return chosen;
}

//----------------------------------------------------------------

/**
 * Generating a BER encoding of the object
 * and implicitly tagging it.
 * <p>
 * This method is for internal use only. You should use
 * the ber_encode method that does not take a parameter.
 * <p>
 * This function should never be used, because this
 * production is a CHOICE.
 * It must never have an implicit tag.
 * <p>
 * An exception will be thrown if it is called.
 *
 * @param tag_type the type of the tag.
 * @param tag the tag.
 * @exception ASN1Exception if it cannot be BER encoded.
 */

public BEREncoding
ber_encode(int tag_type, int tag)
       throws ASN1Exception
{
  // This method must not be called!

  // Method is not available because this is a basic CHOICE
  // which does not have an explicit tag on it. So it is not
  // permitted to allow something else to apply an implicit
  // tag on it, otherwise the tag identifying which CHOICE
  // it is will be overwritten and lost.

  throw new ASN1EncodingException("Zebulun Records: cannot implicitly tag");
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the Records. 
 */

public String
toString()
{
  int p;
  StringBuffer str = new StringBuffer("{");

  boolean found = false;

  if (c_dataBaseOrSurDiagnostics != null) {
    if (found)
      str.append("<ERROR: multiple CHOICE: dataBaseOrSurDiagnostics> ");
    found = true;
    str.append("dataBaseOrSurDiagnostics ");
  str.append("{");
  for (p = 0; p < c_dataBaseOrSurDiagnostics.length; p++) {
    str.append(c_dataBaseOrSurDiagnostics[p]);
  }
  str.append("}");
  }

  if (c_nonSurrogateDiagnostic != null) {
    if (found)
      str.append("<ERROR: multiple CHOICE: nonSurrogateDiagnostic> ");
    found = true;
    str.append("nonSurrogateDiagnostic ");
  str.append(c_nonSurrogateDiagnostic);
  }

  str.append("}");

  return str.toString();
}

//----------------------------------------------------------------
/*
 * Internal variables for class.
 */

public NamePlusRecord c_dataBaseOrSurDiagnostics[];
public DiagRec c_nonSurrogateDiagnostic;

} // Records

//----------------------------------------------------------------
//EOF
