package cz.mammahelp.tools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.NodeMap;
import org.simpleframework.xml.stream.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.CharacterData;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.sun.xml.internal.ws.util.StringUtils;

public class NodeWrapper implements InputNode {

	Logger log = LoggerFactory.getLogger(NodeWrapper.class);

	public class NodeMapWrapper implements NodeMap<InputNode> {

		private NamedNodeMap attrs;
		private Node node;

		public NodeMapWrapper(Node node) {
			if (node == null)
				throw new NullPointerException();
			this.node = node;
			this.attrs = node.getAttributes();
		}

		@Override
		public InputNode getNode() {
			return new NodeWrapper(node);
		}

		@Override
		public String getName() {
			return node.getNodeName();
		}

		@Override
		public InputNode get(String name) {
			return new NodeWrapper(attrs.getNamedItem(name));
		}

		@Override
		public InputNode remove(String name) {
			if (attrs == null || name == null)
				return null;
			Node attr = null;
			try {
				attr = attrs.removeNamedItem(name);
			} catch (DOMException e) {
				if (e.code != 8)
					log.error("Attribute " + name + ": " + e.getMessage());
				// log.debug("", e);
			}
			if (attr == null)
				return null;
			return new NodeWrapper(attr);
		}

		@Override
		public Iterator<String> iterator() {
			List<String> names = new ArrayList<String>();
			if (attrs != null)
				for (int i = 0; i < attrs.getLength(); i++) {
					names.add(attrs.item(i).getNodeName());
				}
			return names.iterator();
		}

		@Override
		public InputNode put(String name, String value) {
			Node a = attrs.item(0);
			Document d = a.getOwnerDocument();
			Attr at = d.createAttribute(name);
			at.setValue(value);
			return new NodeWrapper(attrs.setNamedItem(at));
		}

	}

	private Node dom;
	private Node childpointer;

	public NodeWrapper(Node dom) {
		if (dom == null)
			throw new NullPointerException();

		Node root = dom;
		if (dom instanceof Document) {

			NodeList list = dom.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				if (list.item(i) instanceof Element) {
					root = (Element) list.item(i);
					break;
				}
			}
			root = ((Document) dom).getDocumentElement();
		}
		this.dom = root;
	}

	@Override
	public String getName() {
		return dom.getNodeName();
	}

	@Override
	public String getValue() throws Exception {
		return nodeToString(dom);
	}

	private String nodeToString(Node node) {
		if (node instanceof Element) {
			StringBuffer sb = new StringBuffer();
			boolean found = false;
			for (Node ch = node.getFirstChild(); ch != null; ch = ch
					.getNextSibling()) {
				if (ch instanceof CharacterData) {
					sb.append(((CharacterData) ch).getData());
				} else if (ch instanceof Text) {
					sb.append(((Text) ch).getTextContent());
					found = true;
				} else if (ch instanceof Element) {
					sb.append("<");
					sb.append(ch.getNodeName());

					NamedNodeMap attrs = ch.getAttributes();
					for (int i = 0; i < attrs.getLength(); i++) {
						Node a = attrs.item(i);

						sb.append(" ");
						sb.append(a.getNodeName());
						sb.append("=\"");
						sb.append(a.getNodeValue());
						sb.append("\"");
					}

					sb.append(">");

					sb.append(nodeToString(ch));

					sb.append("</");
					sb.append(ch.getNodeName());
					sb.append(">");

				}
			}
			return sb.toString();
		} else
			return node.getNodeValue();
	}

	@Override
	public boolean isRoot() {
		return dom.getParentNode() == null || dom.getParentNode() == dom;
	}

	@Override
	public boolean isElement() {
		return dom instanceof Element;
	}

	@Override
	public String getPrefix() {
		return dom.getPrefix();
	}

	@Override
	public String getReference() {
		return dom.getNamespaceURI();
	}

	@Override
	public Position getPosition() {
		return new Position() {

			@Override
			public int getLine() {
				Node p = dom;
				int c = 0;
				while (true) {
					Node n = p.getPreviousSibling();
					if (n == null)
						n = p.getParentNode();
					if (n == null || p == n)
						break;
					p = n;
					c++;

				}
				return c;
			}
		};
	}

	@Override
	public InputNode getAttribute(String name) {
		NamedNodeMap attrs = dom.getAttributes();
		Node attr = attrs.getNamedItem(name);
		return new NodeWrapper(attr);
	}

	@Override
	public NodeMap<InputNode> getAttributes() {
		return new NodeMapWrapper(dom);
	}

	@Override
	public InputNode getParent() {
		return new NodeWrapper(dom.getParentNode());
	}

	@Override
	public Object getSource() {
		return dom;
	}

	@Override
	public InputNode getNext() throws Exception {
		if (childpointer == null)
			childpointer = dom.getFirstChild();
		else
			childpointer = childpointer.getNextSibling();
		if (childpointer == null)
			return null;
		return new NodeWrapper(childpointer);
	}

	@Override
	public InputNode getNext(String name) throws Exception {
		InputNode ch = getNext();
		if (name == null || ch == null || name.equals(ch.getName()))
			return ch;
		return null;
	}

	@Override
	public void skip() throws Exception {
		childpointer = null;

	}

	@Override
	public boolean isEmpty() throws Exception {
		return dom.hasChildNodes();
	}

}
