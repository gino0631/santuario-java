/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "<WebSig>" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 2001, Institute for
 * Data Communications Systems, <http://www.nue.et-inf.uni-siegen.de/>.
 * The development of this software was partly funded by the European
 * Commission in the <WebSig> project in the ISIS Programme.
 * For more information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.xml.security.utils;



import java.io.*;
import java.util.Vector;
import java.math.BigInteger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.*;
import org.apache.xml.security.c14n.*;
import org.apache.xml.security.exceptions.*;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.utils.Base64;
import org.apache.xml.security.utils.Constants;
import org.apache.xml.security.utils.HelperNodeList;
import org.apache.xpath.XPathAPI;
import javax.xml.transform.TransformerException;


/**
 * DOM and XML accessibility and comfort functions.
 *
 * @author Christian Geuer-Pollmann
 */
public class XMLUtils {

   /** {@link org.apache.log4j} logging facility */
   static org.apache.log4j.Category cat =
      org.apache.log4j.Category.getInstance(XMLUtils.class.getName());

   public static String getXalanVersion() {
      // return "Apache " + org.apache.xalan.processor.XSLProcessorVersion.S_VERSION;
      return "Apache " + org.apache.xalan.Version.getVersion();
   }

   public static String getXercesVersion() {
      return "Apache " + org.apache.xerces.framework.Version.fVersion;
   }

   /**
    * Method spitOutVersions
    *
    * @param cat
    */
   public static void spitOutVersions(org.apache.log4j.Category cat) {
      cat.debug(XMLUtils.getXercesVersion());
      cat.debug(XMLUtils.getXalanVersion());
   }

   /** Field nodeTypeString */
   private static String[] nodeTypeString = new String[]{ "", "ELEMENT",
                                                          "ATTRIBUTE",
                                                          "TEXT_NODE",
                                                          "CDATA_SECTION",
                                                          "ENTITY_REFERENCE",
                                                          "ENTITY",
                                                          "PROCESSING_INSTRUCTION",
                                                          "COMMENT", "DOCUMENT",
                                                          "DOCUMENT_TYPE",
                                                          "DOCUMENT_FRAGMENT",
                                                          "NOTATION" };

   /**
    * Transforms <code>org.w3c.dom.Node.XXX_NODE</code> NodeType values into
    * Strings.
    *
    * @param nodeType as taken from the {@link org.w3c.dom.Node#getNodeType} function
    * @return the String value.
    * @see org.w3c.dom.Node#getNodeType
    */
   public static String getNodeTypeString(short nodeType) {

      if ((nodeType > 0) && (nodeType < 13)) {
         return nodeTypeString[nodeType];
      } else {
         return "";
      }
   }

   /**
    * Method getNodeTypeString
    *
    * @param n
    * @return
    */
   public static String getNodeTypeString(Node n) {
      return getNodeTypeString(n.getNodeType());
   }

   /**
    * Prints a sub-tree to standard out.
    *
    * @param ctxNode
    *
    *  try {
    *     Document doc = contextNode.getOwnerDocument();
    *     OutputFormat format = new OutputFormat(doc);
    *     StringWriter stringOut = new StringWriter();
    *     XMLSerializer serial = new XMLSerializer(stringOut, format);
    *
    *     serial.asDOMSerializer();
    *     serial.serialize(doc.getDocumentElement());
    *     os.write(stringOut.toString());
    *  } catch (Exception ex) {
    *     ex.printStackTrace();
    *  }
    * }
    * @return
    */
   public static Vector getAncestorElements(Node ctxNode) {

      if (ctxNode.getNodeType() != Node.ELEMENT_NODE) {
         return null;
      }

      Vector ancestorVector = new Vector();
      Node parent = ctxNode;

      while ((parent = parent.getParentNode()) != null
             && (parent.getNodeType() == Node.ELEMENT_NODE)) {
         ancestorVector.add(parent);
      }

      ancestorVector.trimToSize();

      return ancestorVector;
   }

