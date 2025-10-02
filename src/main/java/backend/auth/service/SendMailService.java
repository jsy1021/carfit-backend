package backend.auth.service;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SendMailService {

    private final JavaMailSender mailSender;

    @Value("${email.id}")
    private String fromId;

    @Async("mailExecutor")
    public void sendEmail(String to, String subject, String contend){
        MimeMessagePreparator messagePreparator=
                mimeMessage -> {
            final MimeMessageHelper helper=new MimeMessageHelper(mimeMessage,true,"UTF-8");
            helper.setFrom(fromId);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(contend, true);
                };
        mailSender.send(messagePreparator);
    }
}
