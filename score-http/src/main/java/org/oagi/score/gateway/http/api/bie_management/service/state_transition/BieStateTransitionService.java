package org.oagi.score.gateway.http.api.bie_management.service.state_transition;

import org.oagi.score.gateway.http.api.bie_management.controller.payload.BieUpdateStateListRequest;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.repository.TopLevelAsbiepQueryRepository;
import org.oagi.score.gateway.http.api.bie_management.service.BieService;
import org.oagi.score.gateway.http.api.bie_management.service.edit_tree.BieEditTreeController;
import org.oagi.score.gateway.http.api.bie_management.service.edit_tree.DefaultBieEditTreeController;
import org.oagi.score.gateway.http.api.bie_management.service.state_transition.rule.BieStateTransitionRule;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextSummaryRecord;
import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.ASBIEP;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.ASCCP;
import static org.springframework.util.StringUtils.hasLength;

/**
 * Executes registered {@link BieStateTransitionRule} instances against the
 * projected BIE dependency graph and applies the resulting validation messages
 * to dialog rows.
 */
@Service
@Transactional(readOnly = true)
public class BieStateTransitionService {

    /**
     * Immutable snapshot of the dependency graph and derived metadata for a
     * requested state transition.
     */
    private record BieStateDependencyGraph(
            /**
             * Root BIE ids explicitly requested by the user.
             *
             * <p>These ids seed graph traversal, define which rows are always
             * part of the future-state map, and anchor edge-distance
             * calculation.</p>
             */
            Set<TopLevelAsbiepId> rootTopLevelAsbiepIds,
            /**
             * Summary records for every root and visible dependency row in the
             * graph.
             *
             * <p>This is the canonical lookup used by row projection, rule
             * evaluation, and final state update validation.</p>
             */
            Map<TopLevelAsbiepId, TopLevelAsbiepSummaryRecord> topLevelAsbiepMap,
            /**
             * Direct prerequisite row ids for each visible dependency target.
             *
             * <p>This is the graph structure later copied into
             * {@link BieStateDependencyTarget#getDependencyTopLevelAsbiepIds()}
             * and used to render the Dependencies column.</p>
             */
            LinkedHashMap<TopLevelAsbiepId, LinkedHashSet<TopLevelAsbiepId>> dependencyMap,
            /**
             * Dependency parents that make a row mandatory when they are part of
             * the active-changing set.
             *
             * <p>This drives checkbox validation and becomes
             * {@link BieStateDependencyTarget#getRequiredDependencyTopLevelAsbiepIds()}.</p>
             */
            LinkedHashMap<TopLevelAsbiepId, LinkedHashSet<TopLevelAsbiepId>> requiredDependencyMap,
            /**
             * Whether each visible row is allowed to be updated along the
             * traversed ownership/path rules.
             *
             * <p>Row projection converts this into
             * {@link BieStateDependencyTarget#isDependencyUpdateAllowed()} and
             * its matching explanatory message.</p>
             */
            LinkedHashMap<TopLevelAsbiepId, Boolean> dependencyUpdateAllowedMap,
            /**
             * UI-facing dependency relations for each visible row.
             *
             * <p>These are direct adjacencies projected into
             * {@link BieStateDependencyTarget#getDependencies()} for the dialog
             * table and tooltips.</p>
             */
            Map<TopLevelAsbiepId, List<BieStateDependencyRelation>> dependenciesRelationMap,
            /**
             * Minimum graph distance from any requested root to each visible
             * row.
             *
             * <p>This ordering metadata keeps near dependencies ahead of deeper
             * ones in the dialog.</p>
             */
            Map<TopLevelAsbiepId, Integer> edgeDistanceMap,
            /**
             * Directed distance-1 dependency pairs used by
             * {@link BieStateTransitionRule} implementations.
             *
             * <p>These edges are separate from the dialog relations because rule
             * evaluation needs both relationship direction and source/target
             * future states.</p>
             */
            List<BieStateTransitionEdge> stateTransitionEdges,
            /**
             * Reverse child lookup used to propagate blocking messages from
             * dependent rows back toward their ancestors.
             *
             * <p>This is used only during preview/validation message
             * resolution.</p>
             */
            Map<TopLevelAsbiepId, Set<TopLevelAsbiepId>> childMap) {
    }

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private BieService bieService;

    @Autowired(required = false)
    private List<BieStateTransitionRule> stateTransitionRules = Collections.emptyList();

    /**
     * Builds the dependency graph for the requested transition and projects it
     * into the dialog rows shown on the UI.
     */
    public List<BieStateDependencyTarget> getStateDependencies(
            ScoreUser requester,
            Collection<TopLevelAsbiepId> rootTopLevelAsbiepIds,
            BieState nextState) {
        if (rootTopLevelAsbiepIds == null || rootTopLevelAsbiepIds.isEmpty()) {
            return Collections.emptyList();
        }

        BieStateDependencyGraph dependencyGraph = buildStateDependencyGraph(requester, rootTopLevelAsbiepIds, nextState);
        List<BieStateDependencyTarget> targets = buildDependencyTargets(requester, dependencyGraph, nextState);
        applyStateTransitionRules(
                targets,
                buildFutureStateMap(
                        targets,
                        dependencyGraph.rootTopLevelAsbiepIds(),
                        nextState,
                        new HashSet<>(),
                        dependencyGraph.topLevelAsbiepMap()),
                dependencyGraph.topLevelAsbiepMap(),
                dependencyGraph.stateTransitionEdges(),
                dependencyGraph.edgeDistanceMap());
        return targets;
    }

    /**
     * Revalidates the user's current dependency selection against the same
     * projected graph used to build the initial dialog.
     */
    public List<BieStateDependencyTarget> validateStateDependencies(
            ScoreUser requester,
            Collection<TopLevelAsbiepId> rootTopLevelAsbiepIds,
            BieState nextState,
            Collection<TopLevelAsbiepId> selectedTopLevelAsbiepIds) {
        BieStateDependencyGraph dependencyGraph = buildStateDependencyGraph(requester, rootTopLevelAsbiepIds, nextState);
        List<BieStateDependencyTarget> targets = buildDependencyTargets(requester, dependencyGraph, nextState);
        applyDependencySelection(targets, dependencyGraph.rootTopLevelAsbiepIds(), selectedTopLevelAsbiepIds);

        Set<TopLevelAsbiepId> selectedIdSet = new HashSet<>();
        if (selectedTopLevelAsbiepIds != null) {
            selectedIdSet.addAll(selectedTopLevelAsbiepIds);
        } else {
            targets.stream()
                    .filter(BieStateDependencyTarget::isChecked)
                    .map(BieStateDependencyTarget::getTopLevelAsbiepId)
                    .forEach(selectedIdSet::add);
        }

        applyStateTransitionRules(
                targets,
                buildFutureStateMap(
                        targets,
                        dependencyGraph.rootTopLevelAsbiepIds(),
                        nextState,
                        selectedIdSet,
                        dependencyGraph.topLevelAsbiepMap()),
                dependencyGraph.topLevelAsbiepMap(),
                dependencyGraph.stateTransitionEdges(),
                dependencyGraph.edgeDistanceMap());
        return targets;
    }

    /**
     * Validates the requested state transition against cross-owner reuse and
     * inheritance rules before any write is attempted.
     */
    public void validateStateChange(ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId, BieState state) {
        ensureBieRelationshipsForChangingState(requester, topLevelAsbiepId, state);
    }

    /**
     * Applies a state transition to the root BIE and any dependency rows the
     * user selected in the dialog.
     */
    @Transactional
    public void updateState(ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId, BieState state) {
        updateState(requester, topLevelAsbiepId, state, null);
    }