   /**
    * Method getDirectChildrenElements
    *
    * @param parentElement
    * @return
    */
   public static NodeList getDirectChildrenElements(Element parentElement) {

      NodeList allNodes = parentElement.getChildNodes();
      HelperNodeList selectedNodes = new HelperNodeList();

      for (int i = 0; i < allNodes.getLength(); i++) {
         Node currentNode = allNodes.item(i);

         if ((currentNode.getNodeType() == Node.ELEMENT_NODE)) {
            selectedNodes.appendChild(currentNode);
         }
      }

      return selectedNodes;
   }

   /**
    * Outputs a DOM tree to a file.
    *
    * @param contextNode root node of the DOM tree
    * @param filename the file name
    * @throws java.io.FileNotFoundException
    */
   public static void outputDOM(Node contextNode, String filename)
           throws java.io.FileNotFoundException {

      OutputStream os = new FileOutputStream(filename);

      outputDOM(contextNode, os);
   }

   /**
    * Outputs a DOM tree to an {@link OutputStream}.
    *
    * @param contextNode root node of the DOM tree
    * @param os the {@link OutputStream}
    */
   public static void outputDOM(Node contextNode, OutputStream os) {

      try {
         TransformerFactory tFactory = TransformerFactory.newInstance();
         Transformer transformer = tFactory.newTransformer();

         transformer
            .setOutputProperty(javax.xml.transform.OutputKeys
               .OMIT_XML_DECLARATION, "yes");

         DOMSource source = new DOMSource(contextNode);
         StreamResult result = new StreamResult(os);

         transformer.transform(source, result);
      } catch (TransformerConfigurationException e) {
         e.printStackTrace();
      } catch (TransformerException e) {
         e.printStackTrace();
      }
   }

   /**
    * Method outputDOMc14n
    *
    * @param contextNode
    * @param os
    */
   public static void outputDOMc14nWithComments(Node contextNode,
           OutputStream os) {

      try {
         os.write(Canonicalizer
            .getInstance(Canonicalizer.ALGO_ID_C14N_WITH_COMMENTS)
               .canonicalize(contextNode));
      } catch (IOException ex) {}
      catch (InvalidCanonicalizerException ex) {}
      catch (CanonicalizationException ex) {}
   }

   /**
    * Converts a single {@link Node} into a {@link NodeList} which contains only that {@link Node}
    *
    * @param node the Node
    * @return the NodeList
    */
   public static NodeList elementToNodeList(Node node) {

      HelperNodeList nl = new HelperNodeList();

      nl.appendChild(node);

      return (NodeList) nl;
   }

   /**
    * Creates Attributes {@link org.w3c.dom.Attr} in the given namespace
    * (if possible). If the namespace is empty, only the QName is used.
    *
    * @param doc the generator (factory) Document
    * @param QName the QName of the Attr
    * @param Value the String value of the Attr
    * @param NamespaceURI the namespace for the Attr
    * @return the Attr
    */
   public static Attr createAttr(Document doc, String QName, String Value,
                                 String NamespaceURI) {

      Attr attr = null;

      if ((NamespaceURI != null) && (NamespaceURI.length() > 0)) {
         attr = doc.createAttributeNS(NamespaceURI, QName);
      } else {
         attr = doc.createAttribute(QName);
      }

      attr.appendChild(doc.createTextNode(Value));

      return attr;
   }

   /**
    * Sets the Attribute QName with Value in Element elem.
    *
    * @param elem the Element which has to contain the Attribute
    * @param QName the QName of the Attribute
    * @param Value the value of the Attribute
    */
   public static void setAttr(Element elem, String QName, String Value) {

      Document doc = elem.getOwnerDocument();
      Attr attr = doc.createAttributeNS(Constants.SignatureSpecNS, QName);

      attr.appendChild(doc.createTextNode(Value));
      elem.setAttributeNode(attr);
   }

