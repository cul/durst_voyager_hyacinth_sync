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
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:20:29 UTC
 */

//----------------------------------------------------------------

package z3950.CIP2_4SpecificInfo;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Null;
import asn1.BEREncoding;

//================================================================
/**
 * Class for representing a <code>ChildrenResourceReport</code> from <code>CIP2-4-Release-B-APDU</code>
 *
 * <pre>
 * ChildrenResourceReport ::=
 * CHOICE {
 *   terminalCollection [1] IMPLICIT NULL
 *   nonTerminalCollection [2] IMPLICIT ChildrenReports
 * }
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class ChildrenResourceReport extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080320Z";

//----------------------------------------------------------------
/**
 * Default constructor for a ChildrenResourceReport.
 */

public
ChildrenResourceReport()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a ChildrenResourceReport from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
ChildrenResourceReport(BEREncoding ber, boolean check_tag)
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

  c_terminalCollection = null;
  c_nonTerminalCollection = null;

  // Try choice terminalCollection
  if (ber.tag_get() == 1 &&
      ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    c_terminalCollection = new ASN1Null(ber, false);
    return;
  }

  // Try choice nonTerminalCollection
  if (ber.tag_get() == 2 &&
      ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    c_nonTerminalCollection = new ChildrenReports(ber, false);
    return;
  }

  throw new ASN1Exception("Zebulun ChildrenResourceReport: bad BER encoding: choice not matched");
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of ChildrenResourceReport.
 *
 * @return	The BER encoding.
 * @exception	ASN1Exception Invalid or cannot be encoded.
 */

public BEREncoding
ber_encode()
       throws ASN1Exception
{
  BEREncoding chosen = null;

  // Encoding choice: c_terminalCollection
  if (c_terminalCollection != null) {
    chosen = c_terminalCollection.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);
  }

  // Encoding choice: c_nonTerminalCollection
  if (c_nonTerminalCollection != null) {
    if (chosen != null)
      throw new ASN1Exception("CHOICE multiply set");
    chosen = c_nonTerminalCollection.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
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

  throw new ASN1EncodingException("Zebulun ChildrenResourceReport: cannot implicitly tag");
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the ChildrenResourceReport. 
 */

public String
toString()
{
  StringBuffer str = new StringBuffer("{");

  boolean found = false;

  if (c_terminalCollection != null) {
    if (found)
      str.append("<ERROR: multiple CHOICE: terminalCollection> ");
    found = true;
    str.append("terminalCollection ");
  str.append(c_terminalCollection);
  }

  if (c_nonTerminalCollection != null) {
    if (found)
      str.append("<ERROR: multiple CHOICE: nonTerminalCollection> ");
    found = true;
    str.append("nonTerminalCollection ");
  str.append(c_nonTerminalCollection);
  }

  str.append("}");

  return str.toString();
}

//----------------------------------------------------------------
/*
 * Internal variables for class.
 */

public ASN1Null c_terminalCollection;
public ChildrenReports c_nonTerminalCollection;

} // ChildrenResourceReport

//----------------------------------------------------------------
//EOF
