package com.common.util.xml.model;

import java.util.Collection;

import com.common.util.CollectionUtils;
import com.common.util.reflect.ReflectUtil;


public class XmlList<T extends XmlBean> extends XmlBeanList<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3476134700058543816L;
	private Class<T> xmlBeanCls;

	public XmlList() {
		
	}

	public XmlList(Class<T> xmlBeanCls) {
		super();
		this.xmlBeanCls = xmlBeanCls;
	}

	@Override
	public XmlCapacity newInstance() {
		return new XmlList<T>(xmlBeanCls);
	}

	@Override
	public XmlBean newEleInstance() {
		
		try {
			return (XmlBean) ReflectUtil.newInstance(xmlBeanCls);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean add(T e) {
		if (e == null) {
			return false;
		}
		if (xmlBeanCls == null) {
			xmlBeanCls = (Class<T>) e.getClass();
		}
		return super.add(e);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void add(int location, T e) {
		if (e == null) {
			return ;
		}
		if (xmlBeanCls == null) {
			xmlBeanCls = (Class<T>) e.getClass();
		}
		super.add(location, e);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		if (xmlBeanCls == null) {
			T e = CollectionUtils.getFirst(c);
			if (e == null) {
				return false;
			}
			xmlBeanCls = (Class<T>) e.getClass();
		}
		return super.addAll(index, c);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean addAll(Collection<? extends T> c) {

		if (xmlBeanCls == null) {
			T e = CollectionUtils.getFirst(c);
			if (e == null) {
				return false;
			}
			xmlBeanCls = (Class<T>) e.getClass();
		}
		return super.addAll(c);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addFirst(T e) {
		if (e == null) {
			return;
		}
		if (xmlBeanCls == null) {
			xmlBeanCls = (Class<T>) e.getClass();
		}
		super.addFirst(e);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addLast(T e) {
		if (e == null) {
			return;
		}
		if (xmlBeanCls == null) {
			xmlBeanCls = (Class<T>) e.getClass();
		}
		super.addLast(e);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean offerFirst(T e) {
		if (e == null) {
			return false;
		}
		if (xmlBeanCls == null) {
			xmlBeanCls = (Class<T>) e.getClass();
		}
		return super.offerFirst(e);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean offerLast(T e) {
		if (e == null) {
			return false;
		}
		if (xmlBeanCls == null) {
			xmlBeanCls = (Class<T>) e.getClass();
		}
		return super.offerLast(e);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void push(T e) {
		if (e == null) {
			return;
		}
		if (xmlBeanCls == null) {
			xmlBeanCls = (Class<T>) e.getClass();
		}
		super.push(e);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T set(int location, T e) {
		if (e == null) {
			return null;
		}
		if (xmlBeanCls == null) {
			xmlBeanCls = (Class<T>) e.getClass();
		}
		return super.set(location, e);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean offer(T e) {
		if (e == null) {
			return false;
		}
		if (xmlBeanCls == null) {
			xmlBeanCls = (Class<T>) e.getClass();
		}
		return super.offer(e);
	}

	public String getXmlBeanClsName() {
		if (xmlBeanCls == null) {
			return null;
		}
		return xmlBeanCls.getName();
	}

	public Class<T> getXmlBeanCls() {
		return xmlBeanCls;
	}

	public void setXmlBeanCls(Class<T> xmlBeanCls) {
		this.xmlBeanCls = xmlBeanCls;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		for(XmlBean bean : this){
			sb.append(bean.toString());
		}
		return sb.toString();
	}

}
