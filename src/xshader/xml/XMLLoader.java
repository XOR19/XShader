package xshader.xml;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import xshader.parser.ParserException;


public class XMLLoader {
	
	public static XMLNode load(String file) throws ParserException{
		try{
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(new ByteArrayInputStream(file.getBytes("UTF-8")));
			doc.getDocumentElement().normalize();
			return load(doc.getChildNodes().item(0));			
		}catch(Exception e){
			if(!(e instanceof ParserException)){
				e = new ParserException(e);
			}
			throw (ParserException)e;
		}
	}

	private static XMLNode load(Node item) {
		XMLNode node = new XMLNode(item.getNodeName());
		node.setText(item.getTextContent());
		NamedNodeMap nnm = item.getAttributes();
		if(nnm!=null){
			int l = nnm.getLength();
			for(int i=0; i<l; i++){
				Node n = nnm.item(i);
				node.setProperty(n.getNodeName(), n.getNodeValue());
			}
		}
		NodeList nl = item.getChildNodes();
		for(int i=0; i<nl.getLength(); i++){
			if(!nl.item(i).getNodeName().equals("#text")){
				node.addChild(load(nl.item(i)));
			}
		}
		return node;
	}
	
}
