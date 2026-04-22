import { notFound } from 'next/navigation';

type SectionGroup = {
  title: string;
  items: string[];
};

type ResourceConcept = {
  title: string;
  overview: string[];
  endpointUsage: string[] | SectionGroup[];
};

function isGroupedSectionItems(items: string[] | SectionGroup[]): items is SectionGroup[] {
  return items.length > 0 && typeof items[0] !== 'string';
}

const RESOURCE_CONCEPTS: Record<string, ResourceConcept> = {
  accounts: {
    title: 'Account',
    overview: [
      'Accounts represent application users. They carry login identity, display identity, organization membership, and role flags that determine what the caller can read or change elsewhere in connectCenter.',
      'This resource is mainly operational. You use it to understand who the current caller is and, when needed, to look up another user before ownership transfer or permission-sensitive actions.',
    ],
    endpointUsage: [
      'Use `who_am_i` first when you need to confirm the authenticated caller before performing owner-only operations.',
      'Use `get_users` to search for target users by login ID, username, organization, or enabled/admin flags.',
      'Use the returned `user_id` values as inputs for ownership-transfer operations in other resources, such as top-level BIE ownership transfer.',
    ],
  },
  libraries: {
    title: 'Library',
    overview: [
      'A Library is the top-level container for model content. It defines the overall business domain or standard family that releases, namespaces, core components, and related artifacts belong to.',
      'Most read workflows begin by choosing a library, then enumerating its releases, and only after that drilling into release-scoped resources such as core components, data types, code lists, and agency ID lists.',
    ],
    endpointUsage: [
      'Use `get_libraries` to browse available libraries and identify the `library_id` you want to work with.',
      'Use `get_library` when you need the details of one specific library, such as its organization, domain, state, whether it is marked as default, and the direct dependencies assigned to its working release.',
      'Use the add/remove release-dependency operations when you want to manage the direct dependencies assigned to that library\'s working release one release at a time.',
      'Pass the selected `library_id` into `get_releases` and `get_namespaces` to continue navigation into release-scoped resources.',
    ],
  },
  releases: {
    title: 'Release',
    overview: [
      'A Release is a versioned publication branch inside a library. It is the main scoping key for most catalog-like resources in connectCenter, including core components, data types, code lists, and agency ID lists.',
      'Release state matters. Read operations commonly target published or production releases, while authoring and profile-building workflows often depend on a working release in the same library.',
    ],
    endpointUsage: [
      'Use `get_releases` with a `library_id` to find the release number and state you want to inspect or build against.',
      'Use `get_release` when you already know the `release_id` and need its precise metadata.',
      'Use `get_working_release` with a `library_id` when you need the library\'s `Working` release directly, especially for Developer authoring flows.',
      'Reuse the selected `release_id` across downstream endpoints such as `get_core_components`, `get_data_types`, `get_code_lists`, and `get_agency_id_lists`.',
    ],
  },
  namespaces: {
    title: 'Namespace',
    overview: [
      'Namespaces define stable identifiers such as URI and prefix for model serialization and publication. They are library-scoped and can be marked as standard namespaces.',
      'Namespaces are not usually the first resource you browse, but they become important when you need to understand serialization targets, ownership of prefixes, or standard versus non-standard namespace usage.',
    ],
    endpointUsage: [
      'Use `get_namespaces` with a `library_id` to list namespaces available in that library.',
      'Use `get_namespace` to retrieve one namespace by `namespace_id` when you need its exact URI, prefix, or standard flag.',
      'Use namespace details when reconciling schema publication behavior or when other resources refer to namespace IDs indirectly.',
    ],
  },
  core_components: {
    title: 'Core Component',
    overview: [
      'Core Components, or CCs, are the canonical, context-independent model artifacts in connectCenter. They include ACCs for complex structures, ASCCPs and BCCPs for reusable properties, and the relationships that connect them.',
      'BIEs are profiles of these canonical components. In practice, CC endpoints are used both directly for model inspection and indirectly when a BIE response gives you manifest IDs that you need to resolve.',
    ],
    endpointUsage: [
      {
        title: 'General',
        items: [
          'Use `get_core_components` to search the release for ACC, ASCCP, or BCCP records by DEN, tag, or component type.',
          'Use `get_acc`, `get_asccp`, and `get_bccp` when you already have a manifest ID and need full component details.',
          'When traversing a BIE, follow manifest IDs returned from ASBIEP, ABIE, ASBIE, or BBIE structures into these CC endpoints for the canonical definition behind the profile.',
        ],
      },
      {
        title: 'ACC',
        items: [
          'Use `create_acc` to create a new ACC in the release branch allowed for your role: `Working` for developers, non-`Working` for end-users, with admins following the matching effective-role path.',
          'Use `update_acc` to change mutable ACC fields such as object class term, definition, namespace, component type, abstract flag, deprecation, or `based_acc_manifest_id` while the ACC is in `WIP`.',
          'Use `transfer_acc_ownership` to hand a `WIP` ACC to another user. Only the current owner or an admin can transfer it, and the transfer also updates ownership on the ACC-owned ASCC and BCC rows behind the current manifest.',
          'Use `update_acc` with `based_acc_manifest_id` to assign or clear a base ACC while the ACC is in `WIP`. Base ACC updates must stay within the same release and cannot introduce inherited ASCCP or BCCP conflicts.',
          'Use `add_tags_to_acc` and `remove_tags_from_acc` to attach or detach ACC tags while the ACC is in `WIP`. These tag updates follow the same owner or admin rules as other mutable ACC edits.',
          'Use `change_acc_state` to move an ACC through its lifecycle state transitions, including `WIP` to `Deleted` when you want to mark it for deletion.',
          'Use `revise_amend_acc` to create a new editable ACC revision from a stable ACC: `Published` in `Working` for developers, or `Production` in a non-`Working` release for end-users.',
          'Use `cancel_acc` to abandon the current `WIP` ACC revision and restore the previous stable revision on the same allowed branch and role family.',
          'Use `discard_acc` only after the ACC is already in `Deleted`. Discard permanently removes the ACC from the database and cannot be undone.',
          'Use `add_ascc_to_acc` and `add_bcc_to_acc` to add a reusable ASCCP or BCCP into an ACC sequence, or reposition the existing relationship if that property is already present.',
          'Use `remove_ascc` and `remove_bcc` to delete existing ACC child relationships when you no longer want that ASCCP or BCCP in the owning ACC.',
          'Use `update_ascc` and `update_bcc` to change mutable relationship-level fields on existing ACC children, such as cardinality, definition, deprecation, entity type, nillable, or value constraints.',
          'Use `reorder_ascc_in_acc` and `reorder_bcc_in_acc` when you need a position-only change for an existing ACC relationship without changing which ASCCP or BCCP it targets.',
        ],
      },
      {
        title: 'ASCCP',
        items: [
          'Use `create_asccp` to create a new ASCCP in the release branch allowed for your role: `Working` for developers, non-`Working` for end-users, with admins following the matching effective-role path. New ASCCPs require a semantically meaningful `property_term` for the association characteristic.',
          'Use `update_asccp` to change mutable ASCCP fields while the ASCCP is in `WIP`.',
          'Use `transfer_asccp_ownership` to hand a `WIP` ASCCP to another user. Only the current owner or an admin can transfer it.',
          'Use `change_asccp_role_of_acc` to update the ACC that supplies the ASCCP role while the ASCCP is in `WIP`.',
          'Use `add_asccp_tags` and `remove_asccp_tags` to attach or detach ASCCP tags while the ASCCP is in `WIP`. These tag updates follow the same owner or admin rules as other mutable ASCCP edits.',
          'Use `change_asccp_state` to move an ASCCP through its lifecycle state transitions, including `WIP` to `Deleted` when you want to mark it for deletion.',
          'Use `revise_asccp` to create a new editable ASCCP revision from a stable ASCCP: `Published` in `Working` for developers, or `Production` in a non-`Working` release for end-users.',
          'Use `cancel_asccp` to abandon the current `WIP` ASCCP revision and restore the previous stable revision on the same allowed branch and role family.',
          'Use `discard_asccp` only after the ASCCP is already in `Deleted`. Discard permanently removes the ASCCP from the database and cannot be undone.',
        ],
      },
      {
        title: 'BCCP',
        items: [
          'Use `create_bccp` to create a new BCCP in the release branch allowed for your role: `Working` for developers, non-`Working` for end-users, with admins following the matching effective-role path.',
          'Use `update_bccp` to change mutable BCCP fields while the BCCP is in `WIP`.',
          'Use `transfer_bccp_ownership` to hand a `WIP` BCCP to another user. Only the current owner or an admin can transfer it.',
          'Use `change_bccp_bdt` to assign or change the BDT used by a BCCP while the BCCP is in `WIP`.',
          'Use `add_bccp_tags` and `remove_bccp_tags` to attach or detach BCCP tags while the BCCP is in `WIP`. These tag updates follow the same owner or admin rules as other mutable BCCP edits.',
          'Use `change_bccp_state` to move a BCCP through its lifecycle state transitions, including `WIP` to `Deleted` when you want to mark it for deletion.',
          'Use `revise_bccp` to create a new editable BCCP revision from a stable BCCP: `Published` in `Working` for developers, or `Production` in a non-`Working` release for end-users.',
          'Use `cancel_bccp` to abandon the current `WIP` BCCP revision and restore the previous stable revision on the same allowed branch and role family.',
          'Use `discard_bccp` only after the BCCP is already in `Deleted`. Discard permanently removes the BCCP from the database and cannot be undone.',
        ],
      },
    ],
  },
  business_information_entities: {
    title: 'Business Information Entity',
    overview: [
      'A Business Information Entity, or BIE, is a profile of a canonical Core Component for a particular business context. BIEs simplify and constrain the canonical model so that business analysts and integration developers can work with a practical message structure.',
      'Top-level BIEs are always associated with one or more business contexts. Inside a BIE tree, ASBIEs represent complex business characteristics, BBIEs represent value-carrying properties, and BBIE supplementary components represent the applied data-type attributes under those values.',
    ],
    endpointUsage: [
      'Use `get_top_level_asbiep_list` to find candidate BIEs, then `get_top_level_asbiep` to open the root tree of one BIE.',
      'From the root tree, inspect `relationships`. If a child already has `asbie_id` or `bbie_id`, call `get_asbie_by_asbie_id` or `get_bbie_by_bbie_id`. If not, use the `based_*_manifest_id` variants to inspect what could be profiled next.',
      'Use `create_top_level_asbiep`, `create_asbie`, `create_bbie`, and `create_bbie_sc` to profile new content, then use the corresponding update endpoints to refine cardinality, value restrictions, and documentation fields.',
      'Use `assign_biz_ctx_to_top_level_asbiep`, `unassign_biz_ctx_from_top_level_asbiep`, `reuse_top_level_asbiep`, `remove_reused_top_level_asbiep`, and ownership-transfer endpoints to manage lifecycle and reuse relationships around the BIE.',
    ],
  },
  data_types: {
    title: 'Data Type',
    overview: [
      'Data Types define the value domain and supplementary-component structure used by value-carrying properties. In connectCenter, they are exposed directly because they matter to both core-component modeling and BIE-level value restriction.',
      'A data type may point to primitive restrictions, code lists, agency ID lists, and supplementary components. BBIE and BBIE_SC nodes in BIEs are practical applications of these type definitions.',
    ],
    endpointUsage: [
      'Use `get_data_types` with a `release_id` to browse available types by DEN, representation term, or qualifier.',
      'Use `create_dt` to create a new DT from an existing base DT in the release branch allowed for your role: `Working` for developers, non-`Working` for end-users, with admins following the matching effective-role path. Create requests can also apply initial primitive changes with `default_primitive`, `add_primitives`, and `remove_primitives` instead of requiring a second call.',
      'Use `update_dt` to change mutable DT fields such as qualifier, six-digit ID, definition, definition source, content component definition, namespace, deprecation, the default primitive restriction, or primitive membership changes through `add_primitives` and `remove_primitives` while the DT is in `WIP`.',
      'Use `transfer_dt_ownership` to hand a `WIP` DT to another user. Only the current owner or an admin can transfer it, and the transfer also updates ownership on the DT supplementary components under the current DT manifest.',
      'Use `create_dt_sc` to append a new DT supplementary component under a `WIP` DT. Create requests now accept the same mutable fields exposed by `update_dt_sc`, including primitive overrides and value constraints, and they require both `property_term` and `representation_term` up front. New DT_SCs are propagated to inherited DTs.',
      'Use `update_dt_sc` to change mutable DT supplementary-component fields such as property term, representation term, cardinality, definition, value constraint, deprecation, the default primitive restriction, or primitive membership changes through `add_primitives` and `remove_primitives` while the owner DT is in `WIP`. If you change `representation_term`, use a CDT data type term such as `Amount`, `Code`, or `Text`; that change resets the DT_SC primitive rows to the default primitive set for the selected term. For cardinality, use `Prohibited` for `0..0`, `Optional` for `0..1`, and `Required` for `1..1`.',
      'Use `delete_dt_sc` to remove a DT supplementary component while the owner DT is in `WIP`. DT_SC deletion also removes inherited copies and is blocked when BIE supplementary components still reference it.',
      'Use `add_dt_tags` and `remove_dt_tags` to attach or detach DT tags while the DT is in `WIP`. These tag updates follow the same owner or admin rules as other mutable DT edits.',
      'Use `change_dt_state` to move a DT through its lifecycle state transitions, including `WIP` to `Deleted` when you want to mark it for deletion.',
      'Use `revise_dt` to create a new editable DT revision from a stable DT: `Published` in `Working` for developers, or `Production` in a non-`Working` release for end-users.',
      'Use `cancel_dt` to abandon the current `WIP` DT revision and restore the previous stable revision on the same allowed branch and role family.',
      'Use `discard_dt` only after the DT is already in `Deleted`. Discard permanently removes the DT from the database and cannot be undone.',
      'Use `get_data_type` when you already have a `dt_manifest_id` and need the full type definition.',
      'Follow data-type references from BCCPs, BBIEs, or BBIE supplementary components when you need to understand the default value domain or supplementary-component structure.',
    ],
  },
  xbts: {
    title: 'XBT',
    overview: [
      'XBT stands for XML Built-in Type. It is the primitive layer used by data types and BIE value restrictions, with mappings into XML Schema, JSON Schema, OpenAPI, and Avro representations.',
      'You usually reach XBTs indirectly from a data type, BBIE, or BBIE supplementary component when a primitive restriction has been selected or overridden.',
    ],
    endpointUsage: [
      'Use `get_xbt` with an `xbt_manifest_id` to inspect the canonical primitive definition and its schema mappings.',
      'Use XBT details to understand how a primitive restriction will serialize across supported schema targets.',
      'When a BBIE or BBIE_SC response includes `xbt_manifest_id`, use this endpoint to confirm the exact primitive selected.',
    ],
  },
  code_lists: {
    title: 'Code List',
    overview: [
      'A Code List is a controlled set of values used as a value domain for model content. While code lists are not a native CCS registry class, connectCenter manages them alongside core-component artifacts because they are essential to data typing and standard publication.',
      'In practice, code lists appear both as standalone managed resources and as primitive restrictions chosen for BBIE or BBIE supplementary-component value domains.',
    ],
    endpointUsage: [
      'Use `get_code_lists` with a `release_id` to find code lists by name, list ID, version, or definition text.',
      'Use `get_code_list` when you already have a `code_list_manifest_id` and need the details of one specific code list.',
      'Use `retrieve_code_list_value` when you already have a code list value manifest ID and want one value payload. The method page also shows the direct `/code-list-values/{code_list_value_manifest_id}` alternative endpoint.',
      'Use `create_code_list` to create a new code list in the release branch allowed for your role, optionally deriving it from an existing base code list. If `version_id` is omitted, creation uses the base code list version when a base is supplied or `1` otherwise; if `list_id` is omitted, the service generates one.',
      'Use `update_code_list` to change mutable code list fields such as name, version ID, list ID, agency ID list value, definition, namespace, remark, deprecation, or extensibility while the code list is in `WIP`.',
      'Use `create_code_list_value`, `update_code_list_value`, and `delete_code_list_value` when you want explicit value-level changes under a `WIP` code list.',
      'Use `transfer_code_list_ownership` to hand a `WIP` code list to another user. Only the current owner or an admin can transfer it.',
      'Use `change_code_list_state` to move a code list through its lifecycle state transitions, including `WIP` to `Deleted` when you want to mark it for deletion.',
      'Use `revise_code_list` to create a new editable code list revision from a stable code list: `Published` in `Working` for developers, or `Production` in a non-`Working` release for end-users.',
      'Use `cancel_code_list` to abandon the current `WIP` code list revision and restore the previous stable revision on the same allowed branch and role family.',
      'Use `discard_code_list` only after the code list is already in `Deleted`. Discard permanently removes the code list and its direct records from the database and cannot be undone.',
      'Use returned manifest IDs in BBIE or BBIE_SC primitive-restriction overrides when you want a value domain backed by a code list instead of an XBT primitive.',
    ],
  },
  agency_id_lists: {
    title: 'Agency ID List',
    overview: [
      'An Agency ID List is a managed list of organization identifiers. In connectCenter it plays the practical role of an agency-identifier value domain, especially where a primitive restriction needs an organization-identification list rather than a generic code list.',
      'Agency ID Lists are related to code list ownership and BIE value restriction. They are not usually browsed in isolation; they are typically inspected when a data type, BBIE, or code list needs to refer to a managing organization or agency identifier domain.',
    ],
    endpointUsage: [
      'Use `get_agency_id_lists` with a `release_id` to browse the available agency identifier lists in that release.',
      'Use `get_agency_id_list` when you already have an `agency_id_list_manifest_id` and need its details.',
      'Use the returned manifest ID as a BBIE or BBIE_SC primitive restriction when the value domain should come from an agency identifier list rather than an XBT or code list.',
    ],
  },
  tags: {
    title: 'Tag',
    overview: [
      'Tags are lightweight labels used to classify and organize artifacts, especially core components. They make catalog browsing more targeted when names alone are too broad.',
      'This resource is intentionally simple: it mainly supports discovery and filtering rather than a complex workflow of its own.',
    ],
    endpointUsage: [
      'Use `get_tags` to list available tags and inspect their names and descriptions.',
      'Apply tag values returned here as filters in `get_core_components` when narrowing a large core-component catalog.',
    ],
  },
  context_categories: {
    title: 'Context Category',
    overview: [
      'A Context Category identifies what a context dimension is about, such as industry, business process, or application area. It is the highest-level concept in context management.',
      'Context Categories exist so that similar context schemes can be grouped under the same business dimension. They are the starting point for the dependency chain that ends in a Business Context, which is required to create a BIE.',
    ],
    endpointUsage: [
      'Use `get_context_categories` to browse existing categories before creating a new one, because reuse is usually preferable to duplication.',
      'Use `create_context_category`, `update_context_category`, and `delete_context_category` to manage the category definitions themselves.',
      'After a category exists, use it as the parent when creating context schemes that define concrete values within that dimension.',
    ],
  },
  context_schemes: {
    title: 'Context Scheme',
    overview: [
      'A Context Scheme defines a reusable set of context values within a specific context category. It captures scheme identity, maintaining agency, version, and the values that can later be selected into Business Contexts.',
      'A Business Context cannot be assembled until at least one Context Scheme exists, because Business Context values are chosen from scheme values.',
    ],
    endpointUsage: [
      'Use `get_context_schemes` to find existing schemes by category, scheme ID, scheme name, agency, or version.',
      'Use `create_context_scheme` and `update_context_scheme` to manage the scheme metadata itself.',
      'Use `create_context_scheme_value`, `update_context_scheme_value`, and `delete_context_scheme_value` to maintain the value set that Business Contexts can draw from.',
    ],
  },
  business_contexts: {
    title: 'Business Context',
    overview: [
      'A Business Context defines the situation in which a BIE should be used. It is expressed as a conjunctive combination of selected context-scheme values, often spanning multiple context categories such as industry, process, and integration type.',
      'In connectCenter, Business Context is the immediate prerequisite for top-level BIE creation. A BIE is not just a tree of fields; it is a profile that is valid for a specific business context.',
    ],
    endpointUsage: [
      'Use `get_business_contexts` to browse reusable contexts before creating a new one.',
      'Use `create_business_context` and `update_business_context` for the Business Context record itself.',
      'Use `create_business_context_value`, `update_business_context_value`, and `delete_business_context_value` to manage the selected context-scheme values that define the context.',
      'Assign Business Contexts to top-level BIEs through the Business Information Entity endpoints once the context is ready.',
    ],
  },
};

