package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.sscscorbackend.domain.Answer;
import uk.gov.hmcts.reform.sscscorbackend.domain.OnlineHearing;
import uk.gov.hmcts.reform.sscscorbackend.domain.Question;
import uk.gov.hmcts.reform.sscscorbackend.domain.QuestionRound;
import uk.gov.hmcts.reform.sscscorbackend.service.OnlineHearingService;
import uk.gov.hmcts.reform.sscscorbackend.service.QuestionService;

@RestController
@RequestMapping("/continuous-online-hearings")
public class QuestionController {

    private final QuestionService questionService;
    private final OnlineHearingService onlineHearingService;

    public QuestionController(@Autowired QuestionService questionService, @Autowired OnlineHearingService onlineHearingService) {
        this.questionService = questionService;
        this.onlineHearingService = onlineHearingService;
    }

    @ApiOperation(value = "Get an online hearing",
            notes = "Returns an online hearing for the email address. If the email has more than one case it " +
                    "will be the one that is for a PIP appeal with an online panel. This is expected to be called " +
                    "after a user had logged in and will result in a redirect to the question list page."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No online hearing found for email address"),
            @ApiResponse(code = 422, message = "Multiple online hearings found for email address")
    })
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<OnlineHearing> getOnlineHearing(
            @ApiParam(value = "email address of the appellant", example = "foo@bar.com") @RequestParam("email") String emailAddress) {
        try {
            Optional<OnlineHearing> onlineHearing = onlineHearingService.getOnlineHearing(emailAddress);
            return onlineHearing.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalStateException exc) {
            return ResponseEntity.unprocessableEntity().build();
        }

    }

    @ApiOperation(value = "Get a list of questions",
            notes = "Returns a list of the questions for the current round"
    )
    @RequestMapping(method = RequestMethod.GET, value = "{onlineHearingId}")
    public ResponseEntity<QuestionRound> getQuestionList(
            @ApiParam(value = "id of the hearing", example = "ID_1") @PathVariable String onlineHearingId) {
        QuestionRound questions = questionService.getQuestions(onlineHearingId);
        return ResponseEntity.ok(questions);
    }

    @ApiOperation(value = "Get a question",
            notes = "Returns a single question",
            response = Question.class
    )
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Question not found") })
    @RequestMapping(method = RequestMethod.GET, value = "{onlineHearingId}/questions/{questionId}")
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
    @RequestMapping(method = RequestMethod.PUT, value = "{onlineHearingId}/questions/{questionId}", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> updateAnswer(@PathVariable String onlineHearingId,
                                       @PathVariable String questionId,
                                       @RequestBody Answer newAnswer) {
        questionService.updateAnswer(onlineHearingId, questionId, newAnswer.getAnswer());
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "Submit an answer to a question",
            notes = "The question must be answered first then it can be submitted"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Answer saved"),
            @ApiResponse(code = 404, message = "Question has not already been answered")
    })
    @RequestMapping(method = RequestMethod.POST, value = "{onlineHearingId}/questions/{questionId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> submitAnswer(@PathVariable String onlineHearingId,
                                             @PathVariable String questionId) {
        boolean answerSubmitted = questionService.submitAnswer(onlineHearingId, questionId);
        if (answerSubmitted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @ApiOperation(value = "Extend a question round deadline",
            notes = "Extend by 7 days the date the questions in the current round have to be answered by."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Round extended"),
            @ApiResponse(code = 422, message = "Round cannot be extended extended")
    })
    @RequestMapping(method = RequestMethod.PATCH, value = "{onlineHearingId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public ResponseEntity<QuestionRound> extendQuestionRoundDeadline(@PathVariable String onlineHearingId) {
        return questionService.extendQuestionRoundDeadline(onlineHearingId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.unprocessableEntity().build());
    }
}
