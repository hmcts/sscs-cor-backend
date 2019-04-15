package uk.gov.hmcts.reform.sscscorbackend.stubs;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.junit.Assert;

public class MailStub {

    private final SimpleSmtpServer smtpServer;

    public MailStub(int port) throws IOException {
        smtpServer = SimpleSmtpServer.start(port);
    }

    public void stop() {
        smtpServer.stop();
        printEmail();
    }

    public void printEmail() {
        if (System.getenv("SHOW_EMAILS") != null) {
            smtpServer.getReceivedEmails().forEach(message -> {
                System.out.println("-------------- Received Email --------------");
                System.out.println("To: " + message.getHeaderValue("To"));
                System.out.println("Subject: " + message.getHeaderValue("Subject"));
                System.out.println();
                System.out.println(message.getBody());
                System.out.println("-------------- End Of Email --------------");
            });
        }
    }

    public void waitForEmailThatMatches(Predicate<SmtpMessage> matches, String expected) {
        List<SmtpMessage> receivedEmails = emptyList();
        for (int counter = 0; counter < 50; counter++) {
            receivedEmails = smtpServer.getReceivedEmails();
            if (receivedEmails.stream().anyMatch(matches)) {
                return;
            }
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        String emails = receivedEmails.stream()
                .map(email -> "To=\"" + email.getHeaderValue("To") + "\" Subject=\"" + email.getHeaderValue("Subject") + "\"")
                .collect(Collectors.joining("}\n\t{", "\t{", "}"));

        Assert.fail("Expected an email that matches " + expected + "\n" +
                " But got " + emails);
    }

    public void hasEmailWithSubjectAndAttachment(String subject, byte[] attachment) {
        waitForEmailThatMatches(receivedEmail -> subject.equals(
            receivedEmail.getHeaderValue("Subject")) && receivedEmail.getBody().contains(Arrays.toString(attachment)),
            "An email with a subject [" + subject + "] with attachment"
        );
    }

    public void hasEmailWithSubject(String subject) {
        waitForEmailThatMatches(
            receivedEmail -> subject.equals(receivedEmail.getHeaderValue("Subject")),
            "An email with a subject [" + subject + "]"
        );
    }

    public void hasEmailWithSubject(String toAddress, String subject) {
        waitForEmailThatMatches(
            receivedEmail -> subject.equals(receivedEmail.getHeaderValue("Subject")) && toAddress.equals(receivedEmail.getHeaderValue("To")),
            "An email sent to [" + toAddress + "] with a subject [" + subject + "]"
        );
    }

    public void hasNoEmails() {
        List<SmtpMessage> receivedEmails = smtpServer.getReceivedEmails();
        assertThat(receivedEmails, is(empty()));
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        new MailStub(1025);

        while (true) {
            Thread.sleep(50000L);
        }
    }
}
