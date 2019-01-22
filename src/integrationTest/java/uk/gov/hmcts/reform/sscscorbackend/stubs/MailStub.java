package uk.gov.hmcts.reform.sscscorbackend.stubs;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class MailStub {

    private final SimpleSmtpServer smtpServer;

    public MailStub(int port) throws IOException {
        smtpServer = SimpleSmtpServer.start(port);
    }

    public void stop() {
        smtpServer.stop();
    }

    public void hasEmailWithSubjectAndAttachment(String subject, byte[] attachment) {
        List<SmtpMessage> receivedEmails = smtpServer.getReceivedEmails();

        assertThat(receivedEmails, new TypeSafeMatcher<List<SmtpMessage>>() {
            @Override
            protected boolean matchesSafely(List<SmtpMessage> smtpMessages) {
                return receivedEmails.stream()
                        .anyMatch(receivedEmail -> subject.equals(receivedEmail.getHeaderValue("Subject")) &&
                                receivedEmail.getBody().contains(Arrays.toString(attachment)));
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("An email with a subject ").appendValue(subject);
            }

            @Override
            protected void describeMismatchSafely(List<SmtpMessage> item, Description mismatchDescription) {
                List<String> emailSubjects = item.stream().map(email -> email.getHeaderValue("Subject")).collect(toList());
                mismatchDescription.appendText(" got").appendValue(emailSubjects);
            }
        });
    }
}
