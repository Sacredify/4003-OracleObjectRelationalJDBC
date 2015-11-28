import oracle.jdbc.OraclePreparedStatement;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;
import oracle.sql.STRUCT;
import oracle.sql.StructDescriptor;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import static java.lang.System.out;

/**
 * Data access object we'll use to find students.
 * <p/>
 * Created by Mike on 11/28/2015.
 */
@SuppressWarnings("ALL")
public final class DatabaseDAO {

    private final Connection connection;

    private final StructDescriptor undergraduateDescriptor;

    private final StructDescriptor graduateDescriptor;

    private final StructDescriptor coursesDescriptor;

    private final ArrayDescriptor studentsNTDescriptor;

    private final ArrayDescriptor coursesNTDescriptor;

    public DatabaseDAO(final Connection connection) throws SQLException {
        this.connection = connection;
        this.undergraduateDescriptor = StructDescriptor.createDescriptor("UNDERGRADUATE_T", this.connection);
        this.graduateDescriptor = StructDescriptor.createDescriptor("GRADUATE_T", this.connection);
        this.coursesDescriptor = StructDescriptor.createDescriptor("COURSE_T", this.connection);
        this.coursesNTDescriptor = ArrayDescriptor.createDescriptor("COURSE_NT", this.connection);
        this.studentsNTDescriptor = ArrayDescriptor.createDescriptor("STUDENT_NT", this.connection);
    }

    /**
     * Find a student object by the given student name.
     *
     * @param name the student number.
     * @return the student object for the given number, or null if none exists.
     */
    public Student findByStudentName(final String name) throws SQLException {

        final PreparedStatement statement = connection.prepareStatement(
                "SELECT VALUE(S) FROM STUDENTS S WHERE S.NAME = ?");
        statement.setString(1, name);

        final ResultSet result = statement.executeQuery();

        if (result.next()) {
            final STRUCT student = (STRUCT) result.getObject(1);
            final Object[] attributes = student.getAttributes();
            return new Student(((BigDecimal) attributes[0]).longValue(),
                    (String) attributes[1],
                    (String) attributes[2],
                    (ARRAY) attributes[3],
                    (String) attributes[4]);
        } else {
            out.println("No matching student found.");
            throw new IllegalStateException("should have checked for existing entity before reaching this.");
        }
    }

    /**
     * Find a course object by the given course name.
     *
     * @param name the course name.
     * @return the course object.
     */
    public Course findByCourseName(final String name) throws SQLException {
        final PreparedStatement statement = connection.prepareStatement(
                "SELECT VALUE(C) FROM COURSES C WHERE C.NAME = ?");
        statement.setString(1, name);

        final ResultSet result = statement.executeQuery();

        if (result.next()) {
            final STRUCT course = (STRUCT) result.getObject(1);
            final Object[] attributes = course.getAttributes();
            return new Course(((BigDecimal) attributes[0]).longValue(),
                    (String) attributes[1],
                    (ARRAY) attributes[2],
                    (ARRAY) attributes[3]);
        } else {
            out.println("No matching course found for object creation.");
            throw new IllegalStateException("should have checked for existing entity before reaching this.");
        }
    }

    /**
     * Save a new student to the database.
     *
     * @param name          the name of the student.
     * @param gender        their gender.
     * @param underGraduate whether or not they are an undergraduate.
     * @param phoneNumber   (optional) their phone number.
     * @throws SQLException if an error occurs with the persist.
     */
    public void persistStudent(final String name,
                               final char gender,
                               final boolean underGraduate,
                               final String phoneNumber) throws SQLException {

        if (this.studentExists(name)) {
            out.println("A student with that name already exists.");
            return;
        }

        final StructDescriptor insertDescriptor;
        if (underGraduate) {
            insertDescriptor = this.undergraduateDescriptor;
        } else {
            insertDescriptor = this.graduateDescriptor;
        }

        final ARRAY emptyCourses = new ARRAY(this.coursesNTDescriptor, connection, null);

        final Object[] attributes = new Object[5];
        attributes[0] = this.getNextIDForTable("STUDENTS");
        attributes[1] = name;
        attributes[2] = gender;
        attributes[3] = emptyCourses;
        attributes[4] = phoneNumber;

        final STRUCT studentStruct = new STRUCT(insertDescriptor, connection, attributes);

        final PreparedStatement statement = connection.prepareStatement("INSERT INTO STUDENTS VALUES (?)");
        ((OraclePreparedStatement) statement).setSTRUCT(1, studentStruct);
        statement.execute();
        out.println("Student added.");
    }

