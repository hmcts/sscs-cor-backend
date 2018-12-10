package uk.gov.hmcts.reform.sscscorbackend.domain.pdf;

import java.util.Objects;

public class PdfAppealDetails {
    private final String title;
    private final String firstName;
    private final String surname;
    private final String nino;
    private final String caseReference;

    public PdfAppealDetails(String title, String firstName, String surname, String nino, String caseReference) {
        this.title = title;
        this.firstName = firstName;
        this.surname = surname;
        this.nino = nino;
        this.caseReference = caseReference;
    }

    public String getTitle() {
        return title;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getSurname() {
        return surname;
    }

    public String getNino() {
        return nino;
    }

    public String getCaseReference() {
        return caseReference;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PdfAppealDetails that = (PdfAppealDetails) o;
        return Objects.equals(title, that.title) &&
                Objects.equals(firstName, that.firstName) &&
                Objects.equals(surname, that.surname) &&
                Objects.equals(nino, that.nino) &&
                Objects.equals(caseReference, that.caseReference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, firstName, surname, nino, caseReference);
    }

    @Override
    public String toString() {
        return "PdfAppealDetails{" +
                "title='" + title + '\'' +
                ", firstName='" + firstName + '\'' +
                ", surname='" + surname + '\'' +
                ", nino='" + nino + '\'' +
                ", caseReference='" + caseReference + '\'' +
                '}';
    }
}
