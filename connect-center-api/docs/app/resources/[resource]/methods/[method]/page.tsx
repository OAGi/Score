import { MethodKey } from '@/lib/reference-data';

import { MethodPageClient } from './method-page-client';

const RESOURCE_METHODS: Record<string, MethodKey[]> = {
  accounts: ['list', 'retrieve', 'who_am_i'],
  libraries: ['list', 'retrieve'],
  releases: ['list', 'retrieve'],
  namespaces: ['list', 'retrieve'],
  core_components: ['list', 'get_acc', 'get_asccp', 'get_bccp'],
  business_information_entities: [
    'get_top_level_asbiep_list',
    'get_top_level_asbiep',
    'create_top_level_asbiep',
    'update_top_level_asbiep',
    'update_top_level_asbiep_state',
    'delete_top_level_asbiep',
    'transfer_top_level_asbiep_ownership',
    'assign_biz_ctx_to_top_level_asbiep',
    'unassign_biz_ctx_from_top_level_asbiep',
    'get_asbie_by_asbie_id',
    'get_asbie_by_based_ascc_manifest_id',
    'create_asbie',
    'update_asbie',
    'reuse_top_level_asbiep',
    'remove_reused_top_level_asbiep',
    'get_bbie_by_bbie_id',
    'get_bbie_by_based_bcc_manifest_id',
    'create_bbie',
    'update_bbie',
    'create_bbie_sc',
    'update_bbie_sc',
  ],
  data_types: ['list', 'retrieve'],
  xbts: ['retrieve'],
  code_lists: ['list', 'retrieve'],
  agency_id_lists: ['list', 'retrieve'],
  tags: ['list'],
  context_categories: ['create', 'update', 'delete', 'list', 'retrieve'],
  context_schemes: [
    'create',
    'update',
    'delete',
    'list',
    'retrieve',
    'create_value',
    'update_value',
    'delete_value',
    'retrieve_value_by_id',
  ],
  business_contexts: [
    'create',
    'update',
    'delete',
    'list',
    'retrieve',
    'create_value',
    'update_value',
    'delete_value',
    'retrieve_value_by_id',
  ],
};

export function generateStaticParams(): Array<{ resource: string; method: MethodKey }> {
  return Object.entries(RESOURCE_METHODS).flatMap(([resource, methods]) =>
    methods.map((method) => ({ resource, method })),
  );
}

export default async function MethodPage({
  params,
}: {
  params: Promise<{ resource: string; method: MethodKey }>;
}) {
  const { resource, method } = await params;
  return <MethodPageClient resource={resource} method={method} />;
}
