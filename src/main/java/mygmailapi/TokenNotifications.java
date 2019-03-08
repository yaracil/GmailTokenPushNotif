package mygmailapi;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.cloud.ServiceOptions;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import java.io.ByteArrayInputStream;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.History;
import com.google.api.services.gmail.model.HistoryMessageAdded;
import com.google.api.services.gmail.model.ListHistoryResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

public class TokenNotifications {

    private static final String APPLICATION_NAME = "Gmail API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart. If modifying
     * these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList("https://mail.google.com/");
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private static final String PROJECT_ID = ServiceOptions.getDefaultProjectId();

    private static final BlockingQueue<PubsubMessage> messages = new LinkedBlockingDeque<>();

    private static final String user = "me";
    private static final String idLabel_TokenMexitel = "Label_402207207227935324";
    private BigInteger currentHistoryID;
    private BigInteger lastHistoryID;
    Subscriber subscriber;

    private Gmail service;

    public TokenNotifications() throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        ListMessagesResponse rsp = service.users().messages().list(user).setMaxResults(Long.valueOf("1")).execute();
        String id = rsp.getMessages().get(0).getId();
        Message last = service.users().messages().get(user, id).execute();
        lastHistoryID = last.getHistoryId();
        subscriber = null;

        String subscriptionId = "yhacaribegmail";
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(PROJECT_ID, subscriptionId).of(PROJECT_ID, subscriptionId);

//             create a subscriber bound to the asynchronous message receiver
        subscriber
                = Subscriber.newBuilder(subscriptionName, new TokenNotifications.MessageReceiverExample()).build();

