package org.oagi.score.gateway.http.api.external.controller;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.CcListEntryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcListTypes;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpType;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.repository.criteria.CcListFilterCriteria;
import org.oagi.score.gateway.http.api.cc_management.service.CcQueryService;
import org.oagi.score.gateway.http.api.cc_management.service.dsl.CcQueryInterpreter;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.external.model.ExternalChildComponentRecord;
import org.oagi.score.gateway.http.api.external.service.ExternalComponentsService;
import org.oagi.score.gateway.http.api.graph.model.FindUsagesRequest;
import org.oagi.score.gateway.http.api.graph.model.FindUsagesResponse;
import org.oagi.score.gateway.http.api.graph.model.Graph;
import org.oagi.score.gateway.http.api.graph.service.GraphService;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryListEntry;
import org.oagi.score.gateway.http.api.library_management.repository.criteria.LibraryListFilterCriteria;
import org.oagi.score.gateway.http.api.library_management.service.LibraryQueryService;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseListEntryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.api.release_management.repository.criteria.ReleaseListFilterCriteria;
import org.oagi.score.gateway.http.api.release_management.service.ReleaseQueryService;
import org.oagi.score.gateway.http.common.model.*;
import org.oagi.score.gateway.http.common.util.DeleteOnCloseFileSystemResource;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;
import static org.oagi.score.gateway.http.common.util.ControllerUtils.pageRequest;
import static org.oagi.score.gateway.http.common.util.Utility.separate;
import static org.springframework.util.StringUtils.hasLength;

@RestController
public class ExternalComponentsController {

        @Autowired
        private ExternalComponentsService service;

        @Autowired
        private SessionService sessionService;

        @Autowired
        private LibraryQueryService libraryQueryService;

        @Autowired
        private CcQueryInterpreter interpreter;

        @Autowired
        private CcQueryService ccQueryService;

        @Autowired
        private ReleaseQueryService releaseQueryService;

        @Autowired
        private GraphService graphService;

