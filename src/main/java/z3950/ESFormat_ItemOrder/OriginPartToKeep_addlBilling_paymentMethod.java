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
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:15:27 UTC
 */

//----------------------------------------------------------------

package z3950.ESFormat_ItemOrder;
import asn1.*;
import z3950.v3.InternationalString;

//================================================================
/**
 * Class for representing a <code>OriginPartToKeep_addlBilling_paymentMethod</code> from <code>ESFormat-ItemOrder</code>
 *
 * <pre>
 * OriginPartToKeep_addlBilling_paymentMethod ::=
 * CHOICE {
 *   billInvoice [0] IMPLICIT NULL
 *   prepay [1] IMPLICIT NULL
 *   depositAccount [2] IMPLICIT NULL
 *   creditCard [3] IMPLICIT CreditCardInfo
 *   cardInfoPreviouslySupplied [4] IMPLICIT NULL
 *   privateKnown [5] IMPLICIT NULL
 *   privateNotKnown [6] IMPLICIT EXTERNAL
 * }
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class OriginPartToKeep_addlBilling_paymentMethod extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";

//----------------------------------------------------------------
/**
 * Default constructor for a OriginPartToKeep_addlBilling_paymentMethod.
 */

public
OriginPartToKeep_addlBilling_paymentMethod()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a OriginPartToKeep_addlBilling_paymentMethod from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
OriginPartToKeep_addlBilling_paymentMethod(BEREncoding ber, boolean check_tag)
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

  c_billInvoice = null;
  c_prepay = null;
  c_depositAccount = null;
  c_creditCard = null;
  c_cardInfoPreviouslySupplied = null;
  c_privateKnown = null;
  c_privateNotKnown = null;

  // Try choice billInvoice
  if (ber.tag_get() == 0 &&
      ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    c_billInvoice = new ASN1Null(ber, false);
    return;
  }

  // Try choice prepay
  if (ber.tag_get() == 1 &&
      ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    c_prepay = new ASN1Null(ber, false);
    return;
  }

  // Try choice depositAccount
  if (ber.tag_get() == 2 &&
      ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    c_depositAccount = new ASN1Null(ber, false);
    return;
  }

  // Try choice creditCard
  if (ber.tag_get() == 3 &&
      ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    c_creditCard = new CreditCardInfo(ber, false);
    return;
  }

  // Try choice cardInfoPreviouslySupplied
  if (ber.tag_get() == 4 &&
      ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    c_cardInfoPreviouslySupplied = new ASN1Null(ber, false);
    return;
  }

  // Try choice privateKnown
  if (ber.tag_get() == 5 &&
      ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    c_privateKnown = new ASN1Null(ber, false);
    return;
  }

  // Try choice privateNotKnown
  if (ber.tag_get() == 6 &&
      ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    c_privateNotKnown = new ASN1External(ber, false);
    return;
  }

  throw new ASN1Exception("Zebulun OriginPartToKeep_addlBilling_paymentMethod: bad BER encoding: choice not matched");
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of OriginPartToKeep_addlBilling_paymentMethod.
 *
 * @return	The BER encoding.
 * @exception	ASN1Exception Invalid or cannot be encoded.
 */

public BEREncoding
ber_encode()
       throws ASN1Exception
{
  BEREncoding chosen = null;

  // Encoding choice: c_billInvoice
  if (c_billInvoice != null) {
    chosen = c_billInvoice.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 0);
  }

  // Encoding choice: c_prepay
  if (c_prepay != null) {
    if (chosen != null)
      throw new ASN1Exception("CHOICE multiply set");
    chosen = c_prepay.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);
  }

  // Encoding choice: c_depositAccount
  if (c_depositAccount != null) {
    if (chosen != null)
      throw new ASN1Exception("CHOICE multiply set");
    chosen = c_depositAccount.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
  }

  // Encoding choice: c_creditCard
  if (c_creditCard != null) {
    if (chosen != null)
      throw new ASN1Exception("CHOICE multiply set");
    chosen = c_creditCard.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);
  }

  // Encoding choice: c_cardInfoPreviouslySupplied
  if (c_cardInfoPreviouslySupplied != null) {
    if (chosen != null)
      throw new ASN1Exception("CHOICE multiply set");
    chosen = c_cardInfoPreviouslySupplied.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 4);
  }

  // Encoding choice: c_privateKnown
  if (c_privateKnown != null) {
    if (chosen != null)
      throw new ASN1Exception("CHOICE multiply set");
    chosen = c_privateKnown.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 5);
  }

  // Encoding choice: c_privateNotKnown
  if (c_privateNotKnown != null) {
    if (chosen != null)
      throw new ASN1Exception("CHOICE multiply set");
    chosen = c_privateNotKnown.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 6);
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

  throw new ASN1EncodingException("Zebulun OriginPartToKeep_addlBilling_paymentMethod: cannot implicitly tag");
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the OriginPartToKeep_addlBilling_paymentMethod. 
 */

public String
toString()
{
  StringBuffer str = new StringBuffer("{");

  boolean found = false;

  if (c_billInvoice != null) {
    if (found)
      str.append("<ERROR: multiple CHOICE: billInvoice> ");
    found = true;
    str.append("billInvoice ");
  str.append(c_billInvoice);
  }

  if (c_prepay != null) {
    if (found)
      str.append("<ERROR: multiple CHOICE: prepay> ");
    found = true;
    str.append("prepay ");
  str.append(c_prepay);
  }

  if (c_depositAccount != null) {
    if (found)
      str.append("<ERROR: multiple CHOICE: depositAccount> ");
    found = true;
    str.append("depositAccount ");
  str.append(c_depositAccount);
  }

  if (c_creditCard != null) {
    if (found)
      str.append("<ERROR: multiple CHOICE: creditCard> ");
    found = true;
    str.append("creditCard ");
  str.append(c_creditCard);
  }

  if (c_cardInfoPreviouslySupplied != null) {
    if (found)
      str.append("<ERROR: multiple CHOICE: cardInfoPreviouslySupplied> ");
    found = true;
    str.append("cardInfoPreviouslySupplied ");
  str.append(c_cardInfoPreviouslySupplied);
  }

  if (c_privateKnown != null) {
    if (found)
      str.append("<ERROR: multiple CHOICE: privateKnown> ");
    found = true;
    str.append("privateKnown ");
  str.append(c_privateKnown);
  }

  if (c_privateNotKnown != null) {
    if (found)
      str.append("<ERROR: multiple CHOICE: privateNotKnown> ");
    found = true;
    str.append("privateNotKnown ");
  str.append(c_privateNotKnown);
  }

  str.append("}");

  return str.toString();
}

//----------------------------------------------------------------
/*
 * Internal variables for class.
 */

public ASN1Null c_billInvoice;
public ASN1Null c_prepay;
public ASN1Null c_depositAccount;
public CreditCardInfo c_creditCard;
public ASN1Null c_cardInfoPreviouslySupplied;
public ASN1Null c_privateKnown;
public ASN1External c_privateNotKnown;

} // OriginPartToKeep_addlBilling_paymentMethod

//----------------------------------------------------------------
//EOF
