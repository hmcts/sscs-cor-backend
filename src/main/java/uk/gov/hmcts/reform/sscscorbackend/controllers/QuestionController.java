package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.sscscorbackend.domain.Answer;
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

    @ApiOperation(value = "Answer a question",
            notes = "Answers a question"
    )
    @ApiResponses(value = {@ApiResponse(code = 204, message = "Answer saved") })
    @RequestMapping(method = RequestMethod.PUT, value = "questions/{questionId}", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> updateAnswer(@PathVariable String onlineHearingId,
                                       @PathVariable String questionId,
                                       @RequestBody Answer newAnswer) {
        questionService.updateAnswer(onlineHearingId, questionId, newAnswer.getAnswer());
        return ResponseEntity.noContent().build();
    }
}
