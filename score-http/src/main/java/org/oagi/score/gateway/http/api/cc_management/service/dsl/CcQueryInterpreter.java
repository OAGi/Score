package org.oagi.score.gateway.http.api.cc_management.service.dsl;


import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocumentImpl;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.api.cc_management.service.dsl.DSLInterpreter.parseExpression;

@Service
public class CcQueryInterpreter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private OllamaApi ollamaApi;

    private String modelName = "gemma3:12b";

    public List<AsccpManifestId> interpret(ScoreUser requester, ReleaseId releaseId, String statement) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        String systemPrompt = "You are an expert DSL translator. Your task is to translate natural language queries into a specialized domain-specific language \n" +
                "(DSL). This DSL is used to find records that contain specific strings.\n" +
                "\n" +
                "**DSL Definition:**\n" +
                "\n" +
                "<expression> ::= <expression-term> \n" +
                "              | <expression-term> <operator> <expression>\n" +
                "\n" +
                "<expression-term> ::= \"contains\" <expression-content>\n" +
                "                   | \"not\" \"contains\" <expression-content>\n" +
                "                   | <name-expression>\n" +
                "                   | <count-expression>\n" +
                "                   | <child-of-expression>\n" +
                "\n" +
                "<expression-content> ::= <keyword> \n" +
                "                     | <keyword> <operator> <expression-content>\n" +
                "\n" +
                "<operator> ::= \"or\"\n" +
                "           | \"and\"\n" +
                "\n" +
                "<keyword> ::= <quoted-string>\n" +
                "\n" +
                "<quoted-string> ::= \"\\\"\" <string> \"\\\"\"\n" +
                "\n" +
                "<string> ::= <character>*\n" +
                "\n" +
                "<character> ::= any character except for the quote symbol (`\"`), e.g., letters, digits, whitespace, etc.\n" +
                "\n" +
                "<name-expression> ::= \"name(\"<keyword>\")\"\n" +
                "\n" +
                "<count-expression> ::= \"count(children)\" <comparison-operator> <number>\n" +
                "\n" +
                "<comparison-operator> ::= \"=\" | \"<\" | \"<=\" | \">\" | \">=\" | \"!=\"\n" +
                "\n" +
                "<number> ::= a sequence of digits (e.g., 0, 1, 23, 100)\n" +
                "\n" +
                "<child-of-expression> ::= \"child-of(\"<keyword>\")\"\n" +
                "\n" +
                "**Example Translations:**\n" +
                "\n" +
                "*   **Natural Language:** Find records that contains \"City\"\n" +
                "    **DSL:** contains(\"City\")\n" +
                "*   **Natural Language:** Find records that contains \"City\" or \"State\"\n" +
                "    **DSL:** contains(\"City\" or \"State\")\n" +
                "*   **Natural Language:** Find records that contains \"City\" and \"State\"\n" +
                "    **DSL:** contains(\"City\" and \"State\")\n" +
                "*   **Natural Language:** Find records that not contains \"Given Name\" and \"Family Name\"\n" +
                "    **DSL:** not contains(\"Given Name\" and \"Family Name\")\n" +
                "*   **Natural Language:** Find records that have exactly 3 children\n" +
                "    **DSL:** count(children) = 3\n" +
                "*   **Natural Language:** Find records with more than 1 child\n" +
                "    **DSL:** count(children) > 1\n" +
                "*   **Natural Language:** Find records with fewer than 2 children\n" +
                "    **DSL:** count(children) < 2\n" +
                "*   **Natural Language:** Find records with no children\n" +
                "    **DSL:** count(children) = 0\n" +
                "*   **Natural Language:** Find records with at least 5 children\n" +
                "    **DSL:** count(children) >= 5\n" +
                "*   **Natural Language:** Find records with a number of children not equal to 4\n" +
                "    **DSL:** count(children) != 4\n" +
                "*   **Natural Language:** Find records with more than 20 children and contain \"Given Name\" and \"Family Name\"\n" +
                "    **DSL:** count(children) > 20 and contains(\"Given Name\" and \"Family Name\")\n" +
                "*   **Natural Language:** Find records its name or term is \"BOM\"\n" +
                "    **DSL:** name(\"BOM\")\n" +
                "*   **Natural Language:** Find children of \"BOM\"\n" +
                "    **DSL:** child-of(\"BOM\")\n" +
                "\n" +
                "**Your Task:**\n" +
                "\n" +
                "Translate the following natural language query into its DSL equivalent.  Provide *only* the DSL translation.  Do not include any \n" +
                "explanatory text. If the query cannot be translated into DSL, return 'NONE'.";

        var request = OllamaApi.ChatRequest.builder(modelName)
                .stream(false) // not streaming
                .messages(List.of(
                        OllamaApi.Message.builder(OllamaApi.Message.Role.SYSTEM)
                                .content(systemPrompt)
                                .build(),
                        OllamaApi.Message.builder(OllamaApi.Message.Role.USER)
                                .content(statement)
                                .build()))
                .options(OllamaOptions.builder().temperature(1.0).build())
                .build();
        OllamaApi.ChatResponse response;
        try {
            response = ollamaApi.chat(request);
        } catch (Exception e) {
            logger.error("Error occurs while the request for translating DSL for CCs is processing.", e);
            return Collections.emptyList();
        }
        String dslStatement = response.message().content();
        if ("NONE".equals(dslStatement)) {
            return Collections.emptyList();
        }

        Predicate<DSLRecord> predicate;
        try {
            predicate = parseExpression(dslStatement);
        } catch (Exception e) {
            logger.error("An error occurred during parsing the statement: " + dslStatement, e);
            return Collections.emptyList();
        }

        CcDocument ccDocument = new CcDocumentImpl(requester, repositoryFactory, releaseId);

        List<AsccpDSLRecord> recordList = ccDocument.getAsccpList().stream()
                .map(asccp -> new AsccpDSLRecord(ccDocument, asccp))
                .collect(Collectors.toList());

        List<AsccpDSLRecord> records = recordList.stream()
                .filter(e -> predicate.test(e))
                .limit(100)
                .collect(Collectors.toList());
        return records.stream().map(e -> e.asccp.asccpManifestId()).collect(Collectors.toList());
    }

    public static class AsccpDSLRecord implements DSLRecord {

        private final CcDocument ccDocument;
        private final AsccpSummaryRecord asccp;

        public AsccpDSLRecord(CcDocument ccDocument, AsccpSummaryRecord asccp) {
            this.ccDocument = ccDocument;
            this.asccp = asccp;
        }

        @Override
        public boolean contains(String keyword) {
            Stack<AccSummaryRecord> accStack = new Stack<>();
            AccSummaryRecord acc = ccDocument.getAcc(this.asccp.roleOfAccManifestId());
            while (acc != null) {
                accStack.push(acc);
                acc = ccDocument.getAcc(acc.basedAccManifestId());
            }

            while (!accStack.isEmpty()) {
                acc = accStack.pop();

                for (BccSummaryRecord bcc : ccDocument.getBccListByFromAccManifestId(acc.accManifestId())) {
                    BccpSummaryRecord toBccp = ccDocument.getBccp(bcc.toBccpManifestId());
                    if (toBccp.propertyTerm().contains(keyword)) {
                        return true;
                    }
                }

                for (AsccSummaryRecord ascc : ccDocument.getAsccListByFromAccManifestId(acc.accManifestId())) {
                    AsccpSummaryRecord toAsccp = ccDocument.getAsccp(ascc.toAsccpManifestId());
                    if (ccDocument.getAcc(toAsccp.roleOfAccManifestId()).isGroup()) {
                        if (new AsccpDSLRecord(ccDocument, toAsccp).contains(keyword)) {
                            return true;
                        }
                    } else {
                        if (toAsccp.propertyTerm().contains(keyword)) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }

        @Override
        public boolean isChildOf(String keyword) {
            return false;
        }

        @Override
        public int getChildrenCount() {
            int count = 0;
            Stack<AccSummaryRecord> accStack = new Stack<>();
            AccSummaryRecord acc = ccDocument.getAcc(this.asccp.roleOfAccManifestId());
            while (acc != null) {
                accStack.push(acc);
                acc = ccDocument.getAcc(acc.basedAccManifestId());
            }

            while (!accStack.isEmpty()) {
                acc = accStack.pop();

                count += ccDocument.getBccListByFromAccManifestId(acc.accManifestId()).size();

                for (AsccSummaryRecord ascc : ccDocument.getAsccListByFromAccManifestId(acc.accManifestId())) {
                    AsccpSummaryRecord toAsccp = ccDocument.getAsccp(ascc.toAsccpManifestId());
                    if (ccDocument.getAcc(toAsccp.roleOfAccManifestId()).isGroup()) {
                        count += new AsccpDSLRecord(ccDocument, toAsccp).getChildrenCount();
                    } else {
                        count += 1;
                    }
                }
            }

            return count;
        }

        @Override
        public String name() {
            return this.asccp.propertyTerm();
        }

        public String toString() {
            return this.asccp.asccpManifestId() + " " + this.asccp.propertyTerm();
        }
    }

}
