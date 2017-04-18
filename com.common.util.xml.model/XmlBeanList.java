package com.common.util.xml.model;

import java.util.LinkedList;

import org.dom4j.Element;


public abstract class XmlBeanList<T extends XmlBean> extends LinkedList<T>
		implements XmlCapacity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4186394400015681436L;

	public XmlBeanList() {
		super();
	}

	public abstract XmlCapacity newInstance();

	public abstract XmlBean newEleInstance();

	@Override
	public void serialize(Element parentEle, String localName) {
		for (XmlBean bean : this) {
			if (bean == null) {
				continue;
			}
			parentEle.add(bean.serialize(localName));
		}
	}
	
	@Override
	public void serialize(Element parentEle, String localName,
			boolean isShowEmptyFeild){
		for (XmlBean bean : this) {
			if (bean == null) {
				continue;
			}
			parentEle.add(bean.serialize(localName,isShowEmptyFeild));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void unserialize(Element localEle) {
		if (localEle == null
				|| !(localEle.elementIterator().hasNext() || localEle
						.attributeIterator().hasNext())) {// do not have child Node
			return ;
		}
		XmlBean bean = newEleInstance();
		bean.unserialize(localEle);
		this.add((T)bean);
	}

}