export function generateStaticParams(): Array<{ resource: string }> {
  return Object.keys(RESOURCE_CONCEPTS).map((resource) => ({ resource }));
}

function Section({
  title,
  items,
}: {
  title: string;
  items: string[] | SectionGroup[];
}) {
  const groupedItems = isGroupedSectionItems(items) ? items : null;

  return (
    <section className="mt-6">
      <h2 className="text-base font-semibold text-[#111827] dark:text-white">{title}</h2>
      {groupedItems ? (
        <div className="mt-4 space-y-5">
          {groupedItems.map((group) => (
            <div key={group.title}>
              <h3 className="text-sm font-semibold uppercase tracking-[0.12em] text-[#475569] dark:text-[#94a3b8]">
                {group.title}
              </h3>
              <ul className="mt-2 list-disc space-y-2 pl-5 text-sm leading-6 text-[#334155] dark:text-[#cbd5e1]">
                {group.items.map((item) => (
                  <li key={item}>{item}</li>
                ))}
              </ul>
            </div>
          ))}
        </div>
      ) : (
        <ul className="mt-3 list-disc space-y-2 pl-5 text-sm leading-6 text-[#334155] dark:text-[#cbd5e1]">
          {(items as string[]).map((item) => (
            <li key={item}>{item}</li>
          ))}
        </ul>
      )}
    </section>
  );
}

