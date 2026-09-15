package com.sumith.collabpad.dto;

import java.util.List;

public class EnrollmentRequest {

    private String companyName;
    private List<String> selectedDepartments;

    public EnrollmentRequest() {
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public List<String> getSelectedDepartments() {
        return selectedDepartments;
    }

    public void setSelectedDepartments(List<String> selectedDepartments) {
        this.selectedDepartments = selectedDepartments;
    }
}
