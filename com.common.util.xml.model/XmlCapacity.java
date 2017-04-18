package com.common.util.xml.model;

import java.io.Serializable;

import org.dom4j.Element;

public interface XmlCapacity extends Serializable {
	/**
	 * create a new instance
	 * 
	 * @return
	 */
	public XmlCapacity newInstance();

	/**
	 * serializable
	 * 
	 * @param parentEle
	 * @param localName
	 */
	public void serialize(Element parentEle, String localName);

	public void serialize(Element parentEle, String localName,
			boolean isShowEmptyFeild);

	public void unserialize(Element localEle);

}