   /**
    * Creates an Element from a BigInteger. The BigInteger is base64-encoded
    * and put into the Element with a given name.
    *
    * See
    * <A HREF="http://www.w3.org/TR/2001/CR-xmldsig-core-20010419/#sec-CryptoBinary">Section
    * 4.0.1 The ds:CryptoBinary Simple Type</A>:
    *
    * This specification defines the ds:CryptoBinary simple type for
    * representing arbitrary-length integers (e.g. "bignums") in XML as
    * octet strings. The integer value is first converted to a "big
    * endian" bitstring. The bitstring is then padded with leading zero
    * bits so that the total number of bits == 0 mod 8 (so that there are
    * an integral number of octets). If the bitstring contains entire
    * leading octets that are zero, these are removed (so the high-order
    * octet is always non-zero). This octet string is then base64 [MIME]
    * encoded. (The conversion from integer to octet string is equivalent
    * to IEEE 1363's I2OSP [1363] with minimal length).
    *
    *
    * @param doc the factory Document
    * @param elementName the name of the Element
    * @param bigInteger the BigInteger wo be inserted
    * @return the Element
    * @throws XMLSignatureException if bigInteger is not positive
    */
   public static Element createElementFromBigint(
           Document doc, String elementName, BigInteger bigInteger)
              throws XMLSignatureException {

      Element element = doc.createElementNS(Constants.SignatureSpecNS,
                                            Constants.getSignatureSpecNSprefix()
                                            + ":" + elementName);

      /* bigInteger must be positive */
      if (bigInteger.signum() != 1) {
         throw new XMLSignatureException("signature.Util.BignumNonPositive");
      }

      byte byteRepresentation[] = bigInteger.toByteArray();

      while (byteRepresentation[0] == 0) {
         byte oldByteRepresentation[] = byteRepresentation;

         byteRepresentation = new byte[oldByteRepresentation.length - 1];

         System.arraycopy(oldByteRepresentation, 1, byteRepresentation, 0,
                          oldByteRepresentation.length - 1);
      }

      Text text =
         doc.createTextNode(org.apache.xml.security.utils.Base64
            .encode(byteRepresentation));

      element.appendChild(text);

      return element;
   }

   /**
    * Fetches a base64-encoded BigInteger from an Element.
    *
    * @param element the Element
    * @return the BigInteger
    * @throws XMLSignatureException if Element has not exactly one Text child
    */
   public static BigInteger getBigintFromElement(Element element)
           throws XMLSignatureException {

      if (element.getChildNodes().getLength() != 1) {
         throw new XMLSignatureException("signature.Util.TooManyChilds");
      }

      Node child = element.getFirstChild();

      if ((child == null) || (child.getNodeType() != Node.TEXT_NODE)) {
         throw new XMLSignatureException("signature.Util.NonTextNode");
      }

      Text text = (Text) child;
      String textData = text.getData();
      byte magnitude[] = org.apache.xml.security.utils.Base64.decode(textData);
      int signum = 1;
      BigInteger bigInteger = new BigInteger(signum, magnitude);

      return bigInteger;
   }

   /**
    * Fetches base64-encoded byte[] data from an Element.
    *
    * @param element
    * @return the byte[] data
    * @throws XMLSignatureException if Element has not exactly one Text child
    */
   public static byte[] getBytesFromElement(Element element)
           throws XMLSignatureException {

      if (element.getChildNodes().getLength() != 1) {
         throw new XMLSignatureException("signature.Util.TooManyChilds");
      }

      Node child = element.getFirstChild();

      if ((child == null) || (child.getNodeType() != Node.TEXT_NODE)) {
         throw new XMLSignatureException("signature.Util.NonTextNode");
      }

      Text text = (Text) child;
      String textData = text.getData();
      byte bytes[] = org.apache.xml.security.utils.Base64.decode(textData);

      return bytes;
   }

   /**
    * Creates an Element in the XML Signature specification namespace.
    *
    * @param doc the factory Document
    * @param elementName the local name of the Element
    * @return the Element
    */
   public static Element createElementInSignatureSpace(Document doc,
           String elementName) {

      if (doc == null) {
         throw new RuntimeException("Document is null");
      }

      String ds = Constants.getSignatureSpecNSprefix();

      if ((ds == null) || (ds.length() == 0)) {
         Element element = doc.createElementNS(Constants.SignatureSpecNS,
                                               elementName);

         element.setAttribute("xmlns", Constants.SignatureSpecNS);

         return element;
      } else {
         Element element = doc.createElementNS(Constants.SignatureSpecNS,
                                               ds + ":" + elementName);

         element.setAttribute("xmlns:" + ds, Constants.SignatureSpecNS);

         return element;
      }
   }