    /**
     * Applies a validated state transition to the root BIE and the approved
     * dependency rows.
     */
    @Transactional
    public void updateState(ScoreUser requester,
                            TopLevelAsbiepId topLevelAsbiepId,
                            BieState state,
                            Collection<TopLevelAsbiepId> dependencyTopLevelAsbiepIds) {
        validateStateChange(requester, topLevelAsbiepId, state);
        ensureDependencySelectionStateChange(requester, topLevelAsbiepId, state, dependencyTopLevelAsbiepIds);

        BieEditTreeController treeController = getTreeController(requester, topLevelAsbiepId);
        treeController.updateState(requester, state, dependencyTopLevelAsbiepIds);
    }

    /**
     * Applies the same validated state transition to every BIE selected in the
     * list view request.
     */
    @Transactional
    public void updateStateBieList(ScoreUser requester, BieUpdateStateListRequest request) {
        if (request.getTopLevelAsbiepIds() == null || request.getTopLevelAsbiepIds().isEmpty()) {
            return;
        }

        request.getTopLevelAsbiepIds().forEach(topLevelAsbiepId ->
                validateStateChange(requester, topLevelAsbiepId, request.getToState()));
        ensureDependencySelectionStateChange(
                requester,
                request.getTopLevelAsbiepIds(),
                request.getToState(),
                request.getDependencyTopLevelAsbiepIds());

        request.getTopLevelAsbiepIds().forEach(topLevelAsbiepId -> {
            BieEditTreeController treeController = getTreeController(requester, topLevelAsbiepId);
            treeController.updateState(requester, request.getToState(), request.getDependencyTopLevelAsbiepIds());
        });
    }

    /**
     * Builds the dependency graph once so preview rendering and checkbox
     * validation use the same graph snapshot.
     */
    private BieStateDependencyGraph buildStateDependencyGraph(
            ScoreUser requester,
            Collection<TopLevelAsbiepId> rootTopLevelAsbiepIds,
            BieState nextState) {
        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);

        Set<TopLevelAsbiepId> rootIdSet = new LinkedHashSet<>(rootTopLevelAsbiepIds);
        Map<TopLevelAsbiepId, TopLevelAsbiepSummaryRecord> topLevelAsbiepMap = new LinkedHashMap<>();
        LinkedHashMap<TopLevelAsbiepId, LinkedHashSet<TopLevelAsbiepId>> dependencyMap = new LinkedHashMap<>();
        LinkedHashMap<TopLevelAsbiepId, LinkedHashSet<TopLevelAsbiepId>> requiredDependencyMap = new LinkedHashMap<>();
        LinkedHashMap<TopLevelAsbiepId, Boolean> dependencyUpdateAllowedMap = new LinkedHashMap<>();
        Set<String> visitedStates = new HashSet<>();

        for (TopLevelAsbiepId rootTopLevelAsbiepId : rootIdSet) {
            TopLevelAsbiepSummaryRecord rootTopLevelAsbiep =
                    topLevelAsbiepQuery.getTopLevelAsbiepSummary(rootTopLevelAsbiepId);
            if (rootTopLevelAsbiep != null) {
                collectDependencyTargets(rootIdSet, rootTopLevelAsbiep, nextState,
                        new LinkedHashSet<>(Collections.singleton(rootTopLevelAsbiepId)),
                        true, true, visitedStates, topLevelAsbiepMap, dependencyMap, requiredDependencyMap,
                        dependencyUpdateAllowedMap, topLevelAsbiepQuery);
            }
        }

        Set<TopLevelAsbiepId> associatedTopLevelAsbiepIds = new LinkedHashSet<>(rootIdSet);
        associatedTopLevelAsbiepIds.addAll(dependencyMap.keySet());
        Map<TopLevelAsbiepId, List<BieStateDependencyRelation>> dependenciesRelationMap =
                buildDependenciesRelationMap(topLevelAsbiepQuery, topLevelAsbiepMap, associatedTopLevelAsbiepIds);
        Map<TopLevelAsbiepId, List<BieStateDependencyRelation>> connectedRelationMap =
                buildConnectedRelationMap(topLevelAsbiepQuery, topLevelAsbiepMap, associatedTopLevelAsbiepIds);
        Map<TopLevelAsbiepId, Integer> edgeDistanceMap =
                buildEdgeDistanceMap(rootIdSet, connectedRelationMap);
        List<BieStateTransitionEdge> stateTransitionEdges =
                buildStateTransitionEdges(topLevelAsbiepQuery, topLevelAsbiepMap, associatedTopLevelAsbiepIds);

        Map<TopLevelAsbiepId, Set<TopLevelAsbiepId>> childMap = new LinkedHashMap<>();
        for (Map.Entry<TopLevelAsbiepId, LinkedHashSet<TopLevelAsbiepId>> entry : dependencyMap.entrySet()) {
            for (TopLevelAsbiepId dependencyTopLevelAsbiepId : entry.getValue()) {
                if (isBackwardStateTransition(nextState)) {
                    childMap.computeIfAbsent(entry.getKey(), key -> new LinkedHashSet<>())
                            .add(dependencyTopLevelAsbiepId);
                } else if (dependencyMap.containsKey(dependencyTopLevelAsbiepId)) {
                    childMap.computeIfAbsent(dependencyTopLevelAsbiepId, key -> new LinkedHashSet<>())
                            .add(entry.getKey());
                }
            }
        }

