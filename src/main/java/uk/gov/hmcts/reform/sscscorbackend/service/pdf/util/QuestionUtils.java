package uk.gov.hmcts.reform.sscscorbackend.service.pdf.util;

import static java.util.stream.Collectors.toList;

import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscscorbackend.domain.QuestionSummary;

@Component
public class QuestionUtils {
    public List<QuestionSummary> cleanUpQuestionsHtml(List<QuestionSummary> questions) {
        return questions.stream().map(questionSummary -> new QuestionSummary(
                questionSummary.getId(),
                questionSummary.getQuestionOrdinal(),
                questionSummary.getQuestionHeaderText(),
                getXhtmlFromHtml(questionSummary.getQuestionBodyText()),
                questionSummary.getAnswerState(),
                questionSummary.getAnswer())
        ).collect(toList());
    }

    private String getXhtmlFromHtml(String inputHtml) {
        final Document document = Jsoup.parse("<wrapperTag>" + inputHtml + "</wrapperTag>");
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        return document.selectFirst("wrapperTag").html();
    }
}
