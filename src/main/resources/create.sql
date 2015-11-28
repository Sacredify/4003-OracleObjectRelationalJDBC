-- Forward declarations
CREATE OR REPLACE TYPE STUDENT_T;
/
CREATE OR REPLACE TYPE COURSE_T;
/
CREATE OR REPLACE TYPE COURSE_NT AS TABLE OF REF COURSE_T;
/
CREATE OR REPLACE TYPE STUDENT_NT AS TABLE OF REF STUDENT_T;
/

-- Students
CREATE OR REPLACE TYPE STUDENT_T AS OBJECT (
  S#      INTEGER,
  NAME    VARCHAR2(255),
  GENDER  VARCHAR2(1),
  COURSES COURSE_NT
) NOT FINAL;
/

CREATE OR REPLACE TYPE UNDERGRADUATE_T UNDER STUDENT_T (
  PHONE_NUMBER VARCHAR2(10)
);/

CREATE OR REPLACE TYPE GRADUATE_T UNDER STUDENT_T (
  PHONE_NUMBER VARCHAR2(10)
);
/

CREATE TABLE STUDENTS OF STUDENT_T (
  S# PRIMARY KEY ,
NAME NOT NULL UNIQUE,
  GENDER CHECK (GENDER IN ('M', 'F'
)
)
)
NESTED TABLE COURSES STORE AS STUDENT_COURSES;
/

-- Courses
CREATE OR REPLACE TYPE COURSE_T AS OBJECT (
  C#            INTEGER,
  NAME          VARCHAR2(255),
  PREREQUISITES COURSE_NT,
  STUDENTS      STUDENT_NT
);
/

CREATE TABLE COURSES OF COURSE_T (
  "C#" PRIMARY KEY,
NAME NOT NULL UNIQUE
)
NESTED TABLE PREREQUISITES STORE AS COURSE_PREREQUISITES
NESTED TABLE STUDENTS STORE AS COURSE_STUDENTS;
/


-- Data insertion
-- Courses
INSERT INTO COURSES VALUES (COURSE_T(1, 'Math', COURSE_NT(), STUDENT_NT()));
INSERT INTO COURSES VALUES (COURSE_T(2, 'Chem', COURSE_NT(), STUDENT_NT()));
INSERT INTO COURSES VALUES (COURSE_T(3, 'Comp', COURSE_NT(), STUDENT_NT()));
INSERT INTO COURSES VALUES (COURSE_T(4, 'Java', COURSE_NT(), STUDENT_NT()));
INSERT INTO COURSES VALUES (COURSE_T(5, 'OS', COURSE_NT(), STUDENT_NT()));
INSERT INTO COURSES VALUES (COURSE_T(6, 'DBMS', COURSE_NT(), STUDENT_NT()));


-- Course prerequisites
INSERT INTO TABLE (SELECT C.PREREQUISITES
                   FROM COURSES C
                   WHERE C.NAME = 'Comp') SELECT REF(C)
                                          FROM COURSES C
                                          WHERE C.NAME IN ('Math', 'Chem');
INSERT INTO TABLE (SELECT C.PREREQUISITES
                   FROM COURSES C
                   WHERE C.NAME = 'Java') SELECT REF(C)
                                          FROM COURSES C
                                          WHERE C.NAME IN ('Comp');
INSERT INTO TABLE (SELECT C.PREREQUISITES
                   FROM COURSES C
                   WHERE C.NAME = 'OS') SELECT REF(C)
                                        FROM COURSES C
                                        WHERE C.NAME IN ('Comp');
INSERT INTO TABLE (SELECT C.PREREQUISITES
                   FROM COURSES C
                   WHERE C.NAME = 'DBMS') SELECT REF(C)
                                          FROM COURSES C
                                          WHERE C.NAME IN ('Java', 'OS');

