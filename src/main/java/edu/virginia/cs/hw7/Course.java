package edu.virginia.cs.hw7;

public class Course {
    private String department;
    private int catalogNumber;

    public Course(String department, int catalogNumber) {
        this.department = department;
        this.catalogNumber = catalogNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getCatalogNumber() {
        return catalogNumber;
    }

    public void setCatalogNumber(int catalogNumber) {
        this.catalogNumber = catalogNumber;
    }
}
