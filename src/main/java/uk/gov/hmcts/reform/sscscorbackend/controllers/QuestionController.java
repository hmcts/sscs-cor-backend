package uk.gov.hmcts.reform.sscscorbackend.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sscscorbackend.domain.Question;
import uk.gov.hmcts.reform.sscscorbackend.service.QuestionService;

@RestController
@RequestMapping("/continuous-online-hearings/{onlineHearingId}")
public class QuestionController {
    private final QuestionService questionService;

    public QuestionController(@Autowired QuestionService questionService) {
        this.questionService = questionService;
    }

    @ApiOperation(value = "Get a question",
            notes = "Returns a single question",
            response = Question.class
    )
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Question not found") })
    @RequestMapping(method = RequestMethod.GET, value = "questions/{questionId}")
    public ResponseEntity<Question> getQuestion(
            @ApiParam(value = "id of the hearing", example = "ID_1") @PathVariable String onlineHearingId,
            @ApiParam(value = "id of the question", example = "ID_1") @PathVariable String questionId) {
        Question question = questionService.getQuestion(onlineHearingId, questionId);

        if (question != null) {
            return ResponseEntity.ok(question);
        } else {
            return ResponseEntity.notFound().build();
        }

    }
}
