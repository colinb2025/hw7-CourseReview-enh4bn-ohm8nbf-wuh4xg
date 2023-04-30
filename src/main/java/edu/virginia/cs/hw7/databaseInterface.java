package edu.virginia.cs.hw7;

import java.sql.SQLException;
import java.util.ArrayList;

public interface databaseInterface {
     /**
      * this is where we will make the new database if we need to
      * there was a weird requirement for it in the writeup
      */

     void createNewDatabase();
     /**
      * connects to the database
      */
     void connect() throws ClassNotFoundException, SQLException;

/**
 * this is where we should make the three tables
 * STUDENT table has:
 * idNumber AutoIncrement primarykey
 * userName, must be unique( use if not exist else throw)
 * password
 * COURSES:
 * id number AutoIncrement primary key
 * Department Number
 * Catalog number
 * REVIEWS
 * id number Auto increment primary key
 * foreign key to the Students Table
 * foreign key to courses table
 * text of the Review
 * int Rating
 *
 * @throw SQLException

 */
     void tableCreation() throws SQLException;

     /**
      * add a new course
      */
     void addCourses(Course course);

     /**
      * add a new student
      * checks if they are unique
      * should only be used by the scene interface
      */
     void addStudent(Student student);
     /**
      * adds a review
      * links with foreign keys to student and courses
      */
     void addReview(Review review, Course course, Student student);

     /**
      * get the Students login info
      * this will also be used in the scene thingy to let us move to the course page
      *
      */
     Student getLogin(String username) throws SQLException;

     /**
      * @return a list of all of the courses in the database
      * in the scene you should be able to click through the courses to get more info
      * bitz you can do a drop down if you want but that might be hard
      */
     ArrayList<Course> getCourses() throws SQLException;

     /**
      * should run through the courses and students table
      *
      * @return a list of reviews that goes with the course
      */
     ArrayList<Review> getReviews(Course course);

     void disconnect();



}
