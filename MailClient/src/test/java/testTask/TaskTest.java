package testTask;

import client.model.Email;
import client.tasks.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TaskTest {

    public static void main(String[] args){
        List<Email> mailList = new ArrayList<>();
        Consumer<List<Email>> addMails = mails -> {mailList.addAll(mails); for(Email mail : mailList)System.out.println("AddMails "+mail);};
        Consumer<String> statusPrint = status -> System.out.println(status);
        Consumer<Boolean> resultHandler = ok -> System.out.println(ok);

        Task ct = new ConnectionTask("testmail@test.com", false, addMails, statusPrint);
        List<String> recipient= (new ArrayList<>());
        recipient.add("testrecipient@test.com");
        Email testMail = new Email("t1", "testSender@test.com", recipient, "TestSubject", "TestText", "dateTest");
        Task st = new SendTask("connectiontest@test.com", testMail, statusPrint, resultHandler);
        Thread tct = new Thread(ct);
        tct.start();
        Task cht = new CheckTask("checkTest@test.com", statusPrint, resultHandler);
        Thread tcht = new Thread(cht);
        Task rt = new RemoveTask("testRemove@test.com", statusPrint, "test001");
        Thread trt = new Thread(rt);
        tcht.start();
        try {
            tct.join();
        } catch (InterruptedException e) {
            System.out.println("Join fallita");
        }
        for(Email mail : mailList){
            System.out.println(mail);
        }
        Thread tst = new Thread(st);
        tst.start();
        trt.start();
    }
}
