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
			NodeList items = root.getElementsByTagName("attribute");// 查找所有person节点
			for (int i = 0; i < items.getLength(); i++) {
				Attribute attribute = new Attribute();
				// 得到第一个person节点
				Element attributeNode = (Element) items.item(i);
				// 获取person节点的id属性值
				attribute.setName(attributeNode.getAttribute("name"));
				// 获取person节点下的所有子节点(标签之间的空白节点和name/age元素)
				NodeList childsNodes = attributeNode.getChildNodes();
				for (int j = 0; j < childsNodes.getLength(); j++) {
					Node node = (Node) childsNodes.item(j); // 判断是否为元素类型
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element childNode = (Element) node;
						// 判断是否name元素
						if ("appIdentifier".equals(childNode.getNodeName())) {
							// 获取name元素下Text节点,然后从Text节点获取数据
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