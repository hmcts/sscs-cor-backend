package uk.gov.hmcts.reform.sscscorbackend.service;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PdfCoverSheet {
    @JsonProperty("case_id")
    private final String caseId;
    @JsonProperty("address_line1")
    private final String addressLine1;
    @JsonProperty("address_line2")
    private final String addressLine2;
    @JsonProperty("address_town")
    private final String addressTown;
    @JsonProperty("address_county")
    private final String addressCounty;
    @JsonProperty("address_postcode")
    private final String addressPostcode;

    public PdfCoverSheet(String caseId,
                         String addressLine1,
                         String addressLine2,
                         String addressTown,
                         String addressCounty,
                         String addressPostcode) {
        this.caseId = caseId;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.addressTown = addressTown;
        this.addressCounty = addressCounty;
        this.addressPostcode = addressPostcode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PdfCoverSheet that = (PdfCoverSheet) o;

        if (caseId != null ? !caseId.equals(that.caseId) : that.caseId != null) {
            return false;
        }
        if (addressLine1 != null ? !addressLine1.equals(that.addressLine1) : that.addressLine1 != null) {
            return false;
        }
        if (addressLine2 != null ? !addressLine2.equals(that.addressLine2) : that.addressLine2 != null) {
            return false;
        }
        if (addressTown != null ? !addressTown.equals(that.addressTown) : that.addressTown != null) {
            return false;
        }
        if (addressCounty != null ? !addressCounty.equals(that.addressCounty) : that.addressCounty != null) {
            return false;
        }
        return addressPostcode != null ? addressPostcode.equals(that.addressPostcode) : that.addressPostcode == null;
    }

    @Override
    public int hashCode() {
        int result = caseId != null ? caseId.hashCode() : 0;
        result = 31 * result + (addressLine1 != null ? addressLine1.hashCode() : 0);
        result = 31 * result + (addressLine2 != null ? addressLine2.hashCode() : 0);
        result = 31 * result + (addressTown != null ? addressTown.hashCode() : 0);
        result = 31 * result + (addressCounty != null ? addressCounty.hashCode() : 0);
        result = 31 * result + (addressPostcode != null ? addressPostcode.hashCode() : 0);
        return result;
    }
}
