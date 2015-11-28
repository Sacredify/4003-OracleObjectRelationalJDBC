import oracle.sql.ARRAY;

import java.sql.Array;

/**
 * Represents a student
 * <p/>
 * Created by Mike on 11/28/2015.
 */
public class Student {

    private long studentNumber;

    private String name;

    private String gender;

    private ARRAY courses;

    private String phoneNumber;

    public Student(final long studentNumber,
                   final String name,
                   final String gender,
                   final ARRAY courses,
                   final String phoneNumber) {
        this.studentNumber = studentNumber;
        this.name = name;
        this.gender = gender;
        this.courses = courses;
        this.phoneNumber = phoneNumber;
    }

    public long getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(final long studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(final String gender) {
        this.gender = gender;
    }

    public ARRAY getCourses() {
        return courses;
    }

    public void setCourses(final ARRAY courses) {
        this.courses = courses;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

}
