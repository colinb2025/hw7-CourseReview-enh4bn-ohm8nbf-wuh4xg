package edu.virginia.cs.hw7;

import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.*;

public class databaseManager implements databaseInterface{
    private Connection conn;
    private boolean connected = false;
    private static final String DatabaseURL = "jdbc:sqlite:Reviews.sqlite3";
    public  void createNewDatabase(){

    }
    public static void main(String[] args){
        databaseManager m = new databaseManager();
        m.connect();
        m.tableCreation();
        m.disconnect();
    }

    @Override
    public void connect() {
        try {
            if(!connected){
                Class.forName("org.sqlite.JDBC");
                conn = DriverManager.getConnection(DatabaseURL);
                conn.setAutoCommit(false);
                connected = true;
                System.out.println("Connected!");
            }
            else{
                throw new IllegalStateException("Manager already connected");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to connect to the database", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public void createCoursesTable() {
        try {
            Statement statement = conn.createStatement();
            statement.executeUpdate("CREATE TABLE Courses(" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "DepartmentNum INTEGER NOT NULL," +
                    "CatalogNum INTEGER NOT NULL);");
        }
        catch (SQLException e) {
            System.out.println("Failed to create courses table!");
            e.printStackTrace();
        }
    }
    public void createStudentsTable() {
        try {
            Statement statement = conn.createStatement();
            statement.executeUpdate("CREATE TABLE Students(" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "Name VARCHAR NOT NULL," +
                    "Password VARCHAR NOT NULL);");
        }
        catch (SQLException e) {
            System.out.println("Failed to create student table!");
            e.printStackTrace();
        }
    }
    public void createReviewsTable() {
        try {
            Statement statement = conn.createStatement();
            statement.executeUpdate("CREATE TABLE Reviews(" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "CourseID INTEGER NOT NULL REFERENCES Courses(ID) ON DELETE CASCADE," +
                    "StudentID INTEGER NOT NULL REFERENCES Students(ID) ON DELETE CASCADE," +
                    "Review VARCHAR(255) NOT NULL," +
                    "Rating INTEGER NOT NULL); ");
        }
        catch (SQLException e) {
            System.out.println("Failed to create reviews table!");
            e.printStackTrace();
        }
    }
    @Override
    public void tableCreation() {
        if(!connected){
            throw new IllegalStateException("not connected");
        }
        createCoursesTable();
        createReviewsTable();
        createStudentsTable();
    }

    public void deleteTables(){
        String dropReviews = "DROP TABLE Reviews;";
        String dropCourses = "DROP TABLE Courses;";
        String dropStudents = "DROP TABLE Students;";

        try (PreparedStatement stmt1 = conn.prepareStatement(dropReviews);
             PreparedStatement stmt2 = conn.prepareStatement(dropCourses);
             PreparedStatement stmt3 = conn.prepareStatement(dropStudents)) {
            stmt1.executeUpdate();
            stmt2.executeUpdate();
            stmt3.executeUpdate();

        }   catch (NullPointerException e){
            throw new IllegalStateException("Manager hasn't connected yet");
        }
        catch (SQLException e) {
            throw new IllegalStateException("Tables don't exist", e);
        }
    }

    @Override
    public void addCourses(Course course) {
        if(!connected){
            throw new IllegalStateException("Not connected");
        }
        String sql = "INSERT INTO Courses(DepartmentNum, CatalogNum) VALUES (?, ?)";
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, course.getDepartment());
            stmt.setInt(2, course.getCatalogNumber());
            stmt.addBatch();
            stmt.executeBatch();
        }
        catch (SQLException e){
            throw new IllegalStateException("failed to add stop",e );
        }
        catch (IllegalArgumentException e){
            throw new IllegalArgumentException("Stop is already in the table",e );
        }

    }

    @Override
    public void addStudent(Student student) {
        if(!connected){
            throw new IllegalStateException("Not connected");
        }
        String sql = "INSERT INTO Students(Name, Password) VALUES (?, ?)";
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, student.getUserName());
            stmt.setString(2, student.getPassword());
            stmt.addBatch();
            stmt.executeBatch();
        }
        catch (SQLException e){
            throw new IllegalStateException("failed to add stop",e );
        }
        catch (IllegalArgumentException e){
            throw new IllegalArgumentException("Stop is already in the table",e );
        }

    }

    @Override
    public void addReview(Review review) {

    }

    @Override
    public Student getLogin(String username) throws SQLException {
        String sql = "SELECT Password FROM Students Name = "+username+";";
        PreparedStatement stmt= conn.prepareStatement(sql);
        ResultSet  rs= stmt.executeQuery();
        String name = rs.getString(1);
        String password = rs.getString(2);
        Student student = new Student(name, password);
        return student;
    }
    @Override
    public ArrayList<Course> getCourses() throws SQLException {
        ArrayList<Course> result = new ArrayList<>();
        String sql = "SELECT * FROM Courses;";
        PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            int department = rs.getInt(1);
            int catalog = rs.getInt(2);
            Course c = new Course(department, catalog);
            result.add(c);

        }

        return result;
    }

    @Override
    public ArrayList<Review> getReviews() {
        return null;
    }

    @Override
    public void disconnect() {
        try {
            if (connected) {
                conn.commit();
                conn.close();
                System.out.println("Disconnected!");
            } else {
                throw new IllegalStateException("Not connected");
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
