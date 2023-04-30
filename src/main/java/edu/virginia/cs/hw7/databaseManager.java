package edu.virginia.cs.hw7;

import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.*;

public class databaseManager implements databaseInterface{
    private Connection conn;
    private boolean connected = false;
    private static final String DatabaseURL = "jdbc:sqlite:Reviews.sqlite3";
    public static void main(String[] args){
        databaseManager m = new databaseManager();
        Course c = new Course("CV",1700);
        Student s = new Student("James", "cunt");
        Review r = new Review("James", "Course was really ass", 1);
        m.connect();
//        m.addStudent(s);
        m.addReview(r, c, s);
        m.getReviews(c);
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
        String sql = "INSERT INTO Courses(DepartmentNum, CatalogNum) VALUES (?, ?)";
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, course.getDepartment());
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
            throw new IllegalStateException("failed to add stop",e );
        }
        catch (IllegalArgumentException e){
            throw new IllegalArgumentException("Stop is already in the table",e );
        }

    }

    @Override
    public void addReview(Review review, Course course, Student student) {
        try {
            int courseID;
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(String.format("SELECT * FROM Students WHERE Name = '%s' AND Password = '%s'", student.getUserName(), student.getPassword()));
            int studentID = rs.getInt("ID");
            try{
            rs = statement.executeQuery(String.format("SELECT * FROM Courses WHERE DepartmentNum = '%s' AND CatalogNum = %d",course.getDepartment(), course.getCatalogNumber()));
            courseID = rs.getInt("ID");}
            catch (SQLException e){
                addCourses(course);
                rs = statement.executeQuery(String.format("SELECT * FROM Courses WHERE DepartmentNum = '%s' AND CatalogNum = %d",course.getDepartment(), course.getCatalogNumber()));
                courseID = rs.getInt("ID");
            }
            String reviewText = review.getReviewText();
            int rating = review.getRating();
            statement.executeUpdate(String.format("INSERT INTO Reviews (CourseId, StudentID, Review, Rating)VALUES(%d, %d, '%s', %d)", courseID, studentID, reviewText, rating));
        }
        catch (SQLException e) {
            System.out.println("Could not add review!");
            e.printStackTrace();
        }
    }

    @Override
    public Student getLogin(String username) throws SQLException {
        String sql = String.format("SELECT * FROM Students WHERE Name = '%s'", username);
        PreparedStatement stmt= conn.prepareStatement(sql);
        ResultSet resultSet = stmt.executeQuery();
        String password = resultSet.getString(3);
        return new Student(username, password);
    }
    @Override
    public ArrayList<Course> getCourses() throws SQLException {
        ArrayList<Course> result = new ArrayList<>();
        String sql = "SELECT * FROM Courses;";
        PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            String department = rs.getString(1);
            int catalog = rs.getInt(2);
            Course c = new Course(department, catalog);
            result.add(c);

        }

        return result;
    }

    @Override
    public ArrayList<Review> getReviews(Course course) {
        String sql = "SELECT * FROM Courses WHERE DepartmentNum = '"+course.getDepartment()+"' AND CatalogNum ="+course.getCatalogNumber();
        ArrayList<Review> reviewArray = new ArrayList<>();
        try{
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            int courseID = rs.getInt("ID");
            stmt.close();
            Statement stmt1 = conn.createStatement();
            ResultSet rs1 = stmt1.executeQuery(String.format("SELECT * FROM Reviews WHERE CourseID = %d",courseID));
            while(rs1.next()) {
                Review curReview = new Review(null, null, 0);
                curReview.setReviewText(rs1.getString("Review"));
                curReview.setRating(rs1.getInt("Rating"));
                PreparedStatement stmt2 = conn.prepareStatement(String.format("SELECT * FROM Students WHERE ID = %d",rs1.getInt("StudentID")));
                ResultSet rs2 = stmt2.executeQuery();
                String studentName = rs2.getString("Name");
                curReview.setWrittenBy(studentName);
                reviewArray.add(curReview);
            }
            return reviewArray;
        } catch (SQLException e) {
            System.out.println("Could not get reviews!");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void disconnect() {
        try {
            if (connected) {
//                conn.commit();
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