    public void deleteStudent(final String name) throws SQLException {
        if (!this.studentExists(name)) {
            out.println("No student with that name exists for deletion.");
            return;
        }

        final Student student = this.findByStudentName(name);

        final ARRAY enrolledCourses = student.getCourses();
        final int length = enrolledCourses.getOracleArray().length;

        if (length > 0) {
            out.println("Couldn't delete the student as they are still enrolled in " + length + " courses.");
        } else {
            final PreparedStatement statement = connection.prepareStatement("DELETE FROM STUDENTS S WHERE S.NAME = ?");
            statement.setString(1, name);
            statement.execute();
            out.println("Student deleted.");
        }
    }

    /**
     * Save a new course to the database.
     *
     * @param name the course name.
     * @throws SQLException if an error occurs with the persist.
     */
    public void persistCourse(final String name) throws SQLException {

        if (this.courseExists(name)) {
            out.println("A course with that name already exists.");
            return;
        }

        final ARRAY emptyStudents = new ARRAY(this.studentsNTDescriptor, connection, null);
        final ARRAY emptyPreRequisites = new ARRAY(this.coursesNTDescriptor, connection, null);

        final Object[] attributes = new Object[4];
        attributes[0] = this.getNextIDForTable("COURSES");
        attributes[1] = name;
        attributes[2] = emptyPreRequisites;
        attributes[3] = emptyStudents;

        final STRUCT courseStruct = new STRUCT(this.coursesDescriptor, connection, attributes);

        final PreparedStatement statement = connection.prepareStatement("INSERT INTO COURSES VALUES (?)");
        ((OraclePreparedStatement) statement).setSTRUCT(1, courseStruct);
        statement.execute();
        out.println("Course added.");
    }

    public void deleteCourse(final String name) throws SQLException {
        if (!this.courseExists(name)) {
            out.println("No course with that name exists for deletion.");
            return;
        }

        final Course course = this.findByCourseName(name);

        final ARRAY students = course.getEnrolledStudents();

        if (students.getOracleArray().length > 0) {
            out.println("Couldn't delete course as there are currently " + students.getOracleArray().length + " students enrolled.");
        } else {
            final PreparedStatement statement = connection.prepareStatement("DELETE FROM COURSES C WHERE C.NAME = ?");
            statement.setString(1, name);
            statement.execute();

            out.println("Course deleted.");
        }
    }

    public void takeCourse(final String studentName, final String courseName) throws SQLException {
        if (!this.studentExists(studentName)) {
            out.println("No student with that name exists.");
            return;
        } else if (!this.courseExists(courseName)) {
            out.println("No course with that name exists.");
            return;
        }

        final Student student = this.findByStudentName(studentName);
        final Course course = this.findByCourseName(courseName);

        final List<String> coursesEnrolledIn = this.extractEnrolledCourses(studentName);

        if (coursesEnrolledIn.contains(courseName)) {
            out.println("That student is already enrolled for that course.");
            return;
        }

        this.addCourseForStudent(studentName, courseName);
        this.addStudentToCourse(studentName, courseName);
        out.println("Associations created - student succesfully enrolled.");
    }

    public void dropCourse(final String studentName, final String courseName) throws SQLException {
        if (!this.studentExists(studentName)) {
            out.println("No student with that name exists.");
            return;
        } else if (!this.courseExists(courseName)) {
            out.println("No course with that name exists.");
            return;
        }

        final Student student = this.findByStudentName(studentName);
        final Course course = this.findByCourseName(courseName);

        final List<String> coursesEnrolledIn = this.extractEnrolledCourses(studentName);

        if (!coursesEnrolledIn.contains(courseName)) {
            out.println("That student isn't enrolled in that course.");
            return;
        }

        this.removeCourseFromStudent(studentName, courseName);
        this.removeStudentFromCourse(studentName, courseName);
        out.println("Associations removed - student succesfully dropped from the course.");
    }

    public void addPrerequisite(final String courseName, final String prerequisite) throws SQLException {
        if (!this.courseExists(courseName)) {
            out.println("No course with that name exists.");
            return;
        } else if (!this.courseExists(prerequisite)) {
            out.println("No prerequisite with that name exists.");
            return;
        }

        final Course course = this.findByCourseName(courseName);

        final List<String> prerequisites = this.extractPrerequisiteCourses(courseName);

        if (prerequisites.contains(prerequisite)) {
            out.println("The selected prerequisite is already prerequisite of the course.");
            return;
        }

        this.addPrerequisiteCourse(courseName, prerequisite);
        out.println("Prerequisite added.");
    }

