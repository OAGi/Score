package org.oagi.score.gateway.http.api.integration_management.github.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.oagi.score.gateway.http.api.integration_management.github.config.GitHubIntegrationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.util.StringUtils.hasText;

/**
 * The default {@link GitHubApiClient}: talks to GitHub's REST API (issues, comments, repo/org access)
 * and its GraphQL API (the Projects v2 board) over a dedicated, fast-failing {@link RestTemplate}.
 * Issue #1533. This is the ONLY class in the integration that builds an HTTP request, a GraphQL query,
 * or knows a node id — so the transport can change here without touching the service.
 */
@Component
public class GitHubApiClientImpl implements GitHubApiClient {

    private static final MediaType GITHUB_JSON = MediaType.parseMediaType("application/vnd.github+json");

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private GitHubIntegrationProperties properties;

    @Autowired
    @Qualifier("gitHubRestTemplate")
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public OAuthToken exchangeOAuthCode(String code, String state) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());
        form.add("code", code);
        form.add("redirect_uri", properties.getRedirectUri());
        form.add("state", state);

        Map<?, ?> tokenResp;
        try {
            tokenResp = restTemplate.postForObject(properties.getTokenUri(),
                    new HttpEntity<>(form, headers), Map.class);
        } catch (Exception e) {
            return null;
        }
        if (tokenResp == null || tokenResp.get("access_token") == null) {
            return null;
        }
        Object scope = tokenResp.get("scope");
        return new OAuthToken(tokenResp.get("access_token").toString(),
                (scope == null) ? null : scope.toString());
    }

    @Override
    public String fetchLogin(String accessToken) {
        try {
            ResponseEntity<Map> resp = restTemplate.exchange(properties.getApiBaseUrl() + "/user",
                    HttpMethod.GET, new HttpEntity<>(bearerHeaders(accessToken)), Map.class);
            Object login = (resp.getBody() == null) ? null : resp.getBody().get("login");
            return (login == null) ? null : login.toString();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public FetchedIssue fetchIssue(String token, String owner, String repo, int number) {
        try {
            ResponseEntity<JsonNode> resp = restTemplate.exchange(issueUrl(owner, repo, number),
                    HttpMethod.GET, new HttpEntity<>(bearerHeaders(token)), JsonNode.class);
            return new FetchedIssue(resp.getBody(), resp.getHeaders().getETag());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public ConditionalIssue fetchIssueConditional(String token, String owner, String repo, int number, String etag) {
        try {
            HttpHeaders headers = bearerHeaders(token);
            if (etag != null) {
                headers.setIfNoneMatch(etag);
            }
            ResponseEntity<byte[]> resp = restTemplate.exchange(issueUrl(owner, repo, number),
                    HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
            if (resp.getStatusCode().value() == HttpStatus.NOT_MODIFIED.value()) {
                return ConditionalIssue.notModified();
            }
            byte[] body = resp.getBody();
            if (body == null || body.length == 0) {
                return ConditionalIssue.unavailable();
            }
            JsonNode issue = objectMapper.readTree(body);
            return ConditionalIssue.modified(issue, resp.getHeaders().getETag());
        } catch (HttpStatusCodeException e) {
            // Some clients surface 304 as an exception; treat it as not-modified, everything else as a miss.
            return (e.getStatusCode().value() == HttpStatus.NOT_MODIFIED.value())
                    ? ConditionalIssue.notModified() : ConditionalIssue.unavailable();
        } catch (Exception e) {
            return ConditionalIssue.unavailable();
        }
    }

    @Override
    public String postIssueComment(String token, String owner, String repo, int number, String body) {
        try {
            HttpHeaders headers = bearerHeaders(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<JsonNode> resp = restTemplate.exchange(issueUrl(owner, repo, number) + "/comments",
                    HttpMethod.POST, new HttpEntity<>(objectMapper.createObjectNode().put("body", body), headers),
                    JsonNode.class);
            JsonNode created = resp.getBody();
            return (created != null && created.hasNonNull("html_url")) ? created.get("html_url").asText() : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean isRepoAccessible(String token, String owner, String repo) {
        if (!hasText(token) || !hasText(owner) || !hasText(repo)) {
            return false;
        }
        try {
            ResponseEntity<Void> resp = restTemplate.exchange(
                    properties.getApiBaseUrl() + "/repos/" + owner + "/" + repo,
                    HttpMethod.GET, new HttpEntity<>(bearerHeaders(token)), Void.class);
            return resp.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Boolean orgMembershipActive(String token, String org) {
        if (!hasText(token) || !hasText(org)) {
            return null;
        }
        try {
            ResponseEntity<JsonNode> resp = restTemplate.exchange(
                    properties.getApiBaseUrl() + "/user/memberships/orgs/" + org,
                    HttpMethod.GET, new HttpEntity<>(bearerHeaders(token)), JsonNode.class);
            JsonNode body = resp.getBody();
            return body != null && "active".equals(body.path("state").asText(null));
        } catch (HttpStatusCodeException e) {
            return (e.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) ? Boolean.FALSE : null;
        } catch (Exception e) {
            return null;
        }
    }

    // ----------------------------------------------------------------------------------------------
    // Projects v2 board. GitHub gained a (partial) REST surface for Projects v2 in 2025-09 (GA), but
    // this client still speaks GraphQL for the board: mapping an issue to its card on a given board is
    // far cheaper through `issue.projectItems` (issue -> its few memberships) than paging the whole
    // board's items over REST, and there is no REST positive "can this token write the board?" probe.
    // Swapping the transport (e.g. to the REST Projects API) is isolated to THIS class — the service
    // talks only to the GitHubApiClient port and never sees a query or a node id.
    // ----------------------------------------------------------------------------------------------

    @Override
    public boolean isProjectReadable(String token, String ownerType, String owner, int number) {
        String root = "user".equalsIgnoreCase(ownerType) ? "user" : "organization";
        String query = "query($owner:String!,$number:Int!){"
                + root + "(login:$owner){projectV2(number:$number){id}}}";
        ObjectNode vars = objectMapper.createObjectNode();
        vars.put("owner", owner);
        vars.put("number", number);
        JsonNode data = graphql(token, query, vars);
        return data != null && hasText(data.path(root).path("projectV2").path("id").asText(null));
    }

    @Override
    public ProjectData fetchProject(String token, String ownerType, String owner, int number) {
        // The project's owner is an organization or a user; the GraphQL roots differ.
        String root = "user".equalsIgnoreCase(ownerType) ? "user" : "organization";
        String query = "query($owner:String!,$number:Int!){"
                + root + "(login:$owner){projectV2(number:$number){id title "
                + "fields(first:50){nodes{... on ProjectV2SingleSelectField{id name options{id name color}}}}}}}";
        ObjectNode vars = objectMapper.createObjectNode();
        vars.put("owner", owner);
        vars.put("number", number);

        JsonNode data = graphql(token, query, vars);
        if (data == null) {
            return null;
        }
        JsonNode project = data.path(root).path("projectV2");
        String projectNodeId = project.path("id").asText(null);
        if (!hasText(projectNodeId)) {
            return null;
        }
        String projectTitle = project.path("title").asText(null);
        List<SingleSelectField> fields = new ArrayList<>();
        for (JsonNode field : project.path("fields").path("nodes")) {
            JsonNode options = field.path("options");
            // Skip nodes that are not single-select fields (no options array in the GraphQL projection).
            if (!options.isArray()) {
                continue;
            }
            List<SingleSelectOption> optionList = new ArrayList<>();
            for (JsonNode option : options) {
                optionList.add(new SingleSelectOption(option.path("id").asText(null),
                        option.path("name").asText(null), option.path("color").asText(null)));
            }
            fields.add(new SingleSelectField(field.path("id").asText(null),
                    field.path("name").asText(null), optionList));
        }
        return new ProjectData(projectNodeId, projectTitle, fields);
    }

    @Override
    public String resolveIssueNodeId(String token, String owner, String repo, int number) {
        String query = "query($owner:String!,$name:String!,$number:Int!){"
                + "repository(owner:$owner,name:$name){issue(number:$number){id}}}";
        ObjectNode vars = objectMapper.createObjectNode();
        vars.put("owner", owner);
        vars.put("name", repo);
        vars.put("number", number);
        JsonNode data = graphql(token, query, vars);
        if (data == null) {
            return null;
        }
        String id = data.path("repository").path("issue").path("id").asText(null);
        return hasText(id) ? id : null;
    }

    @Override
    public ProjectItem findIssueProjectItem(String token, String issueNodeId, String projectNodeId, String fieldName) {
        // An issue can belong to many project boards, so page through them (GitHub caps `first` at 100):
        // a card on our board beyond the first page must still be found, otherwise the caller would re-add
        // it and read a null current option, defeating the gate-option anti-clobber guard. The page loop
        // is bounded so a pathological issue stays cheap.
        String query = "query($issueId:ID!,$field:String!,$after:String){node(id:$issueId){... on Issue{"
                + "projectItems(first:100,after:$after){pageInfo{hasNextPage endCursor}nodes{id project{id} "
                + "fieldValueByName(name:$field){... on ProjectV2ItemFieldSingleSelectValue{name}}}}}}}";
        String after = null;
        for (int page = 0; page < 20; page++) {
            ObjectNode vars = objectMapper.createObjectNode();
            vars.put("issueId", issueNodeId);
            vars.put("field", fieldName);
            if (after == null) {
                vars.putNull("after");
            } else {
                vars.put("after", after);
            }
            JsonNode data = graphql(token, query, vars);
            if (data == null) {
                return null;
            }
            JsonNode projectItems = data.path("node").path("projectItems");
            for (JsonNode itemNode : projectItems.path("nodes")) {
                if (projectNodeId.equals(itemNode.path("project").path("id").asText(null))) {
                    String itemId = itemNode.path("id").asText(null);
                    String currentFieldOption = itemNode.path("fieldValueByName").path("name").asText(null);
                    return new ProjectItem(itemId, currentFieldOption);
                }
            }
            JsonNode pageInfo = projectItems.path("pageInfo");
            if (!pageInfo.path("hasNextPage").asBoolean(false)) {
                return null;
            }
            after = pageInfo.path("endCursor").asText(null);
            if (!hasText(after)) {
                return null;
            }
        }
        return null;
    }

    @Override
    public String addProjectItem(String token, String projectNodeId, String issueNodeId) {
        String mutation = "mutation($projectId:ID!,$contentId:ID!){"
                + "addProjectV2ItemById(input:{projectId:$projectId,contentId:$contentId}){item{id}}}";
        ObjectNode vars = objectMapper.createObjectNode();
        vars.put("projectId", projectNodeId);
        vars.put("contentId", issueNodeId);
        JsonNode data = graphql(token, mutation, vars);
        if (data == null) {
            return null;
        }
        String id = data.path("addProjectV2ItemById").path("item").path("id").asText(null);
        return hasText(id) ? id : null;
    }

    @Override
    public boolean setProjectItemFieldOption(String token, String projectNodeId, String itemId,
                                             String fieldId, String optionId) {
        String mutation = "mutation($projectId:ID!,$itemId:ID!,$fieldId:ID!,$optionId:String!){"
                + "updateProjectV2ItemFieldValue(input:{projectId:$projectId,itemId:$itemId,fieldId:$fieldId,"
                + "value:{singleSelectOptionId:$optionId}}){projectV2Item{id}}}";
        ObjectNode vars = objectMapper.createObjectNode();
        vars.put("projectId", projectNodeId);
        vars.put("itemId", itemId);
        vars.put("fieldId", fieldId);
        vars.put("optionId", optionId);
        JsonNode data = graphql(token, mutation, vars);
        if (data == null) {
            return false;
        }
        return hasText(data.path("updateProjectV2ItemFieldValue").path("projectV2Item").path("id").asText(null));
    }

    @Override
    public boolean deleteProjectItem(String token, String projectNodeId, String itemId) {
        String mutation = "mutation($projectId:ID!,$itemId:ID!){"
                + "deleteProjectV2Item(input:{projectId:$projectId,itemId:$itemId}){deletedItemId}}";
        ObjectNode vars = objectMapper.createObjectNode();
        vars.put("projectId", projectNodeId);
        vars.put("itemId", itemId);
        JsonNode data = graphql(token, mutation, vars);
        if (data == null) {
            return false;
        }
        return hasText(data.path("deleteProjectV2Item").path("deletedItemId").asText(null));
    }

    /**
     * Single GraphQL entry point: POSTs {@code {query, variables}} to {@code <api-base>/graphql} with
     * the user's bearer token and returns the {@code data} node, or {@code null} on a transport error
     * or a non-empty {@code errors} array (logged). Never throws.
     */
    private JsonNode graphql(String token, String query, ObjectNode variables) {
        try {
            HttpHeaders headers = bearerHeaders(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("query", query);
            payload.set("variables", (variables == null) ? objectMapper.createObjectNode() : variables);
            ResponseEntity<JsonNode> resp = restTemplate.exchange(properties.getApiBaseUrl() + "/graphql",
                    HttpMethod.POST, new HttpEntity<>(payload, headers), JsonNode.class);
            JsonNode body = resp.getBody();
            if (body == null) {
                return null;
            }
            JsonNode errors = body.get("errors");
            if (errors != null && errors.isArray() && !errors.isEmpty()) {
                logger.warn("GitHub GraphQL returned errors: {}", errors);
                return null;
            }
            return body.get("data");
        } catch (Exception e) {
            return null;
        }
    }

    private HttpHeaders bearerHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(List.of(GITHUB_JSON));
        return headers;
    }

    private String issueUrl(String owner, String repo, int number) {
        return properties.getApiBaseUrl() + "/repos/" + owner + "/" + repo + "/issues/" + number;
    }
}
