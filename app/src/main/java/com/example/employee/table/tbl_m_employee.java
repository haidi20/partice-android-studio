package com.example.employee.table;

import com.orm.SugarRecord;

public class tbl_m_employee extends SugarRecord {
    public String nrp;
    public String nama;
    public String Department;

    public tbl_m_employee() {
    }

    public tbl_m_employee(String nrp, String nama, String department) {
        this.nrp = nrp;
        this.nama = nama;
        Department = department;
    }

    public String getNrp() {
        return nrp;
    }

    public void setNrp(String nrp) {
        this.nrp = nrp;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getDepartment() {
        return Department;
    }

    public void setDepartment(String department) {
        Department = department;
    }
}
