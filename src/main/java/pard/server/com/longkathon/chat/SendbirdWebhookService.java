package pard.server.com.longkathon.chat;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class SendbirdWebhookService {

    // ✅ 토이 프로젝트: ObjectMapper는 그냥 new로 두어도 충분
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final WebhookInboxRepo inboxRepo;
    private final ChatMessageRepo chatMessageRepo;

    @Value("${sendbird.master-api-token:}")
    private String masterApiToken;

    // ✅ 서명검증 ON/OFF 토글 (기본 false)
    @Value("${sendbird.webhook.verify-signature:false}")
    private boolean verifySignatureEnabled;

    /**
     * 1) (옵션) 서명 검증
     * 2) inbox에 원본 저장(멱등)
     * 3) 메시지 이벤트면 chat_message 반영
     */
    @Transactional
    public void handle(String rawBody, String signatureHeader) {

        if (verifySignatureEnabled) {
            verifySignature(rawBody, signatureHeader);
        }

        JsonNode root = parse(rawBody);

        String category = text(root, "category");
        String dedupKey = buildDedupKey(category, root);

        // ✅ 멱등 처리: 같은 이벤트 중복 저장 방지
        if (inboxRepo.existsByDedupKey(dedupKey)) {
            return;
        }

        WebhookInbox inbox = inboxRepo.save(WebhookInbox.builder()
                .category(category)
                .dedupKey(dedupKey)
                .signature(signatureHeader)
                .payloadRaw(rawBody)
                .status(WebhookInbox.Status.RECEIVED)
                .build());

        try {
            if (category != null && category.contains("message_")) {
                upsertMessageByCategory(category, root);
            }
            inbox.markProcessed();
        } catch (Exception e) {
            inbox.markFailed(e.getMessage());
            // 토이 기준: 웹훅 응답은 200 유지(throw 안 함)
        }
    }

    /**
     * ✅ 핵심 수정 포인트
     * - Sendbird 웹훅에서 message 관련 정보가 root.message가 아니라 root.payload에 들어오는 케이스 대응
     */
    private void upsertMessageByCategory(String category, JsonNode root) {

        JsonNode msgNode = messageNode(root); // ✅ payload 우선
        JsonNode channelNode = root.path("channel");

        String channelUrl = text(channelNode, "channel_url");
        Long messageId = extractMessageId(root);

        // message_id가 없으면 chat_message upsert를 할 수 없으니 스킵
        if (channelUrl == null || messageId == null) {
            return;
        }

        // type은 root에 있을 수도, payload에 있을 수도
        String messageType = text(msgNode, "type");
        if (messageType == null) messageType = text(root, "type");

        // sender는 케이스가 다양해서 fallback
        String senderId = extractSenderId(root, msgNode);

        if (category.endsWith("message_send")) {
            ChatMessage entity = ChatMessage.builder()
                    .channelUrl(channelUrl)
                    .messageId(messageId)
                    .messageType(messageType)
                    .senderSendbirdUserId(senderId)
                    .message(text(msgNode, "message"))
                    .customType(text(msgNode, "custom_type"))
                    .data(text(msgNode, "data"))
                    .sentAt(longVal(msgNode, "created_at"))   // payload에 있을 가능성 큼
                    .deleted(false)
                    .updatedAt(longVal(msgNode, "updated_at"))
                    .build();

            chatMessageRepo.findByChannelUrlAndMessageId(channelUrl, messageId)
                    .ifPresentOrElse(
                            exist -> exist.updateFromWebhook(
                                    entity.getMessage(),
                                    entity.getCustomType(),
                                    entity.getData(),
                                    entity.getUpdatedAt()
                            ),
                            () -> chatMessageRepo.save(entity)
                    );
            return;
        }

        if (category.endsWith("message_update")) {
            chatMessageRepo.findByChannelUrlAndMessageId(channelUrl, messageId)
                    .ifPresent(exist -> exist.updateFromWebhook(
                            text(msgNode, "message"),
                            text(msgNode, "custom_type"),
                            text(msgNode, "data"),
                            longVal(msgNode, "updated_at")
                    ));
            return;
        }

        if (category.endsWith("message_delete")) {
            chatMessageRepo.findByChannelUrlAndMessageId(channelUrl, messageId)
                    .ifPresent(exist -> exist.markDeleted(longVal(msgNode, "updated_at")));
        }
    }

    /**
     * ✅ payload 우선으로 message node를 선택
     */
    private JsonNode messageNode(JsonNode root) {
        if (root.hasNonNull("payload")) return root.path("payload");
        if (root.hasNonNull("message")) return root.path("message");
        return root;
    }

    /**
     * ✅ message_id는 너가 확인한 대로 $.payload.message_id 에 존재
     * - 그래도 혹시 모를 케이스를 위해 fallback을 조금 둠
     */
    private Long extractMessageId(JsonNode root) {
        Long v;

        // 1) payload.message_id (현재 네 케이스)
        v = longVal(root.path("payload"), "message_id");
        if (v != null) return v;

        // 2) message.message_id (다른 webhook 구조 대비)
        v = longVal(root.path("message"), "message_id");
        if (v != null) return v;

        // 3) top-level message_id (혹시)
        v = longVal(root, "message_id");
        if (v != null) return v;

        return null;
    }

    private String extractSenderId(JsonNode root, JsonNode msgNode) {
        // msgNode.user.user_id
        String v = text(msgNode.path("user"), "user_id");
        if (v != null) return v;

        // root.sender.user_id (혹시)
        v = text(root.path("sender"), "user_id");
        if (v != null) return v;

        // root.user.user_id (혹시)
        v = text(root.path("user"), "user_id");
        return v;
    }

    private String buildDedupKey(String category, JsonNode root) {
        if (category == null) category = "unknown";

        String channelUrl = text(root.path("channel"), "channel_url");
        Long messageId = extractMessageId(root);

        if (channelUrl != null && messageId != null) {
            return category + "|" + channelUrl + "|" + messageId;
        }

        Long ts = longVal(root, "ts");
        if (ts == null) ts = System.currentTimeMillis();
        return category + "|" + ts;
    }

    private JsonNode parse(String raw) {
        try {
            return objectMapper.readTree(raw);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON payload");
        }
    }

    private void verifySignature(String rawBody, String signatureHeader) {
        if (signatureHeader == null || signatureHeader.isBlank()) {
            throw new IllegalArgumentException("Missing x-sendbird-signature");
        }
        if (masterApiToken == null || masterApiToken.isBlank()) {
            throw new IllegalArgumentException("Missing sendbird.master-api-token");
        }

        String expected = hmacSha256Hex(masterApiToken, rawBody);
        if (!constantTimeEquals(expected, signatureHeader)) {
            throw new IllegalArgumentException("Invalid signature");
        }
    }

    private String hmacSha256Hex(String secret, String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new RuntimeException("HMAC failure", e);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        byte[] x = a.getBytes(StandardCharsets.UTF_8);
        byte[] y = b.getBytes(StandardCharsets.UTF_8);
        if (x.length != y.length) return false;
        int r = 0;
        for (int i = 0; i < x.length; i++) r |= x[i] ^ y[i];
        return r == 0;
    }

    private String text(JsonNode node, String field) {
        if (node == null) return null;
        JsonNode v = node.get(field);
        return (v == null || v.isNull()) ? null : v.asText();
    }

    private Long longVal(JsonNode node, String field) {
        if (node == null) return null;
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) return null;
        try {
            return v.asLong();
        } catch (Exception e) {
            return null;
        }
    }
}
