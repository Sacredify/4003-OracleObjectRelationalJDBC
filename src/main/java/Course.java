import oracle.sql.ARRAY;

/**
 * Represents a course.
 * <p/>
 * Created by Mike on 11/28/2015.
 */
public class Course {

    private long courseNumber;

    private String name;

    private ARRAY prerequisiteCourses;

    private ARRAY enrolledStudents;

    public Course(final long courseNumber,
                  final String name,
                  final ARRAY prerequisiteCourses,
                  final ARRAY enrolledStudents) {
        this.courseNumber = courseNumber;
        this.name = name;
        this.prerequisiteCourses = prerequisiteCourses;
        this.enrolledStudents = enrolledStudents;
    }

    public long getCourseNumber() {
        return courseNumber;
    }

    public void setCourseNumber(final long courseNumber) {
        this.courseNumber = courseNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public ARRAY getPrerequisiteCourses() {
        return prerequisiteCourses;
    }

    public void setPrerequisiteCourses(final ARRAY prerequisiteCourses) {
        this.prerequisiteCourses = prerequisiteCourses;
    }

    public ARRAY getEnrolledStudents() {
        return enrolledStudents;
    }

    public void setEnrolledStudents(final ARRAY enrolledStudents) {
        this.enrolledStudents = enrolledStudents;
    }
}