   /**
    * Creates an Element in the XML Encryption specification namespace.
    *
    * @param doc the factory Document
    * @param elementName the local name of the Element
    * @return the Element
    */
   public static Element createElementInEncryptionSpace(Document doc,
           String elementName) {

      if (doc == null) {
         throw new RuntimeException("Document is null");
      }

      String xenc = EncryptionConstants.getEncryptionSpecNSprefix();

      if ((xenc == null) || (xenc.length() == 0)) {
         Element element =
            doc.createElementNS(EncryptionConstants.EncryptionSpecNS,
                                elementName);

         element.setAttribute("xmlns", Constants.SignatureSpecNS);

         return element;
      } else {
         Element element =
            doc.createElementNS(EncryptionConstants.EncryptionSpecNS,
                                xenc + ":" + elementName);

         element.setAttribute("xmlns:" + xenc,
                              EncryptionConstants.EncryptionSpecNS);

         return element;
      }
   }

   /**
    * Returns true if the element is in XML Signature namespace and the local
    * name equals the supplied one.
    *
    * @param element
    * @param localName
    * @return true if the element is in XML Signature namespace and the local name equals the supplied one
    */
   public static boolean elementIsInSignatureSpace(Element element,
           String localName) {

      if (element == null) {
         return false;
      }

      if (element.getNamespaceURI() == null) {
         return false;
      }

      if (!element.getNamespaceURI().equals(Constants.SignatureSpecNS)) {
         return false;
      }

      if (!element.getLocalName().equals(localName)) {
         return false;
      }

      return true;
   }

   /**
    * Returns true if the element is in XML Encryption namespace and the local
    * name equals the supplied one.
    *
    * @param element
    * @param localName
    * @return true if the element is in XML Encryption namespace and the local name equals the supplied one
    */
   public static boolean elementIsInEncryptionSpace(Element element,
           String localName) {

      if (element == null) {
         return false;
      }

      if (element.getNamespaceURI() == null) {
         return false;
      }

      if (!element.getNamespaceURI()
              .equals(EncryptionConstants.EncryptionSpecNS)) {
         return false;
      }

      if (!element.getLocalName().equals(localName)) {
         return false;
      }

      return true;
   }

   /**
    * Verifies that the given Element is in the XML Signature namespace
    * {@link org.apache.xml.security.utils.Constants#SignatureSpecNS} and that the
    * local name of the Element matches the supplied on.
    *
    * @param element Element to be checked
    * @param localName
    * @throws XMLSignatureException if element is not in Signature namespace or if the local name does not match
    * @see org.apache.xml.security.utils.Constants#SignatureSpecNS
    */
   public static void guaranteeThatElementInSignatureSpace(
           Element element, String localName) throws XMLSignatureException {

      /*
      cat.debug("guaranteeThatElementInSignatureSpace(" + element + ", "
                + localName + ")");
      */
      if (element == null) {
         Object exArgs[] = { localName, null };

         throw new XMLSignatureException("xml.WrongElement", exArgs);
      }

      if ((localName == null) || localName.equals("")
              ||!elementIsInSignatureSpace(element, localName)) {
         Object exArgs[] = { localName, element.getLocalName() };

         throw new XMLSignatureException("xml.WrongElement", exArgs);
      }
   }

   /**
    * Verifies that the given Element is in the XML Encryption namespace
    * {@link org.apache.xml.security.utils.Constants#EncryptionSpecNS} and that the
    * local name of the Element matches the supplied on.
    *
    * @param element Element to be checked
    * @param localName
    * @throws XMLSecurityException if element is not in Encryption namespace or if the local name does not match
    * @see org.apache.xml.security.utils.Constants#EncryptionSpecNS
    */
   public static void guaranteeThatElementInEncryptionSpace(
           Element element, String localName) throws XMLSecurityException {

      if (element == null) {
         Object exArgs[] = { localName, null };

         throw new XMLSecurityException("xml.WrongElement", exArgs);
      }

      if ((localName == null) || localName.equals("")
              ||!elementIsInEncryptionSpace(element, localName)) {
         Object exArgs[] = { localName, element.getLocalName() };

         throw new XMLSecurityException("xml.WrongElement", exArgs);
      }
   }

