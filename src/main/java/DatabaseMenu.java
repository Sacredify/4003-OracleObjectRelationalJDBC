import oracle.jdbc.driver.OracleDriver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.lang.System.out;

/**
 * COMP 4004 - Assignment 5 - Part 4
 * <p/>
 * Created by Mike on 11/11/2015.
 */
public class DatabaseMenu {

    private static final String HOST_IP = "192.168.240.133:1521";

    private static final String USER_NAME = "oracle";

    private static final String PASSWORD = "oracle11g";

    private static DatabaseDAO databaseDAO = null;

    public static void main(final String[] args) {
        try {
            final Connection connection = connect(USER_NAME, PASSWORD, false);

            databaseDAO = new DatabaseDAO(connection);

            out.println("--- ASSIGNMENT 5 DATABASE MENU ---");
            printOptions();

            final Scanner input = new Scanner(System.in);

            while (true) {
                out.print("Enter command >>> ");
                final String command = input.nextLine();
                if (command != null) {
                    final String normalized = command.trim();
                    if (normalized.equals("quit")) {
                        break;
                    } else {
                        doCommand(normalized);
                    }
                } else {
                    break;
                }
                out.println("\n");
            }
            connection.close();
            out.println("Connection closed.");
        } catch (final Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static Connection connect(final String username, final String password, final boolean autoCommit) throws
            Exception {
        out.println("Registering oracle JDBC...");
        DriverManager.registerDriver(new OracleDriver());
        out.println("Registered. Connecting to oracle...");
        final Connection connection = DriverManager.getConnection(String.format("jdbc:oracle:thin:@%s:xe", HOST_IP),
                username,
                password);
        connection.setAutoCommit(autoCommit);
        out.println("Connected.");
        return connection;
    }

    private static void printOptions() {
        out.println("Available options:\n " +
                "\tinsert_course <course name>\n" +
                "\tdelete_course <course name>\n" +
                "\tinsert_student <name> <gender> <undergraduate?> <phone #>\n" +
                "\tdelete_student <name>\n" +
                "\ttake_course <student name> <course name>\n" +
                "\tdrop_course <student name> <course name>\n" +
                "\tadd_prerequisite <course name> <prerequisite name>\n" +
                "\thelp\n" +
                "\tquit\n" +
                "Please note that all arguments should be enclosed in quotes where < > are found. Example:\n" +
                "\tinsert_course \"COMP4003\"\n");
    }

    private static void doCommand(final String command) throws Exception {
        final String[] args = command.split(" ");
        try {
            switch (args[0].toLowerCase()) {
                case "help":
                    printOptions();
                    break;
                case "insert_course":
                    insertCourse(command);
                    break;
                case "delete_course":
                    deleteCourse(command);
                    break;
                case "insert_student":
                    insertStudent(command);
                    break;
                case "delete_student":
                    deleteStudent(command);
                    break;
                case "take_course":
                    takeCourse(command);
                    break;
                case "drop_course":
                    dropCourse(command);
                    break;
                case "add_prerequisite":
                    addPrerequisite(command);
                    break;
                default:
                    out.println(String.format("%s not recognized as a valid command.", args[0]));
            }
        } catch (final NullPointerException | NumberFormatException | IndexOutOfBoundsException ignored) {
            out.println("Invalid data entry. Ensure parameters are escaped with quotes.");
        } catch (final SQLException sqlException) {
            out.println("Error running command - " + sqlException.getMessage());
            sqlException.printStackTrace();
        }
    }

    private static String[] formatArgs(final String command) {
        final String[] args = command.split("\"");
        final String[] toReturn = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] != null) {
                toReturn[i] = args[i].replaceAll("\"", "").trim();
            }
        }
        final List<String> asList = new ArrayList<>();
        for (final String string : toReturn) {
            if (string != null && !string.isEmpty()) {
                asList.add(string);
            }
        }
        return asList.toArray(new String[toReturn.length]);
    }

    private static void insertCourse(final String command) throws SQLException {
        databaseDAO.persistCourse(formatArgs(command)[1]);
    }

    private static void deleteCourse(final String command) throws SQLException {
        databaseDAO.deleteCourse(formatArgs(command)[1]);
    }

    private static void insertStudent(final String command) throws SQLException {
        final String[] arguments = formatArgs(command);

        final String name = arguments[1];
        final char gender = arguments[2].charAt(0);
        final boolean undergraduate = Boolean.valueOf(arguments[3]);
        final String phone = arguments[5];

        databaseDAO.persistStudent(name, gender, undergraduate, phone);
    }

    private static void deleteStudent(final String command) throws SQLException {
        databaseDAO.deleteStudent(formatArgs(command)[1]);
    }

    private static void takeCourse(final String command) throws SQLException {
        final String[] arguments = formatArgs(command);

        final String studentName = arguments[1];
        final String courseName = arguments[2];

        databaseDAO.takeCourse(studentName, courseName);
    }

    private static void dropCourse(final String command) throws SQLException {
        final String[] arguments = formatArgs(command);

        final String studentName = arguments[1];
        final String courseName = arguments[2];

        databaseDAO.dropCourse(studentName, courseName);
    }

    private static void addPrerequisite(final String command) throws SQLException {
        final String[] arguments = formatArgs(command);

        final String courseName = arguments[1];
        final String prerequisite = arguments[2];

        databaseDAO.addPrerequisite(courseName, prerequisite);
    }

}
