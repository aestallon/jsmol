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
package hu.aestallon.jsmol.marshaller.impl;

import hu.aestallon.jsmol.json.JsonValue;
import hu.aestallon.jsmol.marshaller.ArrayMapper;
import hu.aestallon.jsmol.marshaller.JsonTypeMapper;
import hu.aestallon.jsmol.marshaller.JsonTypeMapperProvider;
import hu.aestallon.jsmol.marshaller.JsonValueMapper;
import hu.aestallon.jsmol.parser.JsonParser;
import hu.aestallon.jsmol.result.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.assertThat;

class JsonMarshallerTest {

  private final JsonTypeMapperProvider jsonTypeMapperProvider = new JsonTypeMapperProviderImpl();
  private final JsonTypeMapper<String> stringMapper           = JsonPrimitiveMapper.STRING_MAPPER;
  private final JsonParser             parser                 = new JsonParser();

  private static final class Course {
    private static final String SUBJECT  = "subject";
    private static final String TEACHER  = "teacher";
    private static final String STUDENTS = "students";

    private String       subject;
    private Person       teacher;
    private List<Person> students;

    public String getSubject() {
      return subject;
    }

    public Course setSubject(String subject) {
      this.subject = subject;
      return this;
    }

    public Person getTeacher() {
      return teacher;
    }

    public Course setTeacher(Person teacher) {
      this.teacher = teacher;
      return this;
    }

    public List<Person> getStudents() {
      return students;
    }

    public Course setStudents(List<Person> students) {
      this.students = students;
      return this;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {return true;}
      if (o == null || getClass() != o.getClass()) {return false;}
      Course course = (Course) o;
      return Objects.equals(subject, course.subject) && Objects.equals(
          teacher, course.teacher) && Objects.equals(students, course.students);
    }

    @Override
    public int hashCode() {
      return Objects.hash(subject, teacher, students);
    }
  }

  private static final class Person {
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME  = "lastName";

    private String firstName;
    private String lastName;

    public String getFirstName() {
      return firstName;
    }

    public Person setFirstName(String firstName) {
      this.firstName = firstName;
      return this;
    }

    public String getLastName() {
      return lastName;
    }

    public Person setLastName(String lastName) {
      this.lastName = lastName;
      return this;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {return true;}
      if (o == null || getClass() != o.getClass()) {return false;}
      Person person = (Person) o;
      return Objects.equals(firstName, person.firstName) && Objects.equals(
          lastName, person.lastName);
    }

    @Override
    public int hashCode() {
      return Objects.hash(firstName, lastName);
    }
  }

  @Test
  void marshallingToJsonWithCustomMarshallerWorks() throws Exception {
    final var personMapper = new ObjectMapper<>(Person::new)
        .bind(Person.FIRST_NAME, stringMapper, Person::getFirstName, Person::setFirstName)
        .bind(Person.LAST_NAME, stringMapper, Person::getLastName, Person::setLastName);
    final var courseMapper = new ObjectMapper<>(Course::new)
        .bind(Course.SUBJECT, stringMapper, Course::getSubject, Course::setSubject)
        .bind(Course.TEACHER, personMapper, Course::getTeacher, Course::setTeacher)
        .bind(Course.STUDENTS, new ArrayMapper<>(personMapper), Course::getStudents,
            Course::setStudents);

    final Course course = new Course()
        .setSubject("Mathematics")
        .setTeacher(new Person()
            .setFirstName("Obi-Wan")
            .setLastName("Kenobi"))
        .setStudents(List.of(
            new Person()
                .setFirstName("Adam")
                .setLastName("Amber"),
            new Person()
                .setFirstName("Bela")
                .setLastName("Bourbon"),
            new Person()
                .setFirstName("Cecil")
                .setLastName("Cyclist"),
            new Person()
                .setFirstName("Danielle")
                .setLastName("Darth"),
            new Person()
                .setFirstName("Evelin")
                .setLastName("Ennor")
        ));

    Course courseResult = courseMapper
        .marshall(course)                             // turn to internal JSON representation
        .map(JsonValue::toString)                     // fully marshall it to a string
        .flatMap(parser::parse) // parse it back from a string
        .flatMap(courseMapper::unmarshall)            // unmarshall it to an object
        .unwrap();                                    // force unwrap
    Assertions.assertEquals(course, courseResult);
  }

