package com.example.employee.table;

import com.orm.SugarRecord;

public class tbl_m_category extends SugarRecord {
    public String name;

    public tbl_m_category() {
    }

    public tbl_m_category(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
