package uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.config.CcdRequestDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.CreateCcdCaseService;
import uk.gov.hmcts.reform.sscs.ccd.service.ReadCcdCaseService;
import uk.gov.hmcts.reform.sscs.ccd.service.SearchCcdCaseService;
import uk.gov.hmcts.reform.sscs.ccd.service.UpdateCcdCaseService;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.api.CcdAddUser;

@Slf4j
@Service
public class CorCcdService extends uk.gov.hmcts.reform.sscs.ccd.service.CcdService {
    private final CcdClient ccdClient;
    private final IdamService idamService;
    private final CcdRequestDetails ccdRequestDetails;

    public CorCcdService(CreateCcdCaseService createCcdCaseService,
                         SearchCcdCaseService searchCcdCaseService,
                         UpdateCcdCaseService updateCcdCaseService,
                         ReadCcdCaseService readCcdCaseService,
                         CcdClient ccdClient,
                         IdamService idamService,
                         CcdRequestDetails ccdRequestDetails) {
        super(createCcdCaseService, searchCcdCaseService, updateCcdCaseService, readCcdCaseService);

        this.ccdClient = ccdClient;
        this.idamService = idamService;
        this.ccdRequestDetails = ccdRequestDetails;
    }

    public void addUserToCase(String userIdToAdd, long caseId) {
        IdamTokens idamTokens = idamService.getIdamTokens();
        log.info("PANEL MEMBERS Adding user [" + userIdToAdd + "] to case [" + caseId + "]");
        ccdClient.addUserToCase(
                idamTokens.getIdamOauth2Token(),
                idamTokens.getServiceAuthorization(),
                idamTokens.getUserId(),
                ccdRequestDetails.getJurisdictionId(),
                ccdRequestDetails.getCaseTypeId(),
                caseId,
                new CcdAddUser(userIdToAdd)
        );
    }

    public void removeUserFromCase(String userIdToRemove, long caseId) {
        IdamTokens idamTokens = idamService.getIdamTokens();
        log.info("PANEL MEMBERS Removing user [" + userIdToRemove + "] from case [" + caseId + "]");
        ccdClient.removeUserFromCase(
                idamTokens.getIdamOauth2Token(),
                idamTokens.getServiceAuthorization(),
                idamTokens.getUserId(),
                ccdRequestDetails.getJurisdictionId(),
                ccdRequestDetails.getCaseTypeId(),
                caseId,
                userIdToRemove
        );
    }
}