        return new BieStateDependencyGraph(
                rootIdSet,
                topLevelAsbiepMap,
                dependencyMap,
                requiredDependencyMap,
                dependencyUpdateAllowedMap,
                dependenciesRelationMap,
                edgeDistanceMap,
                stateTransitionEdges,
                childMap
        );
    }

    /**
     * Converts the graph snapshot into the transport model used by the state
     * transition dialog.
     */
    private List<BieStateDependencyTarget> buildDependencyTargets(
            ScoreUser requester,
            BieStateDependencyGraph dependencyGraph,
            BieState nextState) {
        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
        var asbiepQuery = repositoryFactory.asbiepQueryRepository(requester);
        var businessContextQuery = repositoryFactory.businessContextQueryRepository(requester);
        Map<TopLevelAsbiepId, String> stateTransitionMessageMap = new LinkedHashMap<>();

        return dependencyGraph.dependencyMap().entrySet().stream()
                .map(entry -> {
                    TopLevelAsbiepSummaryRecord target = dependencyGraph.topLevelAsbiepMap().get(entry.getKey());
                    String stateTransitionMessage = resolveStateTransitionMessage(
                            entry.getKey(),
                            nextState,
                            dependencyGraph.topLevelAsbiepMap(),
                            dependencyGraph.dependencyUpdateAllowedMap(),
                            dependencyGraph.childMap(),
                            dependencyGraph.rootTopLevelAsbiepIds(),
                            dependencyGraph.dependencyMap().keySet(),
                            topLevelAsbiepQuery,
                            stateTransitionMessageMap,
                            new HashSet<>());
                    return toBieStateDependencyTarget(
                            target,
                            asbiepQuery.getAsbiepSummary(target.asbiepId()),
                            businessContextQuery.getBusinessContextSummaryList(target.topLevelAsbiepId()),
                            dependencyGraph.dependenciesRelationMap().getOrDefault(entry.getKey(), Collections.emptyList()),
                            dependencyGraph.edgeDistanceMap().getOrDefault(entry.getKey(), Integer.MAX_VALUE),
                            new ArrayList<>(entry.getValue()),
                            new ArrayList<>(dependencyGraph.requiredDependencyMap().getOrDefault(entry.getKey(), new LinkedHashSet<>())),
                            dependencyGraph.dependencyUpdateAllowedMap().getOrDefault(entry.getKey(), false),
                            stateTransitionMessage,
                            nextState);
                })
                .toList();
    }

    /**
     * Applies the current checkbox selection to the dialog rows before
     * edge-based rule validation runs again.
     */
    private void applyDependencySelection(
            List<BieStateDependencyTarget> targets,
            Collection<TopLevelAsbiepId> rootTopLevelAsbiepIds,
            Collection<TopLevelAsbiepId> selectedTopLevelAsbiepIds) {
        Set<TopLevelAsbiepId> selectedIdSet = new HashSet<>();
        if (selectedTopLevelAsbiepIds != null) {
            selectedIdSet.addAll(selectedTopLevelAsbiepIds);
        } else {
            targets.stream()
                    .filter(BieStateDependencyTarget::isDependencyUpdateAllowed)
                    .map(BieStateDependencyTarget::getTopLevelAsbiepId)
                    .forEach(selectedIdSet::add);
        }

        Set<TopLevelAsbiepId> activeChangingIdSet = new HashSet<>(rootTopLevelAsbiepIds);
        activeChangingIdSet.addAll(selectedIdSet);

        targets.forEach(target -> {
            boolean selectable = target.isDependencyUpdateAllowed();
            boolean checked = selectable && selectedIdSet.contains(target.getTopLevelAsbiepId());
            boolean required = target.getRequiredDependencyTopLevelAsbiepIds() != null &&
                    target.getRequiredDependencyTopLevelAsbiepIds().stream().anyMatch(activeChangingIdSet::contains);
            target.setChecked(checked);
            target.setSelectionConflict(selectable && required && !checked);
            target.setSelectionConflictMessage(target.isSelectionConflict()
                    ? "This BIE must be updated together."
                    : null);
            if (!checked && !required) {
                target.setStateTransitionAllowed(true);
                target.setStateTransitionMessage(null);
            }
        });
    }

    /**
     * Converts repository records into the dialog row transport model.
     */
    private BieStateDependencyTarget toBieStateDependencyTarget(
            TopLevelAsbiepSummaryRecord topLevelAsbiep,
            AsbiepSummaryRecord asbiep,
            List<BusinessContextSummaryRecord> businessContexts,
            List<BieStateDependencyRelation> dependencies,
            int edgeDistance,
            List<TopLevelAsbiepId> dependencyTopLevelAsbiepIds,
            List<TopLevelAsbiepId> requiredDependencyTopLevelAsbiepIds,
            boolean dependencyUpdateAllowed,
            String stateTransitionMessage,
            BieState nextState) {
        String dependencyUpdateMessage = getDependencyUpdateMessage(topLevelAsbiep, dependencyUpdateAllowed, nextState);
        return new BieStateDependencyTarget(
                topLevelAsbiep.topLevelAsbiepId(),
                dependencyTopLevelAsbiepIds,
                requiredDependencyTopLevelAsbiepIds,
                dependencies,
                edgeDistance,
                topLevelAsbiep.propertyTerm(),
                topLevelAsbiep.displayName(),
                topLevelAsbiep.guid().value(),
                businessContexts.stream().map(BusinessContextSummaryRecord::name).toList(),
                topLevelAsbiep.version(),
                topLevelAsbiep.status(),
                (asbiep != null) ? asbiep.remark() : null,
                topLevelAsbiep.state(),
                dependencyUpdateAllowed,
                dependencyUpdateMessage,
                stateTransitionMessage == null,
                stateTransitionMessage,
                dependencyUpdateAllowed && stateTransitionMessage == null,
                false,
                null
        );
    }

    /**
     * Traverses the dependency graph in the direction required by the
     * transition and records the rows that should be visible in the dialog.
     */
    private void collectDependencyTargets(
            Set<TopLevelAsbiepId> rootIdSet,
            TopLevelAsbiepSummaryRecord currentTopLevelAsbiep,
            BieState nextState,
            LinkedHashSet<TopLevelAsbiepId> dependencyTopLevelAsbiepIds,
            boolean dependencyPathAllowed,
            boolean requiredPathAllowed,
            Set<String> visitedStates,
            Map<TopLevelAsbiepId, TopLevelAsbiepSummaryRecord> topLevelAsbiepMap,
            LinkedHashMap<TopLevelAsbiepId, LinkedHashSet<TopLevelAsbiepId>> dependencyMap,
            LinkedHashMap<TopLevelAsbiepId, LinkedHashSet<TopLevelAsbiepId>> requiredDependencyMap,
            LinkedHashMap<TopLevelAsbiepId, Boolean> dependencyUpdateAllowedMap,
            TopLevelAsbiepQueryRepository topLevelAsbiepQuery) {
        String visitedStateKey = currentTopLevelAsbiep.topLevelAsbiepId() + "|" +
                dependencyTopLevelAsbiepIds.stream().map(TopLevelAsbiepId::toString).sorted()
                        .reduce((left, right) -> left + "," + right).orElse("") +
                "|" + dependencyPathAllowed + "|" + requiredPathAllowed;
        if (!visitedStates.add(visitedStateKey)) {
            return;
        }

        topLevelAsbiepMap.putIfAbsent(currentTopLevelAsbiep.topLevelAsbiepId(), currentTopLevelAsbiep);

        if (isBackwardStateTransition(nextState)) {
            topLevelAsbiepQuery.getReusedTopLevelAsbiepSummaryList(currentTopLevelAsbiep.topLevelAsbiepId()).stream()
                    .forEach(target -> collectDependencyTarget(rootIdSet, currentTopLevelAsbiep, target, nextState,
                            dependencyTopLevelAsbiepIds, dependencyPathAllowed, requiredPathAllowed,
                            true, true, true, visitedStates, topLevelAsbiepMap,
                            dependencyMap, requiredDependencyMap, dependencyUpdateAllowedMap, topLevelAsbiepQuery));

            topLevelAsbiepQuery.getReusingTopLevelAsbiepSummaryList(currentTopLevelAsbiep.topLevelAsbiepId()).stream()
                    .forEach(target -> collectDependencyTarget(rootIdSet, currentTopLevelAsbiep, target, nextState,
                            dependencyTopLevelAsbiepIds, dependencyPathAllowed, requiredPathAllowed,
                            true, true, false, visitedStates, topLevelAsbiepMap,
                            dependencyMap, requiredDependencyMap, dependencyUpdateAllowedMap, topLevelAsbiepQuery));

            TopLevelAsbiepSummaryRecord basedTopLevelAsbiep =
                    topLevelAsbiepQuery.getTopLevelAsbiepSummary(currentTopLevelAsbiep.basedTopLevelAsbiepId());
            if (basedTopLevelAsbiep != null) {
                collectDependencyTarget(rootIdSet, currentTopLevelAsbiep, basedTopLevelAsbiep, nextState,
                        dependencyTopLevelAsbiepIds, dependencyPathAllowed, requiredPathAllowed,
                        true, true, false, visitedStates, topLevelAsbiepMap,
                        dependencyMap, requiredDependencyMap, dependencyUpdateAllowedMap, topLevelAsbiepQuery);
            }

            topLevelAsbiepQuery.getDerivedTopLevelAsbiepSummaryList(currentTopLevelAsbiep.topLevelAsbiepId()).stream()
                    .forEach(target -> collectDependencyTarget(rootIdSet, currentTopLevelAsbiep, target, nextState,
                            dependencyTopLevelAsbiepIds, dependencyPathAllowed, requiredPathAllowed,
                            true, true, false, visitedStates, topLevelAsbiepMap,
                            dependencyMap, requiredDependencyMap, dependencyUpdateAllowedMap, topLevelAsbiepQuery));
            return;
        }

        topLevelAsbiepQuery.getReusingTopLevelAsbiepSummaryList(currentTopLevelAsbiep.topLevelAsbiepId()).stream()
                .forEach(target -> collectDependencyTarget(rootIdSet, currentTopLevelAsbiep, target, nextState,
                        dependencyTopLevelAsbiepIds, dependencyPathAllowed, requiredPathAllowed,
                        true, true, true, visitedStates, topLevelAsbiepMap,
                        dependencyMap, requiredDependencyMap, dependencyUpdateAllowedMap, topLevelAsbiepQuery));

        TopLevelAsbiepSummaryRecord basedTopLevelAsbiep =
                topLevelAsbiepQuery.getTopLevelAsbiepSummary(currentTopLevelAsbiep.basedTopLevelAsbiepId());
        if (basedTopLevelAsbiep != null) {
            collectDependencyTarget(rootIdSet, currentTopLevelAsbiep, basedTopLevelAsbiep, nextState,
                    dependencyTopLevelAsbiepIds, dependencyPathAllowed, requiredPathAllowed,
                    true, true, true, visitedStates, topLevelAsbiepMap,
                    dependencyMap, requiredDependencyMap, dependencyUpdateAllowedMap, topLevelAsbiepQuery);
        }

        topLevelAsbiepQuery.getDerivedTopLevelAsbiepSummaryList(currentTopLevelAsbiep.topLevelAsbiepId()).stream()
                .forEach(target -> collectDependencyTarget(rootIdSet, currentTopLevelAsbiep, target, nextState,
                        dependencyTopLevelAsbiepIds, dependencyPathAllowed, requiredPathAllowed,
                        true, true, false, visitedStates, topLevelAsbiepMap,
                        dependencyMap, requiredDependencyMap, dependencyUpdateAllowedMap, topLevelAsbiepQuery));
    }

    /**
     * Adds one dependency row and continues traversal through that row while
     * preserving the ownership-aware dependency flags for the current path.
     */
    private void collectDependencyTarget(
            Set<TopLevelAsbiepId> rootIdSet,
            TopLevelAsbiepSummaryRecord currentTopLevelAsbiep,
            TopLevelAsbiepSummaryRecord target,
            BieState nextState,
            LinkedHashSet<TopLevelAsbiepId> dependencyTopLevelAsbiepIds,
            boolean dependencyPathAllowed,
            boolean requiredPathAllowed,
            boolean dependencyUpdateAllowedOnPath,
            boolean continueTraversal,
            boolean requiredOnPath,
            Set<String> visitedStates,
            Map<TopLevelAsbiepId, TopLevelAsbiepSummaryRecord> topLevelAsbiepMap,
            LinkedHashMap<TopLevelAsbiepId, LinkedHashSet<TopLevelAsbiepId>> dependencyMap,
            LinkedHashMap<TopLevelAsbiepId, LinkedHashSet<TopLevelAsbiepId>> requiredDependencyMap,
            LinkedHashMap<TopLevelAsbiepId, Boolean> dependencyUpdateAllowedMap,
            TopLevelAsbiepQueryRepository topLevelAsbiepQuery) {
        topLevelAsbiepMap.putIfAbsent(target.topLevelAsbiepId(), target);

        boolean sameOwner = currentTopLevelAsbiep.owner().userId().equals(target.owner().userId());
        boolean stateChangeNeeded = shouldApplyDependencyStateChange(target.state(), nextState);
        boolean visible = !rootIdSet.contains(target.topLevelAsbiepId()) && (stateChangeNeeded || !sameOwner);
        boolean nextDependencyPathAllowed = dependencyPathAllowed && sameOwner && dependencyUpdateAllowedOnPath;
        boolean nextRequiredPathAllowed = requiredPathAllowed && requiredOnPath;

        if (visible) {
            dependencyMap.computeIfAbsent(target.topLevelAsbiepId(), key -> new LinkedHashSet<>())
                    .addAll(dependencyTopLevelAsbiepIds);
            if (nextRequiredPathAllowed) {
                requiredDependencyMap.computeIfAbsent(target.topLevelAsbiepId(), key -> new LinkedHashSet<>())
                        .addAll(dependencyTopLevelAsbiepIds);
            }
            dependencyUpdateAllowedMap.merge(
                    target.topLevelAsbiepId(),
                    nextDependencyPathAllowed && stateChangeNeeded,
                    Boolean::logicalOr);
        }

        if (!continueTraversal) {
            return;
        }

        LinkedHashSet<TopLevelAsbiepId> nextDependencies = visible
                ? new LinkedHashSet<>(Collections.singleton(target.topLevelAsbiepId()))
                : new LinkedHashSet<>(dependencyTopLevelAsbiepIds);
        collectDependencyTargets(rootIdSet, target, nextState, nextDependencies,
                nextDependencyPathAllowed, nextRequiredPathAllowed, visitedStates,
                topLevelAsbiepMap, dependencyMap, requiredDependencyMap,
                dependencyUpdateAllowedMap, topLevelAsbiepQuery);
    }

    private BieStateDependencyRelation toBieStateDependencyRelation(TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        return toBieStateDependencyRelation(topLevelAsbiep, null);
    }

    private BieStateDependencyRelation toBieStateDependencyRelation(
            TopLevelAsbiepSummaryRecord topLevelAsbiep,
            BieStateTransitionDependency dependency) {
        return new BieStateDependencyRelation(
                topLevelAsbiep.topLevelAsbiepId(),
                dependency,
                toName(topLevelAsbiep),
                topLevelAsbiep.guid().value()
        );
    }

    private boolean isBackwardStateTransition(BieState nextState) {
        return nextState == BieState.WIP;
    }

    private String resolveStateTransitionMessage(
            TopLevelAsbiepId topLevelAsbiepId,
            BieState nextState,
            Map<TopLevelAsbiepId, TopLevelAsbiepSummaryRecord> topLevelAsbiepMap,
            Map<TopLevelAsbiepId, Boolean> dependencyUpdateAllowedMap,
            Map<TopLevelAsbiepId, Set<TopLevelAsbiepId>> childMap,
            Set<TopLevelAsbiepId> rootTopLevelAsbiepIds,
            Set<TopLevelAsbiepId> affectedTopLevelAsbiepIds,
            TopLevelAsbiepQueryRepository topLevelAsbiepQuery,
            Map<TopLevelAsbiepId, String> stateTransitionMessageMap,
            Set<TopLevelAsbiepId> visitedTopLevelAsbiepIds) {
        if (stateTransitionMessageMap.containsKey(topLevelAsbiepId)) {
            return stateTransitionMessageMap.get(topLevelAsbiepId);
        }
        if (!visitedTopLevelAsbiepIds.add(topLevelAsbiepId)) {
            return null;
        }

        TopLevelAsbiepSummaryRecord topLevelAsbiep = topLevelAsbiepMap.get(topLevelAsbiepId);
        String directMessage = getStateTransitionMessage(
                topLevelAsbiep.state(),
                dependencyUpdateAllowedMap.getOrDefault(topLevelAsbiepId, false),
                nextState);
        if (directMessage == null) {
            directMessage = resolveBackwardDependencyConflictMessage(
                    topLevelAsbiep,
                    rootTopLevelAsbiepIds,
                    affectedTopLevelAsbiepIds,
                    nextState,
                    topLevelAsbiepQuery);
        }
        if (directMessage != null) {
            stateTransitionMessageMap.put(topLevelAsbiepId, directMessage);
            visitedTopLevelAsbiepIds.remove(topLevelAsbiepId);
            return directMessage;
        }

        for (TopLevelAsbiepId childTopLevelAsbiepId : childMap.getOrDefault(topLevelAsbiepId, Collections.emptySet())) {
            String childMessage = resolveStateTransitionMessage(
                    childTopLevelAsbiepId, nextState, topLevelAsbiepMap,
                    dependencyUpdateAllowedMap, childMap, rootTopLevelAsbiepIds,
                    affectedTopLevelAsbiepIds, topLevelAsbiepQuery,
                    stateTransitionMessageMap, visitedTopLevelAsbiepIds);
            if (childMessage != null) {
                if (rootTopLevelAsbiepIds.contains(childTopLevelAsbiepId)) {
                    continue;
                }
                String propagatedMessage = toRelatedConflictMessage(childMessage, nextState);
                stateTransitionMessageMap.put(topLevelAsbiepId, propagatedMessage);
                visitedTopLevelAsbiepIds.remove(topLevelAsbiepId);
                return propagatedMessage;
            }
        }

        stateTransitionMessageMap.put(topLevelAsbiepId, null);
        visitedTopLevelAsbiepIds.remove(topLevelAsbiepId);
        return null;
    }

    private String getStateTransitionMessage(
            BieState currentState,
            boolean dependencyUpdateAllowed,
            BieState nextState) {
        if (currentState == null || nextState == null) {
            return null;
        }
        if (!dependencyUpdateAllowed && shouldApplyDependencyStateChange(currentState, nextState)) {
            return "This BIE is owned by another user and cannot be updated.";
        }
        if (nextState.getLevel() <= BieState.WIP.getLevel()) {
            return null;
        }
        BieState requiredState = requiredState(nextState);
        if (currentState.getLevel() < requiredState.getLevel()) {
            return "This BIE should be '" + requiredState + "' first to proceed.";
        }
        return null;
    }

    private String getDependencyUpdateMessage(
            TopLevelAsbiepSummaryRecord topLevelAsbiep,
            boolean dependencyUpdateAllowed,
            BieState nextState) {
        if (!shouldApplyDependencyStateChange(topLevelAsbiep.state(), nextState)) {
            return "This BIE will not be updated.";
        }
        return null;
    }

    private String toRelatedConflictMessage(String childMessage, BieState nextState) {
        if (childMessage != null) {
            if (childMessage.startsWith("This BIE should be '")) {
                return childMessage.replaceFirst("This BIE", "A related BIE");
            }
            if (childMessage.contains("owned by another user")) {
                return "A related BIE is owned by another user and cannot be updated.";
            }
        }
        return "A related BIE should be '" + nextState + "' first to proceed.";
    }

    private String resolveBackwardDependencyConflictMessage(
            TopLevelAsbiepSummaryRecord topLevelAsbiep,
            Set<TopLevelAsbiepId> rootTopLevelAsbiepIds,
            Set<TopLevelAsbiepId> affectedTopLevelAsbiepIds,
            BieState nextState,
            TopLevelAsbiepQueryRepository topLevelAsbiepQuery) {
        if (!isBackwardStateTransition(nextState) || topLevelAsbiep == null) {
            return null;
        }

        Set<TopLevelAsbiepId> changingTopLevelAsbiepIds = new HashSet<>(affectedTopLevelAsbiepIds);
        changingTopLevelAsbiepIds.addAll(rootTopLevelAsbiepIds);

        for (TopLevelAsbiepSummaryRecord dependency :
                topLevelAsbiepQuery.getReusingTopLevelAsbiepSummaryList(topLevelAsbiep.topLevelAsbiepId())) {
            if (!changingTopLevelAsbiepIds.contains(dependency.topLevelAsbiepId()) &&
                    dependency.state() != null &&
                    dependency.state().getLevel() > nextState.getLevel()) {
                return "A related BIE should be '" + nextState + "' first to proceed.";
            }
        }

        return null;
    }

    /**
     * Builds the direct dependency relations displayed in the dialog's
     * Dependencies column.
     */
    private Map<TopLevelAsbiepId, List<BieStateDependencyRelation>> buildDependenciesRelationMap(
            TopLevelAsbiepQueryRepository topLevelAsbiepQuery,
            Map<TopLevelAsbiepId, TopLevelAsbiepSummaryRecord> topLevelAsbiepMap,
            Set<TopLevelAsbiepId> associatedTopLevelAsbiepIds) {
        Map<TopLevelAsbiepId, List<BieStateDependencyRelation>> dependenciesRelationMap = new LinkedHashMap<>();

        for (TopLevelAsbiepId topLevelAsbiepId : associatedTopLevelAsbiepIds) {
            LinkedHashMap<TopLevelAsbiepId, BieStateDependencyRelation> relationMap = new LinkedHashMap<>();

            topLevelAsbiepQuery.getReusingTopLevelAsbiepSummaryList(topLevelAsbiepId).stream()
                    .filter(target -> associatedTopLevelAsbiepIds.contains(target.topLevelAsbiepId()))
                    .forEach(target -> relationMap.putIfAbsent(
                            target.topLevelAsbiepId(),
                            toBieStateDependencyRelation(target, BieStateTransitionDependency.REUSES)));

            TopLevelAsbiepSummaryRecord topLevelAsbiep = topLevelAsbiepMap.get(topLevelAsbiepId);
            TopLevelAsbiepSummaryRecord basedTopLevelAsbiep =
                    (topLevelAsbiep != null)
                            ? topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiep.basedTopLevelAsbiepId())
                            : null;
            if (basedTopLevelAsbiep != null && associatedTopLevelAsbiepIds.contains(basedTopLevelAsbiep.topLevelAsbiepId())) {
                relationMap.putIfAbsent(
                        basedTopLevelAsbiep.topLevelAsbiepId(),
                        toBieStateDependencyRelation(basedTopLevelAsbiep, BieStateTransitionDependency.INHERITS_FROM));
            }

            dependenciesRelationMap.put(topLevelAsbiepId, new ArrayList<>(relationMap.values()));
        }

        return dependenciesRelationMap;
    }

    /**
     * Builds the undirected connected relation map used for edge distance
     * calculation across the visible dependency graph.
     */
    private Map<TopLevelAsbiepId, List<BieStateDependencyRelation>> buildConnectedRelationMap(
            TopLevelAsbiepQueryRepository topLevelAsbiepQuery,
            Map<TopLevelAsbiepId, TopLevelAsbiepSummaryRecord> topLevelAsbiepMap,
            Set<TopLevelAsbiepId> associatedTopLevelAsbiepIds) {
        Map<TopLevelAsbiepId, List<BieStateDependencyRelation>> connectedRelationMap = new LinkedHashMap<>();

        for (TopLevelAsbiepId topLevelAsbiepId : associatedTopLevelAsbiepIds) {
            LinkedHashMap<TopLevelAsbiepId, BieStateDependencyRelation> relationMap = new LinkedHashMap<>();

            topLevelAsbiepQuery.getReusingTopLevelAsbiepSummaryList(topLevelAsbiepId).stream()
                    .filter(target -> associatedTopLevelAsbiepIds.contains(target.topLevelAsbiepId()))
                    .forEach(target -> relationMap.putIfAbsent(target.topLevelAsbiepId(), toBieStateDependencyRelation(target)));

            topLevelAsbiepQuery.getReusedTopLevelAsbiepSummaryList(topLevelAsbiepId).stream()
                    .filter(target -> associatedTopLevelAsbiepIds.contains(target.topLevelAsbiepId()))
                    .forEach(target -> relationMap.putIfAbsent(target.topLevelAsbiepId(), toBieStateDependencyRelation(target)));

            topLevelAsbiepQuery.getDerivedTopLevelAsbiepSummaryList(topLevelAsbiepId).stream()
                    .filter(target -> associatedTopLevelAsbiepIds.contains(target.topLevelAsbiepId()))
                    .forEach(target -> relationMap.putIfAbsent(target.topLevelAsbiepId(), toBieStateDependencyRelation(target)));

            TopLevelAsbiepSummaryRecord topLevelAsbiep = topLevelAsbiepMap.get(topLevelAsbiepId);
            TopLevelAsbiepSummaryRecord basedTopLevelAsbiep =
                    (topLevelAsbiep != null)
                            ? topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiep.basedTopLevelAsbiepId())
                            : null;
            if (basedTopLevelAsbiep != null && associatedTopLevelAsbiepIds.contains(basedTopLevelAsbiep.topLevelAsbiepId())) {
                relationMap.putIfAbsent(basedTopLevelAsbiep.topLevelAsbiepId(), toBieStateDependencyRelation(basedTopLevelAsbiep));
            }

            connectedRelationMap.put(topLevelAsbiepId, new ArrayList<>(relationMap.values()));
        }

        return connectedRelationMap;
    }

    private Map<TopLevelAsbiepId, Integer> buildEdgeDistanceMap(
            Set<TopLevelAsbiepId> rootTopLevelAsbiepIds,
            Map<TopLevelAsbiepId, List<BieStateDependencyRelation>> connectedRelationMap) {
        Map<TopLevelAsbiepId, Integer> edgeDistanceMap = new HashMap<>();
        Queue<TopLevelAsbiepId> queue = new LinkedList<>();

        for (TopLevelAsbiepId rootTopLevelAsbiepId : rootTopLevelAsbiepIds) {
            edgeDistanceMap.put(rootTopLevelAsbiepId, 0);
            queue.offer(rootTopLevelAsbiepId);
        }

        while (!queue.isEmpty()) {
            TopLevelAsbiepId currentTopLevelAsbiepId = queue.poll();
            int currentDistance = edgeDistanceMap.getOrDefault(currentTopLevelAsbiepId, 0);
            for (BieStateDependencyRelation relation :
                    connectedRelationMap.getOrDefault(currentTopLevelAsbiepId, Collections.emptyList())) {
                if (edgeDistanceMap.containsKey(relation.getTopLevelAsbiepId())) {
                    continue;
                }
                edgeDistanceMap.put(relation.getTopLevelAsbiepId(), currentDistance + 1);
                queue.offer(relation.getTopLevelAsbiepId());
            }
        }

        return edgeDistanceMap;
    }

    /**
     * Computes the future state of every visible node after applying the root
     * transition and the currently selected dependency rows.
     */
    private Map<TopLevelAsbiepId, BieState> buildFutureStateMap(
            List<BieStateDependencyTarget> targets,
            Set<TopLevelAsbiepId> rootTopLevelAsbiepIds,
            BieState nextState,
            Set<TopLevelAsbiepId> selectedTopLevelAsbiepIds,
            Map<TopLevelAsbiepId, TopLevelAsbiepSummaryRecord> topLevelAsbiepMap) {
        Map<TopLevelAsbiepId, BieState> futureStateMap = new LinkedHashMap<>();

        for (Map.Entry<TopLevelAsbiepId, TopLevelAsbiepSummaryRecord> entry : topLevelAsbiepMap.entrySet()) {
            futureStateMap.put(entry.getKey(), entry.getValue().state());
        }

        for (TopLevelAsbiepId rootTopLevelAsbiepId : rootTopLevelAsbiepIds) {
            futureStateMap.put(rootTopLevelAsbiepId, nextState);
        }

        for (BieStateDependencyTarget target : targets) {
            if (!selectedTopLevelAsbiepIds.contains(target.getTopLevelAsbiepId()) ||
                    !target.isDependencyUpdateAllowed() ||
                    !shouldApplyDependencyStateChange(target.getState(), nextState)) {
                continue;
            }
            futureStateMap.put(target.getTopLevelAsbiepId(), nextState);
        }

        return futureStateMap;
    }

    /**
     * Builds the direct distance-1 dependency edges evaluated by transition
     * rules. Reuse and inheritance are emitted in both directions so rules can
     * describe the user action from either side of the relationship.
     */
    private List<BieStateTransitionEdge> buildStateTransitionEdges(
            TopLevelAsbiepQueryRepository topLevelAsbiepQuery,
            Map<TopLevelAsbiepId, TopLevelAsbiepSummaryRecord> topLevelAsbiepMap,
            Set<TopLevelAsbiepId> associatedTopLevelAsbiepIds) {
        LinkedHashMap<String, BieStateTransitionEdge> edgeMap = new LinkedHashMap<>();

        for (TopLevelAsbiepId sourceTopLevelAsbiepId : associatedTopLevelAsbiepIds) {
            topLevelAsbiepQuery.getReusingTopLevelAsbiepSummaryList(sourceTopLevelAsbiepId).stream()
                    .filter(target -> associatedTopLevelAsbiepIds.contains(target.topLevelAsbiepId()))
                    .forEach(target -> {
                        String edgeKey = sourceTopLevelAsbiepId + "|REUSES|" + target.topLevelAsbiepId();
                        edgeMap.putIfAbsent(edgeKey, new BieStateTransitionEdge(
                                sourceTopLevelAsbiepId,
                                target.topLevelAsbiepId(),
                                BieStateTransitionDependency.REUSES));
                        String reverseEdgeKey = target.topLevelAsbiepId() + "|REUSED_BY|" + sourceTopLevelAsbiepId;
                        edgeMap.putIfAbsent(reverseEdgeKey, new BieStateTransitionEdge(
                                target.topLevelAsbiepId(),
                                sourceTopLevelAsbiepId,
                                BieStateTransitionDependency.REUSED_BY));
                    });

            TopLevelAsbiepSummaryRecord sourceTopLevelAsbiep = topLevelAsbiepMap.get(sourceTopLevelAsbiepId);
            TopLevelAsbiepSummaryRecord basedTopLevelAsbiep =
                    (sourceTopLevelAsbiep != null)
                            ? topLevelAsbiepQuery.getTopLevelAsbiepSummary(sourceTopLevelAsbiep.basedTopLevelAsbiepId())
                            : null;
            if (basedTopLevelAsbiep != null && associatedTopLevelAsbiepIds.contains(basedTopLevelAsbiep.topLevelAsbiepId())) {
                String edgeKey = sourceTopLevelAsbiepId + "|INHERITS_FROM|" + basedTopLevelAsbiep.topLevelAsbiepId();
                edgeMap.putIfAbsent(edgeKey, new BieStateTransitionEdge(
                        sourceTopLevelAsbiepId,
                        basedTopLevelAsbiep.topLevelAsbiepId(),
                        BieStateTransitionDependency.INHERITS_FROM));
                String reverseEdgeKey = basedTopLevelAsbiep.topLevelAsbiepId() + "|IS_A_BASED_OF|" + sourceTopLevelAsbiepId;
                edgeMap.putIfAbsent(reverseEdgeKey, new BieStateTransitionEdge(
                        basedTopLevelAsbiep.topLevelAsbiepId(),
                        sourceTopLevelAsbiepId,
                        BieStateTransitionDependency.IS_A_BASED_OF));
            }
        }

        return new ArrayList<>(edgeMap.values());
    }

    /**
     * Evaluates every registered rule across every directed dependency edge in
     * the visible graph.
     */
    private void applyStateTransitionRules(List<BieStateDependencyTarget> targets,
                                          Map<TopLevelAsbiepId, BieState> futureStateMap,
                                          Map<TopLevelAsbiepId, TopLevelAsbiepSummaryRecord> topLevelAsbiepMap,
                                          List<BieStateTransitionEdge> stateTransitionEdges,
                                          Map<TopLevelAsbiepId, Integer> edgeDistanceMap) {
        if (targets == null || targets.isEmpty() || stateTransitionRules.isEmpty()) {
            return;
        }

        Map<TopLevelAsbiepId, BieStateDependencyTarget> targetMap = targets.stream()
                .collect(LinkedHashMap::new,
                        (map, target) -> map.put(target.getTopLevelAsbiepId(), target),
                        LinkedHashMap::putAll);

        List<BieStateTransitionEdge> sortedEdges = new ArrayList<>(stateTransitionEdges);
        sortedEdges.sort(Comparator
                .comparingInt((BieStateTransitionEdge edge) ->
                        edgeDistanceMap.getOrDefault(edge.sourceTopLevelAsbiepId(), Integer.MAX_VALUE) +
                                edgeDistanceMap.getOrDefault(edge.targetTopLevelAsbiepId(), Integer.MAX_VALUE))
                .thenComparingInt(edge ->
                        Math.min(edgeDistanceMap.getOrDefault(edge.sourceTopLevelAsbiepId(), Integer.MAX_VALUE),
                                edgeDistanceMap.getOrDefault(edge.targetTopLevelAsbiepId(), Integer.MAX_VALUE))));

        for (BieStateTransitionEdge edge : sortedEdges) {
            TopLevelAsbiepSummaryRecord source = topLevelAsbiepMap.get(edge.sourceTopLevelAsbiepId());
            TopLevelAsbiepSummaryRecord target = topLevelAsbiepMap.get(edge.targetTopLevelAsbiepId());
            if (source == null || target == null) {
                continue;
            }

            BieState sourceFutureState = futureStateMap.get(edge.sourceTopLevelAsbiepId());
            BieState targetFutureState = futureStateMap.get(edge.targetTopLevelAsbiepId());
            if (sourceFutureState == null || Objects.equals(source.state(), sourceFutureState)) {
                continue;
            }

            BieStateDependencyTarget affectedRow = targetMap.get(edge.sourceTopLevelAsbiepId());
            if (affectedRow == null) {
                affectedRow = targetMap.get(edge.targetTopLevelAsbiepId());
            }
            if (affectedRow == null || !affectedRow.isStateTransitionAllowed()) {
                continue;
            }

            for (BieStateTransitionRule stateTransitionRule : stateTransitionRules) {
                try {
                    stateTransitionRule.validate(
                            source, target, edge.dependency(), sourceFutureState, targetFutureState);
                } catch (BieStateTransitionRuleViolationException e) {
                    affectedRow.setStateTransitionAllowed(false);
                    affectedRow.setStateTransitionMessage(
                            buildStateTransitionMessage(edge.dependency(), sourceFutureState, target));
                    break;
                }
            }
        }
    }

    /**
     * Produces the user-facing validation text for one violated dependency
     * edge. Rules themselves only report that an edge is invalid; wording stays
     * centralized here so the UI can remain consistent across rules.
     */
    private String buildStateTransitionMessage(BieStateTransitionDependency dependency,
                                               BieState sourceFutureState,
                                               TopLevelAsbiepSummaryRecord target) {
        String targetName = toName(target);
        return switch (dependency) {
            case REUSES -> "Reused BIE '" + targetName + "' must also be moved to '" + sourceFutureState + "'.";
            case REUSED_BY -> "Reusing BIE '" + targetName + "' must also be moved to '" + sourceFutureState + "'.";
            case INHERITS_FROM -> "Base BIE '" + targetName + "' must also be moved to '" + sourceFutureState + "'.";
            case IS_A_BASED_OF -> "Inherited BIE '" + targetName + "' must also be moved to '" + sourceFutureState + "'.";
        };
    }

    private String toName(TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        return hasLength(topLevelAsbiep.displayName()) ?
                topLevelAsbiep.displayName() :
                topLevelAsbiep.propertyTerm();
    }

    /**
     * Returns whether a dependency row would actually change state if it
     * participates in the requested transition.
     */
    private boolean shouldApplyDependencyStateChange(BieState currentState, BieState nextState) {
        if (currentState == null || nextState == null) {
            return false;
        }
        if (currentState == nextState) {
            return false;
        }
        return currentState != BieState.Production;
    }

    /**
     * Returns the prerequisite state a dependency must already have before it
     * can be promoted into the requested next state.
     */
    private BieState requiredState(BieState state) {
        if (state == BieState.Production) {
            return BieState.QA;
        }
        if (state == BieState.QA) {
            return BieState.WIP;
        }
        return state;
    }

    private BieEditTreeController getTreeController(ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId) {
        DefaultBieEditTreeController bieEditTreeController =
                applicationContext.getBean(DefaultBieEditTreeController.class);

        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
        TopLevelAsbiepSummaryRecord topLevelAsbiep =
                topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiepId);
        bieEditTreeController.initialize(requester, topLevelAsbiep);

        return bieEditTreeController;
    }

    private void ensureBieRelationshipsForChangingState(ScoreUser requester,
                                                        TopLevelAsbiepId topLevelAsbiepId,
                                                        BieState state) {
        StringBuilder failureMessageBody = new StringBuilder();
        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);

        if (state == BieState.WIP) {
            List<TopLevelAsbiepSummaryRecord> reusedTopLevelAsbiepList =
                    topLevelAsbiepQuery.getReusedTopLevelAsbiepSummaryList(topLevelAsbiepId).stream()
                            .filter(e -> !e.owner().userId().equals(requester.userId()))
                            .filter(e -> e.state().getLevel() > state.getLevel())
                            .collect(Collectors.toList());
            if (!reusedTopLevelAsbiepList.isEmpty()) {
                var source = bieService.selectAsccpPropertyTermAndAsbiepGuidByTopLevelAsbiepId(topLevelAsbiepId);
                failureMessageBody.append("\n---\n[**")
                        .append(source.get(ASCCP.PROPERTY_TERM))
                        .append("**](")
                        .append("/profile_bie/").append(topLevelAsbiepId)
                        .append(") (")
                        .append(source.get(ASBIEP.GUID))
                        .append(") cannot move to ")
                        .append(state)
                        .append(" because the following reusing BIEs:")
                        .append("\n\n");
                for (TopLevelAsbiepSummaryRecord target : reusedTopLevelAsbiepList) {
                    failureMessageBody.append("- [")
                            .append(target.propertyTerm())
                            .append("](")
                            .append("/profile_bie/").append(target.topLevelAsbiepId())
                            .append(") (")
                            .append(target.guid())
                            .append(") - ")
                            .append(target.state()).append(" state")
                            .append("\n");
                }
            }
        } else {
            List<TopLevelAsbiepSummaryRecord> reusingTopLevelAsbiepList =
                    topLevelAsbiepQuery.getReusingTopLevelAsbiepSummaryList(topLevelAsbiepId).stream()
                            .filter(e -> !e.owner().userId().equals(requester.userId()))
                            .filter(e -> e.state().getLevel() < state.getLevel())
                            .collect(Collectors.toList());

            if (!reusingTopLevelAsbiepList.isEmpty()) {
                var source = bieService.selectAsccpPropertyTermAndAsbiepGuidByTopLevelAsbiepId(topLevelAsbiepId);
                failureMessageBody.append("\n---\n[**")
                        .append(source.get(ASCCP.PROPERTY_TERM))
                        .append("**](")
                        .append("/profile_bie/").append(topLevelAsbiepId)
                        .append(") (")
                        .append(source.get(ASBIEP.GUID))
                        .append(") cannot move to ")
                        .append(state)
                        .append(" because the following reused BIEs:")
                        .append("\n\n");
                for (TopLevelAsbiepSummaryRecord target : reusingTopLevelAsbiepList) {
                    failureMessageBody.append("- [")
                            .append(target.propertyTerm())
                            .append("](")
                            .append("/profile_bie/").append(target.topLevelAsbiepId())
                            .append(") (")
                            .append(target.guid())
                            .append(") - ")
                            .append(target.state()).append(" state")
                            .append("\n");
                }
            }
        }

        if (state == BieState.WIP) {
            List<TopLevelAsbiepSummaryRecord> derivedTopLevelAsbiepList =
                    topLevelAsbiepQuery.getDerivedTopLevelAsbiepSummaryList(topLevelAsbiepId).stream()
                            .filter(e -> !e.owner().userId().equals(requester.userId()))
                            .filter(e -> e.state().getLevel() > state.getLevel())
                            .collect(Collectors.toList());
            if (!derivedTopLevelAsbiepList.isEmpty()) {
                var source = bieService.selectAsccpPropertyTermAndAsbiepGuidByTopLevelAsbiepId(topLevelAsbiepId);
                failureMessageBody.append("\n---\n[**")
                        .append(source.get(ASCCP.PROPERTY_TERM))
                        .append("**](")
                        .append("/profile_bie/").append(topLevelAsbiepId)
                        .append(") (")
                        .append(source.get(ASBIEP.GUID))
                        .append(") cannot move to ")
                        .append(state)
                        .append(" because the following inherited BIEs:")
                        .append("\n\n");
                for (TopLevelAsbiepSummaryRecord target : derivedTopLevelAsbiepList) {
                    failureMessageBody.append("- [")
                            .append(target.propertyTerm())
                            .append("](")
                            .append("/profile_bie/").append(target.topLevelAsbiepId())
                            .append(") (")
                            .append(target.guid())
                            .append(") - ")
                            .append(target.state()).append(" state")
                            .append("\n");
                }
            }
        } else {
            TopLevelAsbiepSummaryRecord topLevelAsbiep = topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiepId);
            TopLevelAsbiepSummaryRecord basedTopLevelAsbiep =
                    topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiep.basedTopLevelAsbiepId());

            if (basedTopLevelAsbiep != null &&
                    !requester.userId().equals(basedTopLevelAsbiep.owner().userId()) &&
                    basedTopLevelAsbiep.state().getLevel() < state.getLevel()) {
                var source = bieService.selectAsccpPropertyTermAndAsbiepGuidByTopLevelAsbiepId(topLevelAsbiepId);
                failureMessageBody.append("\n---\n[**")
                        .append(source.get(ASCCP.PROPERTY_TERM))
                        .append("**](")
                        .append("/profile_bie/").append(topLevelAsbiepId)
                        .append(") (")
                        .append(source.get(ASBIEP.GUID))
                        .append(") cannot move to ")
                        .append(state)
                        .append(" because the following base BIE:")
                        .append("\n\n")
                        .append("- [")
                        .append(basedTopLevelAsbiep.propertyTerm())
                        .append("](")
                        .append("/profile_bie/").append(basedTopLevelAsbiep.topLevelAsbiepId())
                        .append(") (")
                        .append(basedTopLevelAsbiep.guid())
                        .append(") - ")
                        .append(basedTopLevelAsbiep.state()).append(" state")
                        .append("\n");
            }
        }

        ensureSameOwnerDependencyTargetsForChangingState(
                requester, topLevelAsbiepId, state, failureMessageBody, topLevelAsbiepQuery);

        if (failureMessageBody.length() > 0) {
            throw new DataAccessForbiddenException(toPlainTextStateFailureMessage(
                    "Failed to update BIE state", failureMessageBody.toString()));
        }
    }

    private void ensureSameOwnerDependencyTargetsForChangingState(ScoreUser requester,
                                                                  TopLevelAsbiepId topLevelAsbiepId,
                                                                  BieState state,
                                                                  StringBuilder failureMessageBody,
                                                                  TopLevelAsbiepQueryRepository topLevelAsbiepQuery) {
        if (state == null || state.getLevel() <= BieState.WIP.getLevel()) {
            return;
        }

        int requiredLevel = state.getLevel() - 1;
        Set<TopLevelAsbiepId> visitedTopLevelAsbiepIds = new HashSet<>();
        Queue<TopLevelAsbiepSummaryRecord> topLevelAsbiepQueue = new LinkedList<>();
        Map<TopLevelAsbiepId, TopLevelAsbiepSummaryRecord> blockingTargetMap = new LinkedHashMap<>();

        TopLevelAsbiepSummaryRecord rootTopLevelAsbiep = topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiepId);
        if (rootTopLevelAsbiep == null) {
            return;
        }
        topLevelAsbiepQueue.offer(rootTopLevelAsbiep);

        while (!topLevelAsbiepQueue.isEmpty()) {
            TopLevelAsbiepSummaryRecord currentTopLevelAsbiep = topLevelAsbiepQueue.poll();
            if (!visitedTopLevelAsbiepIds.add(currentTopLevelAsbiep.topLevelAsbiepId())) {
                continue;
            }

            List<TopLevelAsbiepSummaryRecord> dependencyTargets = new ArrayList<>();
            dependencyTargets.addAll(topLevelAsbiepQuery.getReusingTopLevelAsbiepSummaryList(
                    currentTopLevelAsbiep.topLevelAsbiepId()));
            TopLevelAsbiepSummaryRecord basedTopLevelAsbiep =
                    topLevelAsbiepQuery.getTopLevelAsbiepSummary(currentTopLevelAsbiep.basedTopLevelAsbiepId());
            if (basedTopLevelAsbiep != null) {
                dependencyTargets.add(basedTopLevelAsbiep);
            }

            dependencyTargets.stream()
                    .filter(target -> currentTopLevelAsbiep.owner().userId().equals(target.owner().userId()))
                    .forEach(target -> {
                        topLevelAsbiepQueue.offer(target);
                        if (!topLevelAsbiepId.equals(target.topLevelAsbiepId()) &&
                                shouldApplyDependencyStateChange(target.state(), state) &&
                                target.state().getLevel() < requiredLevel) {
                            blockingTargetMap.putIfAbsent(target.topLevelAsbiepId(), target);
                        }
                    });
        }

        if (!blockingTargetMap.isEmpty()) {
            var source = bieService.selectAsccpPropertyTermAndAsbiepGuidByTopLevelAsbiepId(topLevelAsbiepId);
            failureMessageBody.append("\n---\n[**")
                    .append(source.get(ASCCP.PROPERTY_TERM))
                    .append("**](")
                    .append("/profile_bie/").append(topLevelAsbiepId)
                    .append(") (")
                    .append(source.get(ASBIEP.GUID))
                    .append(") cannot move to ")
                    .append(state)
                    .append(" because the following same-owner reused or inherited BIEs must already be in ")
                    .append(requiredState(state))
                    .append(" state:")
                    .append("\n\n");
            for (TopLevelAsbiepSummaryRecord target : blockingTargetMap.values().stream()
                    .sorted(Comparator.comparing(TopLevelAsbiepSummaryRecord::propertyTerm)
                            .thenComparing(target -> target.guid().value()))
                    .toList()) {
                failureMessageBody.append("- [")
                        .append(target.propertyTerm())
                        .append("](")
                        .append("/profile_bie/").append(target.topLevelAsbiepId())
                        .append(") (")
                        .append(target.guid())
                        .append(") - ")
                        .append(target.state()).append(" state")
                        .append("\n");
            }
        }
    }

    private void ensureDependencySelectionStateChange(ScoreUser requester,
                                                      TopLevelAsbiepId topLevelAsbiepId,
                                                      BieState state,
                                                      Collection<TopLevelAsbiepId> dependencyTopLevelAsbiepIds) {
        ensureDependencySelectionStateChange(
                requester,
                List.of(topLevelAsbiepId),
                state,
                dependencyTopLevelAsbiepIds);
    }

    private void ensureDependencySelectionStateChange(ScoreUser requester,
                                                      Collection<TopLevelAsbiepId> rootTopLevelAsbiepIds,
                                                      BieState state,
                                                      Collection<TopLevelAsbiepId> dependencyTopLevelAsbiepIds) {
        boolean hasInvalidSelection = validateStateDependencies(
                requester, rootTopLevelAsbiepIds, state, dependencyTopLevelAsbiepIds).stream()
                .anyMatch(target -> !target.isStateTransitionAllowed() || target.isSelectionConflict());
        if (hasInvalidSelection) {
            String subject = (rootTopLevelAsbiepIds != null && rootTopLevelAsbiepIds.size() > 1)
                    ? "Selected BIEs cannot move to '"
                    : "This BIE cannot move to '";
            throw new DataAccessForbiddenException("Failed to update BIE state\n" +
                    subject + state + "'. Resolve the conflicting records to continue.");
        }
    }

    private String toPlainTextStateFailureMessage(String subject, String messageBody) {
        String plainTextMessageBody = messageBody
                .replace("\n---\n", "\n\n")
                .replaceAll("\\*\\*(.*?)\\*\\*", "$1")
                .replaceAll("\\[(.*?)\\]\\([^)]*\\)", "$1");
        return subject + "\n" + plainTextMessageBody.trim();
    }
}
