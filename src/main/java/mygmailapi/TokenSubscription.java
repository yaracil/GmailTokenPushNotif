/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygmailapi;

/*
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import com.google.api.gax.rpc.ApiException;
import com.google.api.services.gmail.Gmail;
import com.google.cloud.Identity;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.iam.v1.Binding;
import com.google.iam.v1.Policy;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.Topic;
import java.io.IOException;
import com.google.api.services.gmail.model.WatchRequest;
import com.google.api.services.gmail.model.WatchResponse;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class TokenSubscription {

    private static final String user = "me";
    WatchRequest reqst;

    private Gmail service;
    private String projectId;
    private String idLabel_TokenMexitel;

    private String topicId;
    private String subscriptionId;

    public TokenSubscription(Gmail service, String PROJECT_ID, String topicId, String subscriptionId, String idLabel_TokenMexitel) {
        this.service = service;
        this.projectId = PROJECT_ID;
        this.idLabel_TokenMexitel = idLabel_TokenMexitel;
        this.topicId = topicId;
        this.subscriptionId = subscriptionId;

    }

    private BigInteger enablingPushNotif() throws GeneralSecurityException, IOException {
        System.out.println("Enabling push notifications...");
        reqst = new WatchRequest();
        reqst.setTopicName("projects/" + projectId + "/topics/" + topicId);
        reqst.setLabelIds(Collections.singletonList(idLabel_TokenMexitel));
//        reqst.setLabelFilterAction("include");
        WatchResponse resp = service.users().watch(user, reqst).execute();
        BigInteger lastHist = resp.getHistoryId();
        return lastHist;
    }

    public void tearDown() throws Exception {
        deleteTestSubscription();
        deleteTestTopic();
    }

    public BigInteger setUpSuscriberNotif(boolean secundary) throws Exception {
        System.out.println("Project ID: " + projectId);
        if (!secundary) {
            createTopic();
            createPullSubscriptionExample();
            replaceTopicPolicy();
        }
        Thread.sleep(50000);
        return enablingPushNotif();
    }

    private void deleteTestTopic() throws Exception {
        try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
            topicAdminClient.deleteTopic(ProjectTopicName.of(projectId, topicId));
        } catch (IOException e) {
            System.err.println("Error deleting topic " + e.getMessage());
        }
    }

    private void deleteTestSubscription() throws Exception {
        try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create()) {
            subscriptionAdminClient.deleteSubscription(
                    ProjectSubscriptionName.of(projectId, subscriptionId));
        } catch (IOException e) {
            System.err.println("Error deleting subscription " + e.getMessage());
        }
    }

    /**
     * Example of creating a topic.
     */
    private Topic createTopic() throws Exception {
        // [START pubsub_create_topic]
        try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
            // projectId <=  unique project identifier, eg. "my-project-id"
            // topicId <= "my-topic-id"
            ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId);
            Topic topic = topicAdminClient.createTopic(topicName);
            return topic;
        }
        // [END pubsub_create_topic]
    }

    private void createPullSubscriptionExample() throws Exception {

        ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId);

        // Create a new subscription
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(
                projectId, subscriptionId);
        try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create()) {
            // create a pull subscription with default acknowledgement deadline (= 10 seconds)
            Subscription subscription
                    = subscriptionAdminClient.createSubscription(
                            subscriptionName, topicName, PushConfig.getDefaultInstance(), 0);
        } catch (ApiException e) {
            // example : code = ALREADY_EXISTS(409) implies subscription already exists
            System.out.print(e.getStatusCode().getCode());
            System.out.print(e.isRetryable());
        }

        System.out.printf(
                "Subscription %s:%s created.\n",
                subscriptionName.getProject(), subscriptionName.getSubscription());
    }

    private ProjectTopicName deleteTopic() throws Exception {
        // [START pubsub_delete_topic]
        try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
            ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId);
            topicAdminClient.deleteTopic(topicName);
            return topicName;
        }
        // [END pubsub_delete_topic]
    }

    /**
     * Example of replacing a topic policy.
     */
    private Policy replaceTopicPolicy() throws Exception {
        // [START pubsub_set_topic_policy]
        try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
            String topicName = ProjectTopicName.format(projectId, topicId);
            Policy policy = topicAdminClient.getIamPolicy(topicName);
            // add role -> members binding
            Binding binding
                    = Binding.newBuilder()
                            .setRole(("roles/pubsub.publisher").toString())
                            .addMembers(Identity.serviceAccount("gmail-api-push@system.gserviceaccount.com").toString())
                            .build();
            // create updated policy
            Policy updatedPolicy = Policy.newBuilder(policy).addBindings(binding).build();
            updatedPolicy = topicAdminClient.setIamPolicy(topicName, updatedPolicy);
            return updatedPolicy;
        }
        // [END pubsub_set_topic_policy]
    }

