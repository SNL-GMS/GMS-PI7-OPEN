package gms.dataacquisition.seedlink.clientlibrary;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Utils {
	public static byte[] fillBuffer(byte[] buf,int off,int len,InputStream is)
			throws IOException{
		int read = 0;
		while(read < len) read += is.read(buf,off+read,len-read);
		return buf;
	}
	
	public static byte[] fillBuffer(byte[] buf,InputStream is)
			throws IOException{
		fillBuffer(buf,0,buf.length,is);
		return buf;
	}
	
	public static String nextString(InputStream is, byte[] buf, int len,
			String fieldName) throws IOException{
		//if(is.read(buf,0,len) != len) throw new IOException(
		//		"reached end of stream while reading "+fieldName);
		Utils.fillBuffer(buf, 0, len, is);
		return new String(buf,0,len,StandardCharsets.US_ASCII);
	}
	
	public static byte nextByte(InputStream is, String fieldName)
			throws IOException{
		try{
			return (byte)is.read();
		} catch (IOException e){
			throw new IOException("exception thrown while reading "+fieldName);
		}
	}
	
	public static byte[] nextBytes(InputStream is, byte[] buf, int len,
			String fieldName) throws IOException{
		Utils.fillBuffer(buf, 0, len, is);
		return Arrays.copyOf(buf,len);
	}
	
	public static short nextShort(DataInputStream is, String fieldName)
			throws IOException{
		try{
			return (short)is.readShort();
		} catch (IOException e){
			throw new IOException("exception thrown while reading "+fieldName);
		}
	}
	
	public static int nextInt(DataInputStream is, String fieldName)
			throws IOException{
		try{
			return is.readInt();
		} catch (IOException e){
			throw new IOException("exception thrown while reading "+fieldName);
		}
	}

	public static String formatXml(String xml, int indent)
			throws UnsupportedEncodingException, SAXException, IOException,
			ParserConfigurationException, XPathExpressionException,
			TransformerException{
		// Turn xml string into a document
		Document document = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder()
				.parse(new InputSource(new ByteArrayInputStream(xml.getBytes("utf-8"))));

		// Remove whitespaces outside tags
		document.normalize();
		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList nodeList = (NodeList) xPath.evaluate("//text()[normalize-space()='']",
				document,
				XPathConstants.NODESET);

		for (int i = 0; i < nodeList.getLength(); ++i) {
			Node node = nodeList.item(i);
			node.getParentNode().removeChild(node);
		}

		// Setup pretty print options
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		transformerFactory.setAttribute("indent-number", indent);
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		// Return pretty print xml string
		StringWriter stringWriter = new StringWriter();
		transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
		return stringWriter.toString();
	}
	
	public static Document parseXml(InputStream is)
			throws IOException, SAXException, ParserConfigurationException{
		return DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.parse(is);
	}
	
	public static Document parseXml(String xml)
			throws IOException, SAXException, ParserConfigurationException{
		return parseXml(new ByteArrayInputStream(xml.getBytes()));
	}
	
	public static String checkLength(String varName, String val, int len){
		if(val.length() != len) throw new IllegalArgumentException(
			varName+" must be length "+len+" but was length "+val.length());
		return val;
	}
	
	public static byte[] checkLength(String varName, byte[] val, int len){
		if(val.length != len) throw new IllegalArgumentException(
				varName+" must be length "+len+" but was length "+val.length);
		return val;
	}
	
	public static String ascii(byte[] bytes){
		return new String(bytes,StandardCharsets.US_ASCII);
	}
	
	public static String repeatChar(char val, int ct){
		char[] cs = new char[ct];
		for(int i = 0; i < cs.length; i++) cs[i] = val;
		return new String(cs);
	}
}