  @Test
  void recordMarshallingWorksWithoutAnnotations() throws Exception {
    record Cat(String name, String owner) {}
    Cat cat = new Cat("Kabala", "aestallon");
    Cat reparsedCat = jsonTypeMapperProvider
        .provide(Cat.class)
        .flatMap(mapper -> mapper.marshall(cat))
        .map(JsonValue::toString)
        .flatMap(parser::parse)
        .flatMap(json -> jsonTypeMapperProvider
            .provide(Cat.class)
            .flatMap(mapper -> mapper.unmarshall(json)))
        .unwrap();
    System.out.println("original cat : " + cat);
    System.out.println("reparsed cat : " + reparsedCat);
    Assertions.assertEquals(cat, reparsedCat);

    record Owner(String name, String hometown) {}
    record Dog(String name, Owner owner) {}
    Dog dog = new Dog("Rex", new Owner("John", "Budapest"));

    Dog reparsedDog = jsonTypeMapperProvider
        .provide(Dog.class)
        .flatMap(mapper -> mapper.marshall(dog))
        .map(JsonValue::toString)
        .flatMap(parser::parse)
        .flatMap(json -> jsonTypeMapperProvider
            .provide(Dog.class)
            .flatMap(mapper -> mapper.unmarshall(json)))
        .unwrap();
    Assertions.assertEquals(dog, reparsedDog);

    record Household(String nickname, List<Dog> dogs) {}
    Household household = new Household("nickname", List.of(
        new Dog("Rex", new Owner("John", "Budapest")),
        new Dog("Bl0ki", new Owner("Emily", "Budapest")),
        new Dog("Fasz", new Owner("John", "Budapest")),
        new Dog("Kecske", new Owner("Blaize", "Budapest"))
    ));
    Household reparsedHousehold = jsonTypeMapperProvider.provide(Household.class)
        .flatMap(mapper -> mapper.marshall(household))
        .map(JsonValue::toString)
        .flatMap(parser::parse)
        .flatMap(json -> jsonTypeMapperProvider
            .provide(Household.class)
            .flatMap(mapper -> mapper.unmarshall(json)))
        .unwrap();
    Assertions.assertEquals(household, reparsedHousehold);
  }

  @Test
  void parsingObjectMapFromFileAndMarshallingItBack_correctlyTransformsTheData() throws Exception {
    final JsonValueMapper valueMapper = new JsonValueMapper();

    InputStream testResource01 = JsonMarshallerTest.class.getResourceAsStream("/test01.json");
    Assertions.assertNotNull(testResource01);
    try (var reader = new BufferedReader(new InputStreamReader(testResource01))) {
      String jsonString = reader.lines().collect(joining());
      Result<Object> result = parser.parse(jsonString).flatMap(valueMapper::unmarshall);
      assertThat(result)
          .isNotNull()
          .matches(Result::isOk);
      Object objectMap = result.unwrap();
      assertThat(objectMap).isInstanceOf(Map.class);

      @SuppressWarnings("unchecked")
      Map<String, Object> map = (Map<String, Object>) objectMap;
      assertThat(map).containsOnlyKeys("companyName", "address", "departments", "facilities");
      @SuppressWarnings("unchecked")
      Map<String, Object> facilities = (Map<String, Object>) map.get("facilities");
      assertThat(facilities)
          .returns(true, m -> m.get("cafeteria"))
          .returns(true, m -> m.get("gym"));

      System.out.println(objectMap);

      record Employee(String name, String position) {}
      record Department(String name, List<Employee> employees) {}
      record Address(String street, String city, String state, String postalCode) {}
      record ConferenceRoom(String name, long capacity) {}
      record Facilities(boolean cafeteria, boolean gym, List<ConferenceRoom> conferenceRooms) {}
      record Company(String companyName, Address address, List<Department> departments,
                     Facilities facilities) {}
      Company company = parser.parse(jsonString)
          .flatMap(json -> jsonTypeMapperProvider.provide(Company.class)
              .flatMap(companyMapper -> companyMapper.unmarshall(json)))
          .unwrap();
      Object objectMapAcquiredThroughRecord = jsonTypeMapperProvider.provide(Company.class)
          .flatMap(companyMapper -> companyMapper.marshall(company))
          .flatMap(valueMapper::unmarshall)
          .unwrap();
      assertThat(objectMapAcquiredThroughRecord).isEqualTo(objectMap);

      Object reparsedObjectMap = valueMapper
          .marshall(objectMap)
          .map(JsonValue::toString)
          .flatMap(parser::parse)
          .flatMap(valueMapper::unmarshall)
          .unwrap();
      assertThat(reparsedObjectMap).isEqualTo(objectMap);
    }
  }

}