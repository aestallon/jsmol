package hu.aestallon.jsmol.marshaller.impl;

import hu.aestallon.jsmol.json.JsonValue;
import hu.aestallon.jsmol.marshaller.ArrayMapper;
import hu.aestallon.jsmol.marshaller.JsonTypeMapper;
import hu.aestallon.jsmol.marshaller.JsonTypeMapperProvider;
import hu.aestallon.jsmol.parser.JsmolParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

class JsonMarshallerTest {

  private final JsonTypeMapperProvider jsonTypeMapperProvider = new JsonTypeMapperProviderImpl();
  private final JsonTypeMapper<String> stringMarshaller       = JsonPrimitiveMapper.STRING_MAPPER;

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
    final var personMarshaller = new ObjectMapper<>(Person::new)
        .bind(
            Person.FIRST_NAME, stringMarshaller, Person::getFirstName, stringMarshaller,
            Person::setFirstName)
        .bind(
            Person.LAST_NAME, stringMarshaller, Person::getLastName, stringMarshaller,
            Person::setLastName);
    final var courseMarshaller = new ObjectMapper<>(Course::new)
        .bind(
            Course.SUBJECT, stringMarshaller, Course::getSubject, stringMarshaller,
            Course::setSubject)
        .bind(Course.TEACHER, personMarshaller, Course::getTeacher, personMarshaller,
            Course::setTeacher)
        .bind(Course.STUDENTS, new ArrayMapper<>(personMarshaller), Course::getStudents,
            new ArrayMapper<>(personMarshaller),
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

    Course courseResult = courseMarshaller
        .marshall(course)                                // turn to internal JSON representation
        .map(JsonValue::toString)                        // fully marshall it to a string
        .flatMap(str -> new JsmolParser().parse(str)) // parse it back from a string
        .flatMap(courseMarshaller::unmarshall)           // unmarshall it to an object
        .unwrap();                                       // force unwrap
    Assertions.assertEquals(course, courseResult);
  }

  @Test
  void recordMarshallingWorksWithoutAnnotations() throws Exception {
    record Cat(String name, String owner) {}
    Cat cat = new Cat("Kabala", "aestallon");
    jsonTypeMapperProvider
        .provide(Cat.class)
        .flatMap(mapper -> mapper.marshall(cat))
        .map(JsonValue::toString)
        .unwrap();

    record Owner(String name, String hometown) {}
    record Dog(String name, Owner owner) {}
    Dog dog = new Dog("Rex", new Owner("John", "Budapest"));

    jsonTypeMapperProvider
        .provide(Dog.class)
        .flatMap(mapper -> mapper.marshall(dog))
        .map(JsonValue::toString)
        .unwrap();

    record Household(String nickname, List<Dog> dogs) {}
    Household household = new Household("nickname", List.of(
        new Dog("Rex", new Owner("John", "Budapest")),
        new Dog("Bl0ki", new Owner("Emily", "Budapest")),
        new Dog("Fasz", new Owner("John", "Budapest")),
        new Dog("Kecske", new Owner("Blaize", "Budapest"))
    ));
    jsonTypeMapperProvider.provide(Household.class)
        .flatMap(mapper -> mapper.marshall(household))
        .map(JsonValue::toString)
        .unwrap();

  }

}