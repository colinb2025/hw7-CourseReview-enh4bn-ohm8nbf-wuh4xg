package edu.virginia.cs.hw7;

import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.*;

public class databaseManager implements databaseInterface{
    private Connection conn;
    private boolean connected = false;
    private static final String DatabaseURL = "jdbc:sqlite:Reviews.sqlite3";
    public static void main(String[] args) throws SQLException {
        databaseManager m = new databaseManager();
        Course c = new Course("CS",1700);
        m.connect();
        System.out.println(m.getReviews(c));
        m.disconnect();
    }

    @Override
    public void connect() {
        try {
            if(!connected){
                Class.forName("org.sqlite.JDBC");
                conn = DriverManager.getConnection(DatabaseURL);
                conn.setAutoCommit(true);
                connected = true;
                System.out.println("Connected!");
                DatabaseMetaData metaData = conn.getMetaData();
                ResultSet studentsTable = metaData.getTables(null, null, "Students", null);
                ResultSet coursesTable = metaData.getTables(null, null, "Courses", null);
                ResultSet reviewsTable = metaData.getTables(null, null, "Reviews", null);
                if(!studentsTable.next()){
                    createStudentsTable();
                } else if (!coursesTable.next()) {
                    createCoursesTable();
                }
                else if(!reviewsTable.next()){
                    createReviewsTable();
                }
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
                    "DepartmentNum VARCHAR NOT NULL," +
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
    public void clearTables(String query){
        String clearRoutesTable = "DELETE FROM Students;";
        String clearBusLinesTable = "DELETE FROM Courses;";
        String clearStopsTable = "DELETE FROM Reviews;";

        try (PreparedStatement stmt1 = conn.prepareStatement(clearRoutesTable);
             PreparedStatement stmt2 = conn.prepareStatement(clearBusLinesTable);
             PreparedStatement stmt3 = conn.prepareStatement(clearStopsTable)) {
            if(query.equals("students")){
                stmt1.executeUpdate();

            }
            else if(query.equals("courses")){
                stmt2.execute();
            }
            else if(query.equals("reviews")){
                stmt3.execute();
            } else if (query.equals("all")) {
                stmt1.executeUpdate();
                stmt2.executeUpdate();
                stmt3.executeUpdate();
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to clear tables", e);
        }
        if (!connected) {
            throw new IllegalStateException("Manager hasn't connected yet");
        }
    }

    @Override
    public void addCourses(Course course) {
        if(!connected){
            throw new IllegalStateException("Not connected");
        }
        try{
            ArrayList<Course> c =getCourses();
            if(c.size()!=0){
            for(int i=0; i<c.size(); i++){
            if(c.get(i).getCatalogNumber()==course.getCatalogNumber()&& c.get(i).getDepartment()==course.getDepartment()){
                throw new IllegalArgumentException("already exists");
                    }
                }
            }
        } catch (SQLException e) {

        }
        String sql = "INSERT INTO Courses(DepartmentNum, CatalogNum) VALUES (?, ?)";
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, course.getDepartment());
            stmt.setInt(2, course.getCatalogNumber());
            stmt.addBatch();
            stmt.executeBatch();
        }
        catch (SQLException e){
            throw new IllegalStateException("failed to add Course",e );
        }
        catch (IllegalArgumentException e){
            throw new IllegalArgumentException("Course is already in the table",e );
        }

    }

    @Override
    public void addStudent(Student student) {
        if(!connected){
            throw new IllegalStateException("Not connected");
        }
        try{
        String sql1 = "SELECT * FROM Students WHERE Name ='"+ student.getUserName()+"';";
        PreparedStatement stmt = conn.prepareStatement(sql1);
        ResultSet rs = stmt.executeQuery();
        if(rs.getString(2).equals(student.getUserName())){
            System.out.println(true);
            throw new IllegalStateException("user already exists");
        }

        } catch (SQLException e) {
        }
        String sql = "INSERT INTO Students (Name,Password )VALUES (?, ?)";
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, student.getUserName());
            stmt.setString(2, student.getPassword());
            stmt.addBatch();
            stmt.executeBatch();
        }
        catch (SQLException e){
            throw new IllegalStateException("failed to add student",e );
        }
        catch (IllegalArgumentException e){
            throw new IllegalArgumentException("Student is already in the table",e );
        }

    }

    @Override
    public void addReview(Review review) {

    }

    @Override
    public Student getLogin(String username) throws SQLException {
        String sql = String.format("SELECT * FROM Students WHERE Name = \'%s\'", username);
        PreparedStatement stmt= conn.prepareStatement(sql);
        ResultSet resultSet = stmt.executeQuery();
        String password = resultSet.getString(3);
        Student student = new Student(username, password);
        return student;
    }
    @Override
    public ArrayList<Course> getCourses() throws SQLException {
        ArrayList<Course> result = new ArrayList<>();
        String sql = "SELECT * FROM Courses;";
        PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            String department = rs.getString(2);
            int catalog = rs.getInt(3);
            Course c = new Course(department, catalog);
            result.add(c);

        }

        return result;
    }

    @Override
    public ArrayList<Review> getReviews(Course course) {
    String sql = "SELECT * FROM Reviews WHERE CourseID = (SELECT ID FROM Courses WHERE DepartmentNum = '"+course.getDepartment()+"' AND CatalogNum = "+course.getCatalogNumber()+")";
        try{
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            System.out.print(rs.getString(4));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void disconnect() {
        try {
            if (connected) {
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
