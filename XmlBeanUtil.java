package com.common.util.xml;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.common.util.StringUtils;
import com.common.util.reflect.BeanUtils;
import com.common.util.xml.model.XMLChar;
import com.common.util.xml.model.XmlBean;
import com.common.util.xml.model.XmlCapacity;
import com.common.util.xml.type.ITypeConverter;
import com.common.util.xml.type.TypeFactory;


public final class XmlBeanUtil {

	public static final String XML_ENCODING = "UTF-8";
	public static final String XML_DEFAULT = "##default";
	public static final String XML_CDATA_PREFIX = "<![CDATA[";
	public static final String XML_CDATA_SUFFIX = "]]>";

	public static final String XML_DEFAULT_NAMESPACE_FEILD = "xmlns";

	private static final ThreadLocal<Boolean> isOutputZeroThread = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return true;
		}
	};

	public static String setCDATA(String data) {
		if (StringUtils.isEmpty(data)) {
			return data;
		}
		return XML_CDATA_PREFIX + data + XML_CDATA_SUFFIX;
	}

	public static String ignoreCDATA(String data) {
		if (StringUtils.isEmpty(data)) {
			return data;
		}
		return data.substring(XML_CDATA_PREFIX.length(),
				data.lastIndexOf(XML_CDATA_SUFFIX));
	}

	//
	public static String bean2Xml(Object bean) {
		if (bean == null) {
			return null;
		}
		if (XmlBean.class.isAssignableFrom(bean.getClass())) {// if the bean has
																// extended
																// xmlbean
			return ele2Xml(((XmlBean) bean).serialize(), null);
		} else {//
			return JaxbUtils.bean2Xml(bean);
		}

	}

	@SuppressWarnings("unchecked")
	public static <T> T xml2Bean(T targetBean, String xml) {
		if (targetBean == null || StringUtils.isEmpty(xml)) {
			return null;
		}
		if (XmlBean.class.isAssignableFrom(targetBean.getClass())) {
			XmlBean xmlBean = (XmlBean) targetBean;
			XmlBeanUtil.unserialize((XmlBean) targetBean,
					XmlBeanUtil.xml2Ele(xml));
			return (T) xmlBean;
		}
		return null;
	}

	public static void setOutputZero(boolean isOutputZzero) {
		isOutputZeroThread.set(isOutputZzero);
	}

	public static boolean isOutputZero() {
		return isOutputZeroThread.get();
	}

	public static String ele2Xml(Element ele) {
		return XmlBeanUtil.ele2Xml(ele, XML_ENCODING);
	}

	public static String ele2Xml(Element ele, String encoding) {
		Document doc = ele.getDocument();
		if (doc == null) {
			doc = DocumentHelper.createDocument(ele);
		}
		if (StringUtils.isEmpty(encoding)) {
			encoding = XML_ENCODING;
		}
		doc.setXMLEncoding(encoding);
		OutputFormat format = new OutputFormat();
		format.setTrimText(true);
		format.setNewlines(true);
		format.setNewLineAfterDeclaration(false);
		format.setIndent(true);
		format.setIndent("  ");
		format.setEncoding(encoding);

		StringWriter sw = new StringWriter();
		XMLWriter xw = new XMLWriter(sw, format);
		try {
			xw.write(doc);
			return sw.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			try {
				if (xw != null) {
					xw.flush();
					xw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			IOUtils.closeQuietly(sw);
		}

		return null;
	}

	public static Document xml2Doc(String xml) {
		try {
			return DocumentHelper.parseText(xml);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Element xml2Ele(String xml) {
		Document doc = xml2Doc(filterCharts(xml));
		return doc.getRootElement();
	}

	public static Element serialize(XmlBean bean) {
		if (bean == null) {
			return null;
		}
		return serialize(bean, getElementName(bean));
	}

	public static Element serialize(XmlBean bean, String elementName) {
		if (bean == null) {
			return null;
		}
		return serialize(bean, elementName, false);
	}

	public static Element serialize(XmlBean bean, boolean isShowEmptyFeild) {
		if (bean == null) {
			return null;
		}
		return serialize(bean, getElementName(bean), isShowEmptyFeild);
	}

	public static Element serialize(XmlBean bean, String elementName,
			boolean isShowEmptyFeild) {
		// Element localEle = DocumentHelper.createElement(elementName);

		Element localEle = createElement(bean, elementName);

		XmlType type = bean.getClass().getAnnotation(XmlType.class);
		if (type != null) {
			String[] propOrders = type.propOrder();
			if (propOrders != null && propOrders.length > 0
					&& StringUtils.isNotEmpty(propOrders[0])) {
				for (String po : propOrders) {
					PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(
							bean.getClass(), po);
					if (pd != null) {
						serialize(bean, pd, localEle, isShowEmptyFeild);
					}
				}
				return localEle;
			}
		}
		PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(bean
				.getClass());
		if (pds != null) {
			for (PropertyDescriptor pd : pds) {
				serialize(bean, pd, localEle, isShowEmptyFeild);
			}
		}
		setDefaultNameSpaceNull();
		return localEle;
	}

	private static void serialize(XmlBean bean, PropertyDescriptor pd,
			Element eleName, boolean isShowEmptyFeild) {
		if (getAnnotation(bean, pd, XmlTransient.class) != null) {
			// can not be serialize current Transient property.
			return;
		}
		Class<?> cls = pd.getPropertyType();
		if (cls == null) {
			return;
		}
		// next node
		if (XmlBean.class.isAssignableFrom(cls)) {
			XmlBean child = (XmlBean) BeanUtils.getPropertyValue(bean, pd);
			if (child != null) {
				Element childEle = serialize(child, getElementName(bean, pd),
						isShowEmptyFeild);
				if (childEle != null) {
					eleName.add(childEle);
				}
			}
			return;
		}
		// xml capacity
		if (XmlCapacity.class.isAssignableFrom(cls)) {
			XmlCapacity capty = (XmlCapacity) BeanUtils.getPropertyValue(bean,
					pd);
			if (capty != null) {
				capty.serialize(eleName, getElementName(bean, pd),
						isShowEmptyFeild);
			}
		}

		// XML element
		if (Element.class.isAssignableFrom(cls)) {
			Element childEle = (Element) BeanUtils.getPropertyValue(bean, pd);
			if (childEle != null) {
				childEle.setParent(null);
				eleName.add(childEle);
			}
		}
		// base Type
		ITypeConverter type = TypeFactory.getType(cls);
		if (type == null) {
			return;
		}
		Object value = BeanUtils.getPropertyValue(bean, pd);

		if (isShowEmptyFeild == true && value == null) {
			if (String.class == cls) {
				// return;
				value = "";
			} else if (value == null) {
				return;
			}
		} else if (isShowEmptyFeild == false && value == null) {
			return;
		}

		if (Number.class.isAssignableFrom(value.getClass())) {
			Number num = (Number) value;
			if (0 == num.longValue() && !isOutputZero()) {
				return;
			}
		}
		String text = type.getText(value);
		// attr
		XmlAttribute xmlAttr = getAnnotation(bean, pd, XmlAttribute.class);
		if (xmlAttr != null) {
			if (!StringUtils.isEmpty(text)) {
				String attrName = xmlAttr.name();
				if (StringUtils.isEmpty(attrName)
						|| XML_DEFAULT.equals(attrName)) {
					attrName = getElementName(bean, pd);
				}
				eleName.addAttribute(attrName, text);
			}
			return;
		}

		// value
		XmlValue xmlVal = getAnnotation(bean, pd, XmlValue.class);
		if (xmlVal != null) {
			if (StringUtils.isNotEmpty(text)
					&& text.indexOf(XML_CDATA_PREFIX) == 0) {
				eleName.addCDATA(ignoreCDATA(text));
			} else {
				eleName.addText(text);
			}
			return;
		}
		// element
		Element ele = eleName.addElement(getElementName(bean, pd));
		if (StringUtils.isNotEmpty(text) && text.indexOf(XML_CDATA_PREFIX) == 0) {
			ele.addCDATA(ignoreCDATA(text));
		} else {
			ele.addText(text);
		}
	}

	public static void unserialize(XmlBean bean, Element eleName) {
		if (bean == null) {
			return;
		}
		XmlType xmlType = bean.getClass().getAnnotation(XmlType.class);
		if (xmlType != null) {
			String[] pos = xmlType.propOrder();
			if (pos != null && pos.length > 0 && StringUtils.isNotEmpty(pos[0])) {
				for (String p : pos) {
					PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(
							bean.getClass(), p);
					if (pd != null) {
						unserialize(bean, pd, eleName);
					}
				}
				return;
			}
		}
		PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(bean
				.getClass());
		if (pds != null) {
			for (PropertyDescriptor pd : pds) {
				unserialize(bean, pd, eleName);
			}
		}

	}

	private static void unserialize(XmlBean bean, PropertyDescriptor pd,
			Element ele) {
		if (getAnnotation(bean, pd, XmlTransient.class) != null) {
			// can not be serialize current Transient property.
			return;
		}
		Class<?> cls = pd.getPropertyType();
		if (cls == null) {
			return;
		}
		// next node
		if (XmlBean.class.isAssignableFrom(cls)) {
			Element childEle = ele.element(getElementName(bean, pd));
			if (childEle != null) {
				XmlBean child = (XmlBean) newInstance(cls);

				if (child != null) {
					unserialize(child, childEle);
					BeanUtils.setPropertyValue(bean, pd, child);
				}
			}
			return;
		}
		// xml capacity
		if (XmlCapacity.class.isAssignableFrom(cls)) {
			List<?> childEles = ele.elements(getElementName(bean, pd));
			if (childEles == null || childEles.size() <= 0) {
				return;
			}
			XmlCapacity capty = (XmlCapacity) BeanUtils.getPropertyValue(bean,
					pd);
			if (capty == null) {
				capty = (XmlCapacity) newInstance(cls);
				BeanUtils.setPropertyValue(bean, pd, capty);
			}
			if (capty != null) {
				for (int i = 0; i < childEles.size(); i++) {
					Element e = (Element) childEles.get(i);
					capty.unserialize(e);
				}
			}
		}

		// XML element
		if (Element.class.isAssignableFrom(cls)) {

			Element childEle = ele.element(getElementName(bean, pd));
			if (childEle != null) {
				childEle.setParent(null);
				BeanUtils.setPropertyValue(bean, pd, childEle);
			}
		}
		// base Type
		ITypeConverter type = TypeFactory.getType(cls);
		if (type == null) {
			return;
		}

		// attr
		XmlAttribute xmlAttr = getAnnotation(bean, pd, XmlAttribute.class);
		if (xmlAttr != null) {

			String attrName = xmlAttr.name();
			if (StringUtils.isEmpty(attrName) || XML_DEFAULT.equals(attrName)) {
				attrName = getElementName(bean, pd);
			}
			String text = ele.attributeValue(attrName);
			if (StringUtils.isNotEmpty(text)) {
				Object value = type.getValue(text);
				if (value != null) {
					BeanUtils.setPropertyValue(bean, pd, value);
				}
			}
			return;
		}

		// value
		XmlValue xmlVal = getAnnotation(bean, pd, XmlValue.class);
		if (xmlVal != null) {
			String text = ele.getText();
			if (StringUtils.isNotEmpty(text)) {
				Object value = type.getValue(text);
				if (value != null) {
					BeanUtils.setPropertyValue(bean, pd, text);
				}
			}
			return;
		}
		// element
		String eleName = getElementName(bean, pd);
		Element childEle = ele.element(eleName);
		String text = null;
		if (childEle != null) {
			text = childEle.getText();
			if (StringUtils.isEmpty(text) && childEle.hasContent()) {
				text = childEle.getStringValue();
			}
		}
		if (StringUtils.isNotEmpty(text)) {
			Object value = type.getValue(text);
			if (value != null) {
				BeanUtils.setPropertyValue(bean, pd, value);
			}
		}

	}

	private static Object newInstance(Class<?> cls) {
		try {
			return cls.newInstance();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String getElementName(XmlBean bean, PropertyDescriptor pd) {
		String eleName = null;
		XmlElement xmlEle = getAnnotation(bean, pd, XmlElement.class);
		if (xmlEle != null) {
			eleName = xmlEle.name();
		}
		if (StringUtils.isEmpty(eleName) || XML_DEFAULT.equals(eleName)) {
			eleName = StringUtils.firstChar2Upper(pd.getName());
		}
		return eleName;
	}

	/**
	 * get annotationClass from field or getter method
	 * 
	 * @param bean
	 *            java bean
	 * @param pd
	 *            PropertyDescriptor getter and setter and so on;
	 * @param annotationClass
	 * @return
	 */
	private static <T extends Annotation> T getAnnotation(XmlBean bean,
			PropertyDescriptor pd, Class<T> annotationClass) {
		if (bean == null || pd == null) {
			return null;
		}
		Method method = pd.getReadMethod();
		Field filed = getField(bean.getClass(), pd.getName());
		if (filed != null && filed.getAnnotation(annotationClass) != null) {
			return filed.getAnnotation(annotationClass);
		} else if (method != null) {
			// get annotation on getter methods.
			return method.getAnnotation(annotationClass);
		}

		return null;
	}

	/**
	 * get Class feild by feildName;
	 * 
	 * @param cls
	 * @param name
	 * @return
	 */
	private static Field getField(Class<?> cls, String fieldName) {
		if (cls == null || StringUtils.isEmpty(fieldName)) {
			return null;
		}
		Field[] fields = cls.getDeclaredFields();
		if (fields != null && fields.length > 0) {
			for (Field f : fields) {
				if (fieldName.equalsIgnoreCase(f.getName())) {
					return f;
				}
			}
		}
		return null;
	}

	/**
	 * Get the element name from the bean class Name <br/>
	 * or the annotation attribute name of XmlType
	 * 
	 * @param bean
	 * @return
	 */
	public static String getElementName(XmlBean bean) {
		String name = null;
		XmlType type = bean.getClass().getAnnotation(XmlType.class);
		if (type != null) {
			name = type.name();
		}
		if (StringUtils.isEmpty(name) || XML_DEFAULT.equals(name)) {
			name = bean.getClass().getSimpleName();
		}
		return name;
	}

	private static String NAME_SPACE = "";
	private static int NAME_SPACE_FLAG = 0;

	/**
	 * create Element when add NameSpace like
	 * 
	 * @XmlType(namespace="https://www.baidu.com")
	 * @XmlType(namespace="https://www.baidu.com,xmlns:xsi=https://www.baidu.com,xsi:schemaLocation=https://www.baidu.com")
	 * @XmlType(namespace="xmlns=https://www.baidu.com,xmlns:xsi=https://www.baidu.com,xsi:schemaLocation=https://www.baidu.com")
	 * @param bean
	 * @param localEle
	 */
	public static Element createElement(XmlBean bean, String eleName) {
		Document doc = DocumentHelper.createDocument();
		Element localEle = null;
		List<String> ln = null;
		XmlType type = bean.getClass().getAnnotation(XmlType.class);
		if (type != null) {
			String nameSpace = type.namespace();
			if (StringUtils.isNotEmpty(nameSpace)
					&& !XML_DEFAULT.equals(nameSpace)) {
				ln = new ArrayList<String>();
				String[] naS = nameSpace.split(",");
				for (String s : naS) {
					ln.add(s);
				}
			}
		}
		if (ln != null && ln.size() > 0 && NAME_SPACE_FLAG == 0) {

			if (ln.size() == 1) {
				localEle = doc.addElement(eleName, ln.get(0));
				NAME_SPACE = ln.get(0);
			} else {
				for (String s : ln) {
					String[] ns = s.split("=");
					if (ns.length == 1) {
						localEle = doc.addElement(eleName, ns[0]);
						NAME_SPACE = ns[0];
						// localEle.addNamespace("", ns[0]);
					} else {
						if (XML_DEFAULT_NAMESPACE_FEILD.equals(ns[0])) {
							localEle = doc.addElement(eleName, ns[1]);
							NAME_SPACE = ns[1];
						} else {
							localEle.addAttribute(ns[0], ns[1]);
						}
					}
				}
			}
		} else {
			// The child node nameSpace must be like their parent<br/>
			// if no it will be added empty attribute xmlns=""
			if (!"".equals(NAME_SPACE)) {
				localEle = doc.addElement(eleName, NAME_SPACE);
			} else {
				localEle = DocumentHelper.createElement(eleName);
			}
		}

		// if (ln != null && ln.size() > 0) {
		//
		// if (ln.size() == 1) {
		// localEle = doc.addElement(eleName, ln.get(0));
		// if (NAME_SPACE_FLAG == 0) {
		// NAME_SPACE = ln.get(0);
		// }
		// } else {
		// for (String s : ln) {
		// String[] ns = s.split("=");
		// if (ns.length == 1) {
		// localEle = doc.addElement(eleName, ns[0]);
		// if (NAME_SPACE_FLAG == 0) {
		// NAME_SPACE = ns[0];
		// }
		// // localEle.addNamespace("", ns[0]);
		// } else {
		// if (XML_DEFAULT_NAMESPACE_FEILD.equals(ns[0])) {
		// localEle = doc.addElement(eleName, ns[0]);
		// if (NAME_SPACE_FLAG == 0) {
		// NAME_SPACE = ns[0];
		// }
		// } else {
		// localEle.addAttribute(ns[0], ns[1]);
		// }
		// }
		// }
		// }
		// } else {
		// // The child node nameSpace must be like their parent<br/>
		// // if no it will be added empty attribute xmlns=""
		// if (!"".equals(NAME_SPACE)) {
		// localEle = doc.addElement(eleName, NAME_SPACE);
		// } else {
		// localEle = DocumentHelper.createElement(eleName);
		// }
		// }
		++NAME_SPACE_FLAG;
		return localEle;

	}

	private static void setDefaultNameSpaceNull(){
		NAME_SPACE_FLAG=0;
		NAME_SPACE="";
	}
	private static String filterCharts(String text) {
		if (text == null || "".endsWith(text)) {
			return null;
		}
		int size = text.length();
		if (size <= 0) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < size; ++i) {
			char c = text.charAt(i);
			if (c == '>' || c == '<' || c == '&' || c == '\t' || c == '\n'
					|| c == '\r') {// keep
				sb.append(c);
				continue;
			}
			if (c < 32) {// can not see
				continue;
			}
			int v = (int) c;
			if (XMLChar.isInvalid((int) c)) {// incalid char
				//
				if (XMLChar.isHighSurrogate(v)) {
					if (i + 1 < size) {
						char low = text.charAt(i + 1);
						if (XMLChar.isLowSurrogate(low)) {
							sb.append(c).append(low);
							++i;
							continue;
						}
					}
				}
				continue;
			}
			sb.append(c);
		}
		return sb.toString();
	}

}
