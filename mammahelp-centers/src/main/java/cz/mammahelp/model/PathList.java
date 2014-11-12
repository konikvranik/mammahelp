package cz.mammahelp.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "articles")
public class PathList extends ArrayList<String> {

	private static final long serialVersionUID = -5939718621029899053L;

	@ElementList(data = false, inline = true, entry = "file", type = String.class, required = false)
	private ArrayList<String> thisList;

	public PathList(Collection<? extends String> arg0) {
		thisList = new ArrayList<>(arg0);
	}

	public PathList(int arg0) {
		thisList = new ArrayList<>(arg0);
	}

	public PathList() {
		thisList = new ArrayList<>();
	}

	public void add(int arg0, String arg1) {
		thisList.add(arg0, arg1);
	}

	public boolean add(String arg0) {
		return thisList.add(arg0);
	}

	public boolean addAll(Collection<? extends String> arg0) {
		return thisList.addAll(arg0);
	}

	public boolean addAll(int arg0, Collection<? extends String> arg1) {
		return thisList.addAll(arg0, arg1);
	}

	public void clear() {
		thisList.clear();
	}

	public Object clone() {
		return thisList.clone();
	}

	public boolean contains(Object arg0) {
		return thisList.contains(arg0);
	}

	public boolean containsAll(Collection<?> arg0) {
		return thisList.containsAll(arg0);
	}

	public void ensureCapacity(int arg0) {
		thisList.ensureCapacity(arg0);
	}

	public boolean equals(Object arg0) {
		return thisList.equals(arg0);
	}

	public String get(int arg0) {
		return thisList.get(arg0);
	}

	public int hashCode() {
		return thisList.hashCode();
	}

	public int indexOf(Object arg0) {
		return thisList.indexOf(arg0);
	}

	public boolean isEmpty() {
		return thisList.isEmpty();
	}

	public Iterator<String> iterator() {
		return thisList.iterator();
	}

	public int lastIndexOf(Object arg0) {
		return thisList.lastIndexOf(arg0);
	}

	public ListIterator<String> listIterator() {
		return thisList.listIterator();
	}

	public ListIterator<String> listIterator(int arg0) {
		return thisList.listIterator(arg0);
	}

	public String remove(int arg0) {
		return thisList.remove(arg0);
	}

	public boolean remove(Object arg0) {
		return thisList.remove(arg0);
	}

	public boolean removeAll(Collection<?> arg0) {
		return thisList.removeAll(arg0);
	}

	public boolean retainAll(Collection<?> arg0) {
		return thisList.retainAll(arg0);
	}

	public String set(int arg0, String arg1) {
		return thisList.set(arg0, arg1);
	}

	public int size() {
		return thisList.size();
	}

	public List<String> subList(int arg0, int arg1) {
		return thisList.subList(arg0, arg1);
	}

	public Object[] toArray() {
		return thisList.toArray();
	}

	public <T> T[] toArray(T[] arg0) {
		return thisList.toArray(arg0);
	}

	public String toString() {
		return thisList.toString();
	}

	public void trimToSize() {
		thisList.trimToSize();
	}

}
