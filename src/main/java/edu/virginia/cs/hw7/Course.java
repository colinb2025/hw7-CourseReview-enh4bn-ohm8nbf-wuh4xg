package edu.virginia.cs.hw7;

public class Course {
    private int department;
    private int catalogNumber;

    public Course(int department, int catalogNumber) {
        this.department = department;
        this.catalogNumber = catalogNumber;
    }

    public int getDepartment() {
        return department;
    }

    public void setDepartment(int department) {
        this.department = department;
    }

    public int getCatalogNumber() {
        return catalogNumber;
    }

    public void setCatalogNumber(int catalogNumber) {
        this.catalogNumber = catalogNumber;
    }
}