   /**
    * This method returns the owner document of a particular node.
    * This method is necessary because it <I>always</I> returns a
    * {@link Document}. {@link Node#getOwnerDocument} returns <CODE>null</CODE>
    * if the {@link Node} is a {@link Document}.
    *
    * @param node
    * @return the owner document of the node
    */
   public static Document getOwnerDocument(Node node) {

      if (node.getNodeType() == Node.DOCUMENT_NODE) {
         return (Document) node;
      } else {
         return node.getOwnerDocument();
      }
   }

   /** Field randomNS */
   private static String randomNS = null;

   /**
    * Prefix for random namespaces.
    *
    * @see #getRandomNamespace
    */
   public static final String randomNSprefix =
      "http://www.xmlsecurity.org/NS#randomval";

   /**
    * This method creates a random String like
    * <CODE>http://www.xmlsecurity.org/NS#randomval8dcc/C2qwxFukXjJhS7W1xvHHq4Z</CODE>
    * that will be used for registering the <CODE>here()</CODE> function in a
    * specific namespace. The random string is the Base64 encoded version of a
    * 168 bit {@link java.security.SecureRandom} value.
    * <BR/>
    * This random namespace prefix prevents attackers from inserting malicious
    * here() functions in our namespace. The method caches the valued for
    * subsequent calls during the application run.
    *
    * @return the random namespace prefix String.
    */
   public static String getRandomNamespacePrefix() {

      if (XMLUtils.randomNS == null) {
         byte[] randomData = new byte[21];
         java.security.SecureRandom sr = new java.security.SecureRandom();

         sr.nextBytes(randomData);

         String prefix =
            "xmlsecurityOrgPref"
            + org.apache.xml.security.utils.Base64.encode(randomData);

         XMLUtils.randomNS = "";

         for (int i = 0; i < prefix.length(); i++) {
            if ((prefix.charAt(i) != '+') && (prefix.charAt(i) != '/')
                    && (prefix.charAt(i) != '=')) {
               XMLUtils.randomNS += prefix.charAt(i);
            }
         }
      }

      return XMLUtils.randomNS;
   }

   /**
    * Method createDSctx
    *
    * @param doc
    * @param prefix
    * @param namespace
    * @return
    */
   public static Element createDSctx(Document doc, String prefix,
                                     String namespace) {

      Element ctx = doc.createElement("namespaceContext");

      ctx.setAttribute("xmlns:" + prefix.trim(), namespace);

      return ctx;
   }

   /**
    * Method createDSctx
    *
    * @param doc
    * @param prefix
    * @return
    */
   public static Element createDSctx(Document doc, String prefix) {
      return XMLUtils.createDSctx(doc, prefix, Constants.SignatureSpecNS);
   }

   /**
    * Method indentSignature
    *
    * @param element
    * @param indentString
    * @param initialDepth
    */
   public static void indentSignature(Element element, String indentString,
                                      int initialDepth) {

      try {
         NodeList returns = XPathAPI.selectNodeList(element, ".//text()");

         for (int i = 0; i < returns.getLength(); i++) {
            Text returnText = (Text) returns.item(i);
            Element parent = (Element) returnText.getParentNode();
            Document doc = returnText.getOwnerDocument();
            int j = 0;

            while (parent != element) {
               j++;
            }

            String newReturn = "";

            for (int k = 0; k < j; k++) {
               newReturn += indentString;
            }

            Text newReturnText = doc.createTextNode(newReturn);

            parent.replaceChild(newReturnText, returnText);
         }
      } catch (TransformerException ex) {}
   }

   static {
      org.apache.xml.security.Init.init();
   }
}