    private long getNextIDForTable(final String tableName) throws SQLException {
        final PreparedStatement statement;
        if (tableName.equals("STUDENTS")) {
            statement = connection.prepareStatement("SELECT MAX(S#) FROM STUDENTS");
        } else {
            statement = connection.prepareStatement("SELECT MAX(C#) FROM COURSES");
        }

        final ResultSet result = statement.executeQuery();

        if (result.next()) {
            return result.getLong(1) + 1L;
        } else {
            return 0L;
        }
    }

    private boolean courseExists(final String name) throws SQLException {
        final PreparedStatement statement = connection.prepareStatement("SELECT * FROM COURSES C WHERE C.NAME = ?");
        statement.setString(1, name);
        return statement.executeQuery().next();
    }

    private boolean studentExists(final String name) throws SQLException {
        final PreparedStatement statement = connection.prepareStatement("SELECT * FROM STUDENTS S WHERE S.NAME = ?");
        statement.setString(1, name);
        return statement.executeQuery().next();
    }

    private List<String> extractEnrolledStudents(final String courseName) throws SQLException {
        final PreparedStatement statement = connection.prepareStatement(
                "SELECT value(e).name FROM courses c, TABLE (c.STUDENTS) e WHERE c.name = ?");
        statement.setString(1, courseName);

        final ResultSet result = statement.executeQuery();

        final List<String> names = new LinkedList<>();
        while (result.next()) {
            names.add(result.getString(1));
        }
        return names;
    }

    private List<String> extractPrerequisiteCourses(final String courseName) throws SQLException {
        final PreparedStatement statement = connection.prepareStatement(
                "SELECT value(p).name FROM courses c, TABLE (c.PREREQUISITES) p WHERE c.name = ?");
        statement.setString(1, courseName);

        final ResultSet result = statement.executeQuery();

        final List<String> names = new LinkedList<>();
        while (result.next()) {
            names.add(result.getString(1));
        }
        return names;
    }

    private List<String> extractEnrolledCourses(final String studentName) throws SQLException {
        final PreparedStatement statement = connection.prepareStatement(
                "SELECT value(e).name FROM STUDENTS s, TABLE (s.COURSES) e WHERE s.name = ?");
        statement.setString(1, studentName);

        final ResultSet result = statement.executeQuery();

        final List<String> names = new LinkedList<>();
        while (result.next()) {
            names.add(result.getString(1));
        }
        return names;
    }

    private void addStudentToCourse(final String studentName, final String courseName) throws SQLException {
        final PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO TABLE (SELECT C.STUDENTS FROM COURSES C WHERE C.NAME = ?) SELECT REF(S) FROM STUDENTS S WHERE S.NAME IN (?)");
        statement.setString(1, courseName);
        statement.setString(2, studentName);
        statement.execute();
    }

    private void addCourseForStudent(final String studentName, final String courseName) throws SQLException {
        final PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO TABLE (SELECT S.COURSES FROM STUDENTS S WHERE S.NAME = ?) SELECT REF(C) FROM COURSES C WHERE C.NAME IN (?)");
        statement.setString(1, studentName);
        statement.setString(2, courseName);
        statement.execute();
    }

    private void removeStudentFromCourse(final String studentName, final String courseName) throws SQLException {
        final PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM TABLE(SELECT C.STUDENTS FROM COURSES C WHERE C.NAME = ?) E WHERE value(E).NAME = ?");
        statement.setString(1, courseName);
        statement.setString(2, studentName);
        statement.execute();
    }

    private void removeCourseFromStudent(final String studentName, final String courseName) throws SQLException {
        final PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM TABLE(SELECT S.COURSES FROM STUDENTS S WHERE S.NAME = ?) E WHERE value(E).NAME = ?");
        statement.setString(1, studentName);
        statement.setString(2, courseName);
        statement.execute();
    }

    private void addPrerequisiteCourse(final String courseName, final String prerequisite) throws SQLException {
        final PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO TABLE (SELECT C.PREREQUISITES FROM COURSES C WHERE C.NAME = ?) SELECT REF(P) FROM COURSES P WHERE P.NAME IN (?)");
        statement.setString(1, courseName);
        statement.setString(2, prerequisite);
        statement.execute();
    }

}
