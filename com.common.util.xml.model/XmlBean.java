package com.common.util.xml.model;

import java.io.Serializable;

import org.dom4j.Element;

import com.common.util.xml.XmlBeanUtil;


public abstract class XmlBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2221536860096466561L;

	public Element serialize() {
		return XmlBeanUtil.serialize(this);
	}

	public Element serialize(boolean isShowEmptyFeild) {
		return XmlBeanUtil.serialize(this, isShowEmptyFeild);
	}

	public Element serialize(String localName) {
		return XmlBeanUtil.serialize(this, localName);
	}

	public Element serialize(String localName, boolean isShowEmptyFeild) {
		return XmlBeanUtil.serialize(this, localName, isShowEmptyFeild);
	}

	public void unserialize(Element localEle) {
		XmlBeanUtil.unserialize(this, localEle);
	}

	public String toXml() {
		return this.toXml(null);
	}

	public String toXml(String encoding) {
		return XmlBeanUtil.ele2Xml(this.serialize(), encoding);
	}

	public String toXml(boolean isShowEmptyFeild) {
		return XmlBeanUtil.ele2Xml(this.serialize(isShowEmptyFeild),
				XmlBeanUtil.XML_ENCODING);
	}

	public String toXml(String encoding,boolean isShowEmptyFeild) {
		return XmlBeanUtil.ele2Xml(this.serialize(isShowEmptyFeild), encoding);
	}
	
	public void toBean(String xml) {
		Element rootCopyEle = XmlBeanUtil.xml2Ele(xml);
		XmlBeanUtil.unserialize(this, rootCopyEle);
	}

	public String toString() {
		return this.serialize().asXML();
	}

	public String toString(boolean isShowEmptyFeild) {
		return this.serialize(isShowEmptyFeild).asXML();
	}
}
