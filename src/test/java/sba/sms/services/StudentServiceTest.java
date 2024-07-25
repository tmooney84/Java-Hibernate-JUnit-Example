package sba.sms.services;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sba.sms.models.Course;
import sba.sms.models.Student;
import sba.sms.utils.HibernateUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

class StudentServiceTest {
    private static StudentService studentService;
    private static CourseService courseService;

    @BeforeAll
    public static void preBuild() {
        studentService = new StudentService();
        courseService = new CourseService();
    }

    @BeforeEach
    public void beginSession() {
        //delete databases before test
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.createQuery("delete from Student").executeUpdate();
            session.createQuery("delete from Course").executeUpdate();
            transaction.commit();
        }
    }

    @Test
    public void testCreateStudent() {
        Student student = new Student("bondj7@gmail.com", "James Bond", "MI6-007", Collections.emptySet());
        studentService.createStudent(student);

        Student retrievedStudent = studentService.getStudentByEmail("bondj7@gmail.com");
        assertNotNull(retrievedStudent);
        assertEquals("James Bond", retrievedStudent.getName());
        assertEquals("MI6-007", retrievedStudent.getPassword());
    }

    @Test
    public void testGetAllStudents() {
        Student student1 = new Student("bondj7@gmail.com", "James Bond", "MI6-007", Collections.emptySet());
        Student student2 = new Student("david@gmail.com", "David Gilmour", "meddle", Collections.emptySet());
        studentService.createStudent(student1);
        studentService.createStudent(student2);

        List<Student> students = studentService.getAllStudents();
        assertNotNull(students);
        assertEquals(2, students.size());
        assertThat(students).extracting(Student::getEmail).containsExactlyInAnyOrder("bondj7@gmail.com", "david@gmail.com");
    }

    @Test
    public void testValidateStudent() {
        Student student = new Student("bondj7@gmail.com", "James Bond", "MI6-007", Collections.emptySet());
        studentService.createStudent(student);

        assertTrue(studentService.validateStudent("bondj7@gmail.com", "MI6-007"));
        assertFalse(studentService.validateStudent("bondj7@gmail.com", "falsepassword"));
    }

    @Test
    public void testRegisterStudentToCourse() {
        Course course = new Course("Pink Floyd Studies", "William");
        courseService.createCourse(course);
        Student student = new Student("bondj7@gmail.com", "James Bond", "MI6-007", Collections.emptySet());
        studentService.createStudent(student);
        studentService.registerStudentToCourse("bondj7@gmail.com", 1);
        Student retrievedStudent = studentService.getStudentByEmail("bondj7@gmail.com");

        assertNotNull(retrievedStudent);
        assertEquals(1, retrievedStudent.getCourses().size());
        assertEquals("Pink Floyd Studies", retrievedStudent.getCourses().iterator().next().getName());
    }

    @Test
    public void testGetStudentCourses() {
        Course course = new Course("Pink Floyd Studies", "William");
        courseService.createCourse(course);
        Student student = new Student("bondj7@gmail.com", "James Bond", "MI6-007", new HashSet<>());
        studentService.createStudent(student);
        studentService.registerStudentToCourse("bondj7@gmail.com", course.getId());

        List<Course> courses = studentService.getStudentCourses("bondj7@gmail.com");
        assertEquals(1, studentService.getStudentCourses("bondj7@gmail.com").size());
    }
}