        subscriber.startAsync().awaitRunning();

    }

    public static class MessageReceiverExample implements MessageReceiver {

        @Override
        public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
            messages.offer(message);
            consumer.ack();
        }
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = TokenNotifications.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public byte[] getLastToken() throws IOException, GeneralSecurityException, InterruptedException {
        // Code
        byte[] dataPdf = null;
        //  Message ms=getMessage(service, user, APPLICATION_NAME);
        //  ms.getId();

//        ListMessagesResponse listMesgs = service.users().messages().list(user).execute();
        List<Message> messages = listMessagesWithLabels(service, user, Collections.singletonList(idLabel_TokenMexitel));
        if (messages.isEmpty()) {
            System.out.println("No se encontraron correos con token.");
        } else {
//            System.out.println("Mensajes:");
            int cantTokens = messages.size();
            Message firstMessage = messages.get(0);
            System.out.println("Total de mensajes con tokens " + cantTokens);
            System.out.println("Ultimo token ID " + firstMessage.getId());

//            System.out.println("String" + firstMessage.toPrettyString());
            dataPdf = getAttachments(service, user, firstMessage.getId());
        }
        return dataPdf;
    }

    public byte[] getNewTokenPushNotifications() throws IOException, GeneralSecurityException, InterruptedException {
        byte[] dataPdf = null;

        // Continue to listen to messages
        PubsubMessage message = messages.take();
//            String messageID = message.getMessageId();
        String aux = message.getData().toStringUtf8();
        int charatcterStart = aux.indexOf("historyId") + 11;
        currentHistoryID = BigInteger.valueOf(Long.valueOf(aux.substring(charatcterStart, aux.length() - 1)));

        String token = listHistoryTokens(service, user, lastHistoryID);

        lastHistoryID = currentHistoryID;

        if (token != null) {
            System.out.println("Mensaje id " + token);
            dataPdf = getAttachments(service, user, token);
        }

        return dataPdf;
    }

    public void stopSync() {
        if (subscriber != null) {
            subscriber.stopAsync();
        }
    }

    /**
     * Get Message with given ID.
     *
     * @param service Authorized Gmail API instance.
     * @param userId User's email address. The special value "me" can be used to
     * indicate the authenticated user.
     * @param messageId ID of Message to retrieve.
     * @return Message Retrieved Message.
     * @throws IOException
     */
    public static Message getMessage(Gmail service, String userId, String messageId)
            throws IOException {
        Message message = service.users().messages().get(userId, messageId).execute();

        System.out.println("Message snippet: " + message.getSnippet());

        return message;
    }

    /**
     * Get a Message and use it to create a MimeMessage.
     *
     * @param service Authorized Gmail API instance.
     * @param userId User's email address. The special value "me" can be used to
     * indicate the authenticated user.
     * @param messageId ID of Message to retrieve.
     * @return MimeMessage MimeMessage populated from retrieved Message.
     * @throws IOException
     * @throws MessagingException
     */
    public static MimeMessage getMimeMessage(Gmail service, String userId, String messageId)
            throws IOException, MessagingException {
        Message message = service.users().messages().get(userId, messageId).setFormat("raw").execute();

        Base64 base64Url = new Base64(true);
        byte[] emailBytes = base64Url.decodeBase64(message.getRaw());

        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session, new ByteArrayInputStream(emailBytes));

        return email;
    }

    // ...
    /**
     * Print message changes.
     *
     * @param service Authorized Gmail API instance.
     * @param userId User's email address. The special value "me" can be used to
     * indicate the authenticated user.
     * @param startHistoryId Only return Histories at or after startHistoryId.
     * @throws IOException
     */
    public String listHistoryTokens(Gmail service, String userId, BigInteger startHistoryId)
            throws IOException {
        List<History> histories = new ArrayList<History>();
        ListHistoryResponse response = service.users().history().list(userId).setStartHistoryId(lastHistoryID).setLabelId(idLabel_TokenMexitel).execute();
//        System.out.println("Respuesta history" + response);
        while (response.getHistory() != null) {
            histories.addAll(response.getHistory());
            if (response.getNextPageToken() != null) {
                String pageToken = response.getNextPageToken();
                response = service.users().history().list(userId).setPageToken(pageToken)
                        .setStartHistoryId(startHistoryId).execute();
            } else {
                break;
            }
        }
//        List<String> tokens = new LinkedList<>();
        String token = null;
        for (History history : histories) {
            List<HistoryMessageAdded> msgadd = history.getMessagesAdded();
            if (msgadd != null) {
                for (HistoryMessageAdded hstmsg : msgadd) {
                    Message msg = hstmsg.getMessage();
                    System.out.println("Mensaje info: Label: " + msg.getLabelIds()
                            + " ID:" + msg.getId());
                    token = msg.getId();
                }
            }
        }
        return token;
    }

    /**
     * Get the attachments in a given email.
     *
     * @param service Authorized Gmail API instance.
     * @param userId User's email address. The special value "me" can be used to
     * indicate the authenticated user.
     * @param messageId ID of Message containing attachment..
     * @throws IOException
     */
    public static byte[] getAttachments(Gmail service, String userId, String messageId)
            throws IOException {
        Message message = service.users().messages().get(userId, messageId).execute();
        List<MessagePart> parts = message.getPayload().getParts();
        for (MessagePart part : parts) {
            if (part.getFilename() != null && part.getFilename().length() > 0) {
                String filename = part.getFilename();
                String attId = part.getBody().getAttachmentId();
                MessagePartBody attachPart = service.users().messages().attachments().
                        get(userId, messageId, attId).execute();

                Base64 base64Url = new Base64(true);
                byte[] fileByteArray = base64Url.decodeBase64(attachPart.getData());
                return fileByteArray;
            }
        }
        return null;
    }

    /**
     * List all Messages of the user's mailbox matching the query.
     *
     * @param service Authorized Gmail API instance.
     * @param userId User's email address. The special value "me" can be used to
     * indicate the authenticated user.
     * @param query String used to filter the Messages listed.
     * @throws IOException
     */
    public static List<Message> listMessagesMatchingQuery(Gmail service, String userId,
            String query) throws IOException {
        ListMessagesResponse response = service.users().messages().list(userId).setQ(query).execute();

        List<Message> messages = new ArrayList<Message>();
        while (response.getMessages() != null) {
            messages.addAll(response.getMessages());
            if (response.getNextPageToken() != null) {
                String pageToken = response.getNextPageToken();
                response = service.users().messages().list(userId).setQ(query)
                        .setPageToken(pageToken).execute();
            } else {
                break;
            }
        }
        return messages;
    }

    /**
     * List all Messages of the user's mailbox with labelIds applied.
     *
     * @param service Authorized Gmail API instance.
     * @param userId User's email address. The special value "me" can be used to
     * indicate the authenticated user.
     * @param labelIds Only return Messages with these labelIds applied.
     * @throws IOException
     */
    public static List<Message> listMessagesWithLabels(Gmail service, String userId,
            List<String> labelIds) throws IOException {
        ListMessagesResponse response = service.users().messages().list(userId)
                .setLabelIds(labelIds).execute();

        List<Message> messages = new ArrayList<Message>();
        while (response.getMessages() != null) {
            messages.addAll(response.getMessages());
            if (response.getNextPageToken() != null) {
                String pageToken = response.getNextPageToken();
                response = service.users().messages().list(userId).setLabelIds(labelIds)
                        .setPageToken(pageToken).execute();
            } else {
                break;
            }
        }

        return messages;
    }

    // ...
}
