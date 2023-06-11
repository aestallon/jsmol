/*
 * Copyright 2023 Szabolcs Bazil Papp
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hu.aestallon.jsmol.json;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class JsonArray
    extends WrappedValue<List<JsonValue>>
    implements List<JsonValue>, JsonComplex<JsonValue> {

  public JsonArray(List<JsonValue> l) {super(l);}

  @Override
  public String toString() {
    return value().stream()
        .map(Objects::toString)
        .collect(Collectors.joining(",", "[", "]"));
  }

  @Override
  public Stream<JsonValue> iter() {
    return super.value().stream();
  }

  @Override
  public int size() {
    return super.value().size();
  }

  @Override
  public boolean isEmpty() {
    return super.value().isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return super.value().contains(o);
  }

  @Override
  public Iterator<JsonValue> iterator() {
    return super.value().iterator();
  }

  @Override
  public Object[] toArray() {
    return super.value().toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return super.value().toArray(a);
  }

  @Override
  public boolean add(JsonValue jsonValue) {
    return super.value().add(jsonValue);
  }

  @Override
  public boolean remove(Object o) {
    return super.value().remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return new HashSet<>(super.value()).containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends JsonValue> c) {
    return super.value().addAll(c);
  }

  @Override
  public boolean addAll(int index, Collection<? extends JsonValue> c) {
    return super.value().addAll(index, c);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return super.value().removeAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return super.value().retainAll(c);
  }

  @Override
  public void clear() {
    super.value().clear();
  }

  @Override
  public JsonValue get(int index) {
    return super.value().get(index);
  }

  @Override
  public JsonValue set(int index, JsonValue element) {
    return super.value().set(index, element);
  }

  @Override
  public void add(int index, JsonValue element) {
    super.value().add(index, element);
  }

  @Override
  public JsonValue remove(int index) {
    return super.value().remove(index);
  }

  @Override
  public int indexOf(Object o) {
    return super.value().indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return super.value().lastIndexOf(o);
  }

  @Override
  public ListIterator<JsonValue> listIterator() {
    return super.value().listIterator();
  }

  @Override
  public ListIterator<JsonValue> listIterator(int index) {
    return super.value().listIterator(index);
  }

  @Override
  public List<JsonValue> subList(int fromIndex, int toIndex) {
    return super.value().subList(fromIndex, toIndex);
  }
}
