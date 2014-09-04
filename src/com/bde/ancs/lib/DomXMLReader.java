package com.bde.ancs.lib;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DomXMLReader {
	public static Map<String, Attribute> readXML(InputStream inStream) {
		// List<Attribute> attributes = new ArrayList<Attribute>();
		HashMap<String, Attribute> attributes = new HashMap<String, Attribute>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document dom = builder.parse(inStream);
			Element root = dom.getDocumentElement();
			NodeList items = root.getElementsByTagName("attribute");// ��������person�ڵ�
			for (int i = 0; i < items.getLength(); i++) {
				Attribute attribute = new Attribute();
				// �õ���һ��person�ڵ�
				Element attributeNode = (Element) items.item(i);
				// ��ȡperson�ڵ��id����ֵ
				attribute.setName(attributeNode.getAttribute("name"));
				// ��ȡperson�ڵ��µ������ӽڵ�(��ǩ֮��Ŀհ׽ڵ��name/ageԪ��)
				NodeList childsNodes = attributeNode.getChildNodes();
				for (int j = 0; j < childsNodes.getLength(); j++) {
					Node node = (Node) childsNodes.item(j); // �ж��Ƿ�ΪԪ������
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element childNode = (Element) node;
						// �ж��Ƿ�nameԪ��
						if ("appIdentifier".equals(childNode.getNodeName())) {
							// ��ȡnameԪ����Text�ڵ�,Ȼ���Text�ڵ��ȡ����
							attribute.setAppIdentifier(childNode
									.getAttribute("name"));
						} else if ("categoryID".equals(childNode.getNodeName())) {
							attribute.setCategoryID(Integer.valueOf(childNode
									.getAttribute("name")));
						}
					}
				}
				attributes.put(attribute.getName(), attribute);
			}
			//inStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return attributes;
	}
}