-- Students
INSERT INTO STUDENTS VALUES (UNDERGRADUATE_T(1, 'Alan', 'M', COURSE_NT(), NULL));
INSERT INTO STUDENTS VALUES (UNDERGRADUATE_T(2, 'Beth', 'F', COURSE_NT(), NULL));
INSERT INTO STUDENTS VALUES (UNDERGRADUATE_T(3, 'Cole', 'M', COURSE_NT(), NULL));
INSERT INTO STUDENTS VALUES (UNDERGRADUATE_T(4, 'Dora', 'F', COURSE_NT(), NULL));
INSERT INTO STUDENTS VALUES (GRADUATE_T(5, 'Jack', 'M', COURSE_NT(), NULL));
INSERT INTO STUDENTS VALUES (GRADUATE_T(6, 'Lisa', 'F', COURSE_NT(), NULL));
INSERT INTO STUDENTS VALUES (GRADUATE_T(7, 'Mike', 'M', COURSE_NT(), NULL));
INSERT INTO STUDENTS VALUES (GRADUATE_T(8, 'Sara', 'F', COURSE_NT(), NULL));

-- Student courses
INSERT INTO TABLE (SELECT S.COURSES
                   FROM STUDENTS S
                   WHERE S.NAME = 'Alan') SELECT REF(C)
                                          FROM COURSES C
                                          WHERE C.NAME IN ('Math', 'Chem');
INSERT INTO TABLE (SELECT S.COURSES
                   FROM STUDENTS S
                   WHERE S.NAME = 'Dora') SELECT REF(C)
                                          FROM COURSES C
                                          WHERE C.NAME IN ('Comp');
INSERT INTO TABLE (SELECT S.COURSES
                   FROM STUDENTS S
                   WHERE S.NAME = 'Cole') SELECT REF(C)
                                          FROM COURSES C
                                          WHERE C.NAME IN ('Java');
INSERT INTO TABLE (SELECT S.COURSES
                   FROM STUDENTS S
                   WHERE S.NAME = 'Jack') SELECT REF(C)
                                          FROM COURSES C
                                          WHERE C.NAME IN ('Java');
INSERT INTO TABLE (SELECT S.COURSES
                   FROM STUDENTS S
                   WHERE S.NAME = 'Lisa') SELECT REF(C)
                                          FROM COURSES C
                                          WHERE C.NAME IN ('OS');
INSERT INTO TABLE (SELECT S.COURSES
                   FROM STUDENTS S
                   WHERE S.NAME = 'Sara') SELECT REF(C)
                                          FROM COURSES C
                                          WHERE C.NAME IN ('OS');
INSERT INTO TABLE (SELECT S.COURSES
                   FROM STUDENTS S
                   WHERE S.NAME = 'Jack') SELECT REF(C)
                                          FROM COURSES C
                                          WHERE C.NAME IN ('DBMS');
INSERT INTO TABLE (SELECT S.COURSES
                   FROM STUDENTS S
                   WHERE S.NAME = 'Mike') SELECT REF(C)
                                          FROM COURSES C
                                          WHERE C.NAME IN ('DBMS');

-- Course students
INSERT INTO TABLE (SELECT C.STUDENTS
                   FROM COURSES C
                   WHERE C.NAME = 'Math') SELECT REF(S)
                                          FROM STUDENTS S
                                          WHERE S.NAME IN ('Alan');
INSERT INTO TABLE (SELECT C.STUDENTS
                   FROM COURSES C
                   WHERE C.NAME = 'Chem') SELECT REF(S)
                                          FROM STUDENTS S
                                          WHERE S.NAME IN ('Alan');
INSERT INTO TABLE (SELECT C.STUDENTS
                   FROM COURSES C
                   WHERE C.NAME = 'Comp') SELECT REF(S)
                                          FROM STUDENTS S
                                          WHERE S.NAME IN ('Dora');
INSERT INTO TABLE (SELECT C.STUDENTS
                   FROM COURSES C
                   WHERE C.NAME = 'Java') SELECT REF(S)
                                          FROM STUDENTS S
                                          WHERE S.NAME IN ('Cole', 'Jack');
INSERT INTO TABLE (SELECT C.STUDENTS
                   FROM COURSES C
                   WHERE C.NAME = 'OS') SELECT REF(S)
                                        FROM STUDENTS S
                                        WHERE S.NAME IN ('Lisa', 'Sara');
INSERT INTO TABLE (SELECT C.STUDENTS
                   FROM COURSES C
                   WHERE C.NAME = 'DBMS') SELECT REF(S)
                                          FROM STUDENTS S
                                          WHERE S.NAME IN ('Jack', 'Mike');