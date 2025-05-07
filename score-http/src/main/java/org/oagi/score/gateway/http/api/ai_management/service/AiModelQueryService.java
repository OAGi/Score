package org.oagi.score.gateway.http.api.ai_management.service;

import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocumentImpl;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.hasLength;

@Service
@Transactional(readOnly = true)
public class AiModelQueryService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OllamaApi ollamaApi;

    @Autowired
    private RepositoryFactory repositoryFactory;

    public List<String> getAvailableModels() {

        OllamaApi.ListModelResponse response;
        try {
            response = ollamaApi.listModels();
        } catch (Exception e) {
            logger.error("Error occurs while the list of available models is loading.", e);
            return Collections.emptyList();
        }
        return response.models()
                .stream()
                .sorted(Comparator.comparing(OllamaApi.Model::modifiedAt).reversed())
                .map(e -> e.name()).collect(Collectors.toList());
    }

    public String generateDefinition(
            ScoreUser requester, AccManifestId accManifestId, String model, String originalText) {

        var accQuery = repositoryFactory.accQueryRepository(requester);
        var acc = accQuery.getAccSummary(accManifestId);

        CcDocument ccDocument = new CcDocumentImpl(requester, repositoryFactory, acc.release().releaseId());

        String prompt = "The object class term of the given object is '" + acc.objectClassTerm() + "'.\n" +
                prompt(accManifestId, ccDocument, 0);
        if (hasLength(originalText)) {
            prompt += "\nThe original definition is '" + originalText + "'.";
        }
        System.out.println(prompt);
        var request = OllamaApi.ChatRequest.builder(model)
                .stream(false) // not streaming
                .messages(List.of(
                        OllamaApi.Message.builder(OllamaApi.Message.Role.SYSTEM)
                                .content("Generate a concise definition of the given object in 2-4 sentences based on its description. " +
                                        "If an existing definition is provided, refine it for clarity and correctness, ensuring the best possible version. " +
                                        "Also, your generated definition must not lose the original content or manipulate its meaning. " +
                                        "The final response should contain only the improved definition, without any explanatory notes or structural details.")
                                .build(),
                        OllamaApi.Message.builder(OllamaApi.Message.Role.USER)
                                .content(prompt)
                                .build()))
                .options(OllamaOptions.builder()
                        .temperature(1.0)
                        .numCtx(64 * 1024)
                        .build())
                .build();

        var response = this.ollamaApi.chat(request);

        String content = response.message().content();
        return removeReasoning(content);
    }

    public String generateDefinition(
            ScoreUser requester, AsccpManifestId asccpManifestId, String model, String originalText) {

        var asccpQuery = repositoryFactory.asccpQueryRepository(requester);
        var asccp = asccpQuery.getAsccpSummary(asccpManifestId);

        return generateDefinition(requester, asccp.roleOfAccManifestId(), model, originalText);
    }

    private String prompt(AccManifestId accManifestId, CcDocument ccDocument, int depth) {
        var acc = ccDocument.getAcc(accManifestId);
        StringBuilder sb = new StringBuilder();
        AccSummaryRecord basedAcc = null;
        if (acc.basedAccManifestId() != null) {
            basedAcc = ccDocument.getAcc(acc.basedAccManifestId());
            sb.append("'" + acc.objectClassTerm() + "' is derived from the base object '" + basedAcc.objectClassTerm() + "'.")
                    .append("\n");
            if (basedAcc.definition() != null && hasLength(basedAcc.definition().content())) {
                sb.append("The definition of the base object '" + basedAcc.objectClassTerm() + "' is '" + basedAcc.definition().content() + "'.").append("\n");
                if (hasLength(basedAcc.definition().source())) {
                    sb.append("The source of the base object's definition is '" + basedAcc.definition().source() + "'.").append("\n");
                }
            }
        }
        var asccList = ccDocument.getAsccListByFromAccManifestId(accManifestId).stream()
                .filter(e -> !e.den().contains("Extension. ")).collect(Collectors.toList());
        var bccList = ccDocument.getBccListByFromAccManifestId(accManifestId);
        if (asccList.size() + bccList.size() > 0) {
            sb.append("Additionally, it has child elements, including ");
            List<String> childrenNames = new ArrayList<>();
            for (var ascc : asccList) {
                var asccp = ccDocument.getAsccp(ascc.toAsccpManifestId());
                childrenNames.add("'" + asccp.propertyTerm() + "' " +
                        "(Cardinality: " + ascc.cardinality().min() + ".." +
                        (ascc.cardinality().max() == -1 ? "unbounded" : ascc.cardinality().max()) + ")");
            }
            for (var bcc : bccList) {
                var bccp = ccDocument.getBccp(bcc.toBccpManifestId());
                childrenNames.add("'" + bccp.propertyTerm() + "' " +
                        "(Cardinality: " + bcc.cardinality().min() + ".." +
                        (bcc.cardinality().max() == -1 ? "unbounded" : bcc.cardinality().max()) + ")");
            }
            sb.append(childrenNames.stream().collect(Collectors.joining(", ")));
            sb.append(".").append("\n");
        }
        if (basedAcc != null) {
            sb.append(prompt(acc.basedAccManifestId(), ccDocument, depth));
        }
        return sb.toString();
    }

    private String removeReasoning(String content) {
        if (content != null && content.startsWith("<think>")) {
            int endIndex = content.indexOf("</think>");
            if (endIndex != -1) {
                return content.substring(endIndex + "</think>".length()).trim();
            }
        }
        return content;
    }

    public String suggestName(ScoreUser requester, AccManifestId accManifestId, String model, String originalName) {

        var accQuery = repositoryFactory.accQueryRepository(requester);
        var acc = accQuery.getAccSummary(accManifestId);

        CcDocument ccDocument = new CcDocumentImpl(requester, repositoryFactory, acc.release().releaseId());

        String prompt = "The original object class term of the given object is '" + originalName + "'.\n" +
                prompt(accManifestId, ccDocument, 0);
        System.out.println(prompt);
        var request = OllamaApi.ChatRequest.builder(model)
                .stream(false) // not streaming
                .messages(List.of(
                        OllamaApi.Message.builder(OllamaApi.Message.Role.SYSTEM)
                                .content("Generate a name that describe the given object in word(s) based on its description. " +
                                        "If the original name is perfectly describing the object, do not change it. " +
                                        "Each word must start with the capital letter and must be space separated with only alpha-numeric characters. " +
                                        "Use a smallest number of words if possible without any articles. " +
                                        "The final response should contain only the word(s) without any explanatory notes or structural details.")
                                .build(),
                        OllamaApi.Message.builder(OllamaApi.Message.Role.USER)
                                .content(prompt)
                                .build()))
                .options(OllamaOptions.builder()
                        .temperature(1.0)
                        .numCtx(64 * 1024)
                        .build())
                .build();

        var response = this.ollamaApi.chat(request);

        String content = response.message().content();
        return removeReasoning(content);
    }
}