//    /**
//     * Example of listing topics.
//     */
//    public TopicAdminClient.ListTopicsPagedResponse listTopics() throws Exception {
//        // [START pubsub_list_topics]
//        try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
//            ListTopicsRequest listTopicsRequest
//                    = ListTopicsRequest.newBuilder().setProject(ProjectName.format(projectId)).build();
//            TopicAdminClient.ListTopicsPagedResponse response = topicAdminClient.listTopics(listTopicsRequest);
//            Iterable<Topic> topics = response.iterateAll();
//            for (Topic topic : topics) {
//                // do something with the topic
//            }
//            return response;
//        }
//        // [END pubsub_list_topics]
//    }
//
//    /**
//     * Example of listing subscriptions for a topic.
//     */
//    public TopicAdminClient.ListTopicSubscriptionsPagedResponse listTopicSubscriptions()
//            throws Exception {
//        // [START pubsub_list_topic_subscriptions]
//        try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
//            ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId);
//            ListTopicSubscriptionsRequest request
//                    = ListTopicSubscriptionsRequest.newBuilder().setTopic(topicName.toString()).build();
//            TopicAdminClient.ListTopicSubscriptionsPagedResponse response
//                    = topicAdminClient.listTopicSubscriptions(request);
//            Iterable<String> subscriptionNames = response.iterateAll();
//            for (String subscriptionName : subscriptionNames) {
//                // do something with the subscription name
//            }
//            return response;
//        }
//        // [END pubsub_list_topic_subscriptions]
//    }
    /**
     * Example of deleting a topic.
     */
//    /**
//     * Example of getting a topic policy.
//     */
//    public Policy getTopicPolicy() throws Exception {
//        // [START pubsub_get_topic_policy]
//        try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
//            ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId);
//            Policy policy = topicAdminClient.getIamPolicy(topicName.toString());
//            if (policy == null) {
//                // topic iam policy was not found
//            }
//            return policy;
//        }
//        // [END pubsub_get_topic_policy]
//    }
//    /**
//     * Example of testing whether the caller has the provided permissions on a
//     * topic. Only viewer, editor or admin/owner can view results of
//     * pubsub.topics.get
//     */
//    public TestIamPermissionsResponse testTopicPermissions() throws Exception {
//        // [START pubsub_test_topic_permissions]
//        try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
//            List<String> permissions = new LinkedList<>();
//            permissions.add("pubsub.topics.get");
//            ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId);
//            TestIamPermissionsResponse testedPermissions
//                    = topicAdminClient.testIamPermissions(topicName.toString(), permissions);
//            return testedPermissions;
//        }
//        // [END pubsub_test_topic_permissions]
////    }
//
//    /**
//     * Example of getting a topic.
//     */
//    public Topic getTopic() throws Exception {
//        // [START pubsub_get_topic]
//        try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
//            ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId);
//            Topic topic = topicAdminClient.getTopic(topicName);
//            return topic;
//        }
//        // [END pubsub_get_topic]
//    }
}