export default async function ResourceConceptPage({
  params,
}: {
  params: Promise<{ resource: string }>;
}) {
  const { resource } = await params;
  const concept = RESOURCE_CONCEPTS[resource];

  if (!concept) {
    notFound();
  }

  return (
    <main className="min-h-full min-w-0 bg-[radial-gradient(circle_at_15%_0%,#eef5ff_0%,#ffffff_58%)] dark:bg-[radial-gradient(circle_at_15%_0%,#0b1220_0%,#000000_58%)]">
      <div className="px-4 py-8 pb-[25vh] md:px-6 lg:px-8">
        <div className="rounded-2xl border border-[#dbe3ec] bg-white/95 p-6 shadow-[0_26px_60px_-42px_rgba(15,23,42,0.55)] dark:border-[#1f2937] dark:bg-black/90 md:p-7">
          <p className="text-xs font-semibold uppercase tracking-[0.16em] text-[#64748b] dark:text-[#94a3b8]">
            Resource Guide
          </p>
          <h1 className="mt-2 text-3xl font-semibold tracking-tight text-[#0f172a] font-title dark:text-white">
            {concept.title}
          </h1>
          <Section title="What This Resource Is" items={concept.overview} />
          <Section title="How To Use The API Endpoints" items={concept.endpointUsage} />
        </div>
      </div>
    </main>
  );
}
