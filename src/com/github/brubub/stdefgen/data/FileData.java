package com.github.brubub.stdefgen.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public abstract class FileData implements Iterable<String>{

	private ArrayList<String> file = null;
	private File from = null;
	private File to = null;
	
	public FileData() {
		this.file = new ArrayList<String>();
	}
	
	public FileData(ArrayList<String> file) {
		this.file = file;
	}
	
	public FileData(ArrayList<String> file, File from) {
		this.file = file;
		this.from = from;
	}
	
	public FileData(ArrayList<String> file, File from, File to) {
		this.file = file;
		this.from = from;
		this.to = to;
	}
	
	public File getFrom() {
		return from;
	}

	public File getTo() {
		return to;
	}

	public void setTo(File to) {
		this.to = to;
	}
	
	public void add(int arg0, String arg1) {
		file.add(arg0, arg1);
	}

	public boolean add(String e) {
		return file.add(e);
	}

	public boolean addAll(Collection<? extends String> c) {
		return file.addAll(c);
	}

	public boolean addAll(int arg0, Collection<? extends String> arg1) {
		return file.addAll(arg0, arg1);
	}

	public void clear() {
		file.clear();
	}

	public Object clone() {
		return file.clone();
	}

	public boolean contains(Object o) {
		return file.contains(o);
	}

	public boolean containsAll(Collection<?> arg0) {
		return file.containsAll(arg0);
	}

	public void ensureCapacity(int minCapacity) {
		file.ensureCapacity(minCapacity);
	}

	public boolean equals(Object arg0) {
		return file.equals(arg0);
	}

	public void forEach(Consumer<? super String> action) {
		file.forEach(action);
	}

	public String get(int index) {
		return file.get(index);
	}

	public int hashCode() {
		return file.hashCode();
	}

	public int indexOf(Object o) {
		return file.indexOf(o);
	}

	public boolean isEmpty() {
		return file.isEmpty();
	}

	public Iterator<String> iterator() {
		return file.iterator();
	}

	public int lastIndexOf(Object o) {
		return file.lastIndexOf(o);
	}

	public ListIterator<String> listIterator() {
		return file.listIterator();
	}

	public ListIterator<String> listIterator(int index) {
		return file.listIterator(index);
	}

	public String remove(int index) {
		return file.remove(index);
	}

	public boolean remove(Object o) {
		return file.remove(o);
	}

	public boolean removeAll(Collection<?> c) {
		return file.removeAll(c);
	}

	public boolean removeIf(Predicate<? super String> filter) {
		return file.removeIf(filter);
	}

	public void replaceAll(UnaryOperator<String> arg0) {
		file.replaceAll(arg0);
	}

	public boolean retainAll(Collection<?> c) {
		return file.retainAll(c);
	}

	public String set(int arg0, String arg1) {
		return file.set(arg0, arg1);
	}

	public int size() {
		return file.size();
	}

	public void sort(Comparator<? super String> arg0) {
		file.sort(arg0);
	}

	public Spliterator<String> spliterator() {
		return file.spliterator();
	}

	public List<String> subList(int arg0, int arg1) {
		return file.subList(arg0, arg1);
	}

	public Object[] toArray() {
		return file.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return file.toArray(a);
	}

	public String toString() {
		return file.toString();
	}

	public void trimToSize() {
		file.trimToSize();
	}
	
	
}
