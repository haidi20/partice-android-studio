package com.example.employee.model;

public class Employee {
    public String nrp;
    public String name;
    public String department;

    public Employee() {
    }

    public Employee(String nrp, String name, String department) {
        this.nrp = nrp;
        this.name = name;
        this.department = department;
    }

    public String getNrp() {
        return nrp;
    }

    public void setNrp(String nrp) {
        this.nrp = nrp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