        @RequestMapping(value = "/ext/libraries", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
        public PageResponse<LibraryListEntry> getLibraryList(@AuthenticationPrincipal AuthenticatedPrincipal user,
                        @RequestParam(name = "type", required = false) String type,
                        @RequestParam(name = "name", required = false) String name,
                        @RequestParam(name = "organization", required = false) String organization,
                        @RequestParam(name = "description", required = false) String description,
                        @RequestParam(name = "domain", required = false) String domain,
                        @RequestParam(name = "state", required = false) String state,
                        @RequestParam(name = "updaterLoginIdList", required = false) String updaterLoginIdList,
                        @RequestParam(name = "lastUpdatedOn", required = false) String lastUpdatedOn,
                        @RequestParam(name = "orderBy", required = false) String orderBy,
                        @RequestParam(name = "pageIndex", required = false, defaultValue = "0") Integer pageIndex,
                        @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize) {

                LibraryListFilterCriteria filterCriteria = new LibraryListFilterCriteria(
                                type, name, organization, description,
                                domain, state,
                                separate(updaterLoginIdList).collect(toSet()),
                                DateRangeCriteria.create(lastUpdatedOn));

                PageRequest pageRequest = pageRequest(pageIndex, pageSize, orderBy);

                ScoreUser requester = sessionService.getScoreSystemUser();

                var resultAndCount = libraryQueryService.getLibraryList(requester,
                                filterCriteria, pageRequest);

                PageResponse<LibraryListEntry> response = new PageResponse<>();
                response.setList(resultAndCount.result());
                response.setPage(pageRequest.pageIndex());
                response.setSize(pageRequest.pageSize());
                response.setLength(resultAndCount.count());
                return response;
        }

        @RequestMapping(value = "/ext/releases", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
        public PageResponse<ReleaseListEntryRecord> getReleases(
                        @AuthenticationPrincipal AuthenticatedPrincipal user,
                        @RequestParam(name = "libraryName") String libraryName,
                        @RequestParam(name = "releaseNum", required = false) String releaseNum,
                        @RequestParam(name = "excludeReleaseNums", required = false) String excludeReleaseNums,
                        @RequestParam(name = "releaseStates", required = false) String releaseStates,
                        @RequestParam(name = "namespaceIds", required = false) String namespaceIds,
                        @RequestParam(name = "creatorLoginIdList", required = false) String creatorLoginIdList,
                        @RequestParam(name = "createStart", required = false) String createStart,
                        @RequestParam(name = "createEnd", required = false) String createEnd,
                        @RequestParam(name = "createdOn", required = false) String createdOn,
                        @RequestParam(name = "updaterLoginIdList", required = false) String updaterLoginIdList,
                        @RequestParam(name = "lastUpdatedOn", required = false) String lastUpdatedOn,
                        @RequestParam(name = "orderBy", required = false) String orderBy,
                        @RequestParam(name = "pageIndex", required = false) Integer pageIndex,
                        @RequestParam(name = "pageSize", required = false) Integer pageSize) {

                LibraryId libraryId = service.getLibraryId(libraryName);

                ReleaseListFilterCriteria filterCriteria = new ReleaseListFilterCriteria(
                                libraryId,
                                releaseNum,
                                separate(excludeReleaseNums).collect(toSet()),
                                separate(releaseStates).map(e -> ReleaseState.valueOf(e)).collect(toSet()),
                                separate(namespaceIds).map(e -> new NamespaceId(new BigInteger(e))).collect(toSet()),
                                separate(creatorLoginIdList).collect(toSet()),
                                (hasLength(createdOn)) ? DateRangeCriteria.create(createdOn)
                                                : (hasLength(createStart) || hasLength(createEnd))
                                                                ? DateRangeCriteria.create(
                                                                                hasLength(createStart) ? Long.valueOf(
                                                                                                createStart) : null,
                                                                                hasLength(createEnd) ? Long.valueOf(
                                                                                                createEnd) : null)
                                                                : null,
                                separate(updaterLoginIdList).collect(toSet()),
                                DateRangeCriteria.create(lastUpdatedOn)

                );

                PageRequest pageRequest = pageRequest(pageIndex, pageSize, orderBy);
                ScoreUser requester = sessionService.getScoreSystemUser();

                var resultAndCount = releaseQueryService.getReleaseList(requester,
                                filterCriteria, pageRequest);

                PageResponse<ReleaseListEntryRecord> response = new PageResponse<>();
                response.setList(resultAndCount.result());
                response.setPage(pageRequest.pageIndex());
                response.setSize(pageRequest.pageSize());
                response.setLength(resultAndCount.count());
                return response;
        }

        @RequestMapping(value = "/ext/core-component", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
        public PageResponse<CcListEntryRecord> getCcList(
                        @AuthenticationPrincipal AuthenticatedPrincipal user,
                        @RequestParam(name = "libraryName") String libraryName,
                        @RequestParam(name = "releaseVersion") String releaseVersion,
                        @RequestParam(name = "den", required = false) String den,
                        @RequestParam(name = "definition", required = false) String definition,
                        @RequestParam(name = "module", required = false) String module,
                        @RequestParam(name = "types", required = false) String types,
                        @RequestParam(name = "states", required = false) String states,
                        @RequestParam(name = "reusable", required = false) String reusable,
                        @RequestParam(name = "deprecated", required = false) String deprecated,
                        @RequestParam(name = "newComponent", required = false) String newComponent,
                        @RequestParam(name = "tags", required = false) String tags,
                        @RequestParam(name = "namespaces", required = false) String namespaces,
                        @RequestParam(name = "componentTypes", required = false) String componentTypes,
                        @RequestParam(name = "asccpTypes", required = false) String asccpTypes,
                        @RequestParam(name = "excludes", required = false) String excludes,
                        @RequestParam(name = "isBIEUsable", required = false) String isBIEUsable,
                        @RequestParam(name = "commonlyUsed", required = false) String commonlyUsed,
                        @RequestParam(name = "ownerLoginIdList", required = false) String ownerLoginIdList,
                        @RequestParam(name = "updaterLoginIdList", required = false) String updaterLoginIdList,
                        @RequestParam(name = "lastUpdatedOn", required = false) String lastUpdatedOn,
                        @RequestParam(name = "orderBy", required = false) String orderBy,
                        @RequestParam(name = "pageIndex", required = false) Integer pageIndex,
                        @RequestParam(name = "pageSize", required = false) Integer pageSize)
                        throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

                ReleaseId releaseId = service.getReleaseId(libraryName, releaseVersion);

                ScoreUser requester = sessionService.getScoreSystemUser();

                CcListFilterCriteria filterCriteria = CcListFilterCriteria.builder(releaseId)
                                .den(den)
                                .definition(definition)
                                .module(module)
                                .types(hasLength(types) ? CcListTypes.fromString(types)
                                                : CcListTypes.fromString("ACC,ASCCP,BCCP"))
                                .states(separate(states).map(e -> CcState.valueOf(e)).collect(Collectors.toSet()))
                                .tags(separate(tags).collect(toSet()))
                                .namespaceIds(separate(namespaces).map(e -> NamespaceId.from(e))
                                                .collect(Collectors.toSet()))
                                .componentTypes(separate(componentTypes).map(e -> OagisComponentType.valueOf(e))
                                                .collect(toSet()))
                                .asccpTypes(parseAsccpTypes(asccpTypes))
                                .asccpManifestIds(
                                                (hasLength(den) && den.startsWith("AI:"))
                                                                ? interpreter.interpret(requester, releaseId,
                                                                                den.substring(3).trim())
                                                                : Collections.emptyList())
                                .excludes(separate(excludes).map(e -> new BigInteger(e)).collect(toSet()))
                                .deprecated(hasLength(deprecated) ? ("true".equalsIgnoreCase(deprecated) ? true : false)
                                                : null)
                                .reusable(hasLength(reusable) ? ("true".equalsIgnoreCase(reusable) ? true : false)
                                                : null)
                                .commonlyUsed(hasLength(commonlyUsed)
                                                ? ("true".equalsIgnoreCase(commonlyUsed) ? true : false)
                                                : null)
                                .newComponent(hasLength(newComponent)
                                                ? ("true".equalsIgnoreCase(newComponent) ? true : false)
                                                : null)
                                .isBIEUsable(hasLength(isBIEUsable)
                                                ? ("true".equalsIgnoreCase(isBIEUsable) ? true : false)
                                                : null)
                                .ownerLoginIdList(separate(ownerLoginIdList).collect(toSet()))
                                .updaterLoginIdList(separate(updaterLoginIdList).collect(toSet()))
                                .lastUpdatedTimestampRange(DateRangeCriteria.create(lastUpdatedOn))
                                .build();

                PageRequest pageRequest = pageRequest(pageIndex, pageSize, orderBy);

                ResultAndCount<CcListEntryRecord> resultAndCount = ccQueryService.getCcList(
                                requester, filterCriteria,
                                pageRequest);

                PageResponse<CcListEntryRecord> response = new PageResponse<>();
                response.setList(resultAndCount.result());
                response.setPage(pageRequest.pageIndex());
                response.setSize(pageRequest.pageSize());
                response.setLength(resultAndCount.count());

                return response;

        }

        @RequestMapping(value = "/ext/core-component/children", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
        public PageResponse<ExternalChildComponentRecord> getCcList(
                        @AuthenticationPrincipal AuthenticatedPrincipal user,
                        @RequestParam(name = "libraryName") String libraryName,
                        @RequestParam(name = "releaseVersion") String releaseVersion) {
                List<ExternalChildComponentRecord> childComponents = service
                                .getChildComponents(service.getReleaseId(libraryName, releaseVersion));
                PageResponse<ExternalChildComponentRecord> response = new PageResponse<ExternalChildComponentRecord>();
                response.setList(childComponents);
                return response;
        }

        /*
         * @RequestMapping(value =
         * "/ext/core_component/changes_in_release/{releaseId:[\\d]+}", method =
         * RequestMethod.GET,
         * produces = MediaType.APPLICATION_JSON_VALUE)
         * public CcChangesResponse getCcChanges(
         * 
         * @AuthenticationPrincipal AuthenticatedPrincipal user,
         * 
         * @PathVariable("releaseId") ReleaseId releaseId) {
         * return service.getCcChanges(sessionService.asScoreUser(user), releaseId);
         * }
         */

        @RequestMapping(value = "/ext/core-component/export/standalone", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
        public ResponseEntity<?> exportStandaloneSchema(
                        @RequestParam(name = "libraryName", required = true) String libraryName,
                        @RequestParam(name = "releaseVersion", required = true) String releaseVersion,
                        @RequestParam(name = "guid", required = true) String guid)
                        throws Exception {

                AsccpManifestId asccpManifestId = service
                                .getAsccpManifestId(service.getReleaseId(libraryName, releaseVersion), guid);
                if (asccpManifestId == null) {
                        return new ResponseEntity<>("Component not found", HttpStatus.NOT_FOUND);
                }
                ExportStandaloneSchemaResponse response = service.exportStandaloneSchema(
                                sessionService.getScoreSystemUser(),
                                List.of(asccpManifestId));
                // Arrays.stream(asccpManifestIdList.split(","))
                // .map(e -> new AsccpManifestId(new BigInteger(e)))
                // .collect(Collectors.toList()));

                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=\"" + response.filename() + "\"")
                                .contentType(MediaType.parseMediaType(
                                                (response.filename().endsWith(".zip") ? "application/zip"
                                                                : "application/xml")))
                                .contentLength(response.file().length())
                                .body(new DeleteOnCloseFileSystemResource(response.file()));
        }

        @RequestMapping(value = "/ext/auth", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<String> auth() {
                return ResponseEntity.ok("Authenticated");
        }

        @RequestMapping(value = "/ext/find-usages/{type}/{id:[\\d]+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
        public FindUsagesResponse findUsages(@AuthenticationPrincipal AuthenticatedPrincipal user,
                        @PathVariable("type") String type,
                        @PathVariable("id") BigInteger manifestId) {
                CcType ccType = CcType.valueOf(type.toUpperCase());
                ScoreUser scoreUser = sessionService.getScoreSystemUser();

                switch (ccType) {
                        case ACC:
                                return graphService.findUsages(
                                                scoreUser,
                                                new FindUsagesRequest(ccType, new AccManifestId(manifestId)));
                        case ASCCP:
                                return graphService.findUsages(
                                                scoreUser,
                                                new FindUsagesRequest(ccType, new AsccpManifestId(manifestId)));
                        case BCCP:
                                return graphService.findUsages(
                                                scoreUser,
                                                new FindUsagesRequest(ccType, new BccpManifestId(manifestId)));
                        case DT:
                                return graphService.findUsages(
                                                scoreUser,
                                                new FindUsagesRequest(ccType, new DtManifestId(manifestId)));
                }

                throw new IllegalArgumentException("Unknown graph type " + type);
        }

        @RequestMapping(value = "/ext/graphs/{type}/{id:[\\d]+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
        public Map<String, Object> getGraph(@AuthenticationPrincipal AuthenticatedPrincipal user,
                        @PathVariable("type") String type,
                        @PathVariable("id") BigInteger id,
                        @RequestParam(value = "q", required = false) String query) {

                ScoreUser scoreUser = sessionService.getScoreSystemUser();
                Graph graph;
                switch (type.toLowerCase()) {
                        case "acc":
                        case "extension":
                                graph = graphService.getAccGraph(
                                                scoreUser, new AccManifestId(id));
                                break;

                        case "asccp":
                                graph = graphService.getAsccpGraph(
                                                scoreUser, new AsccpManifestId(id), false);
                                break;

                        case "bccp":
                                graph = graphService.getBccpGraph(
                                                scoreUser, new BccpManifestId(id));
                                break;

                        case "dt":
                                graph = graphService.getDtGraph(
                                                scoreUser, new DtManifestId(id));
                                break;

                        case "top_level_asbiep":
                                graph = graphService.getBieGraph(
                                                scoreUser, new TopLevelAsbiepId(id));
                                break;

                        case "code_list":
                                graph = graphService.getCodeListGraph(
                                                scoreUser, new CodeListManifestId(id));
                                break;

                        default:
                                throw new UnsupportedOperationException();
                }

                Map<String, Object> response = new HashMap();

                if (StringUtils.hasLength(query)) {
                        Collection<List<String>> paths = graph.findPaths(type + id, query);
                        response.put("query", query);
                        response.put("paths", paths.stream()
                                        .map(e -> e.stream()
                                                        .filter(item -> !item.matches("ascc\\d+|bcc\\d+|bdt\\d+"))
                                                        .collect(Collectors.joining(">")))
                                        .collect(Collectors.toList()));
                } else {
                        response.put("graph", graph);
                }

                return response;
        }

        private Set<AsccpType> parseAsccpTypes(String asccpTypes) {
                List<String> filters = separate(asccpTypes)
                                .map(String::trim)
                                .filter(StringUtils::hasLength)
                                .collect(Collectors.toList());
                if (filters.isEmpty()) {
                        return Collections.emptySet();
                }

                EnumSet<AsccpType> include = EnumSet.noneOf(AsccpType.class);
                EnumSet<AsccpType> exclude = EnumSet.noneOf(AsccpType.class);
                for (String filter : filters) {
                        boolean excluded = filter.startsWith("!");
                        String normalized = excluded ? filter.substring(1) : filter;
                        AsccpType type = AsccpType.valueOf(normalized);
                        if (excluded) {
                                exclude.add(type);
                        } else {
                                include.add(type);
                        }
                }

                EnumSet<AsccpType> resolved = include.isEmpty()
                                ? EnumSet.allOf(AsccpType.class)
                                : EnumSet.copyOf(include);
                resolved.removeAll(exclude);
                return resolved;
        }

}
