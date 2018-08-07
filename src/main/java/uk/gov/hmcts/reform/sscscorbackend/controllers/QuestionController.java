package uk.gov.hmcts.reform.sscscorbackend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @RequestMapping(value = "questions/{questionId}")
    public ResponseEntity<Question> getQuestion(@PathVariable String onlineHearingId, @PathVariable String questionId) {
        Question question = questionService.getQuestion(onlineHearingId, questionId);

        return ResponseEntity.ok(question);
    }
}
