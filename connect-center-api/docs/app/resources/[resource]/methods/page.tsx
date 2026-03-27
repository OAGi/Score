import { redirect } from 'next/navigation';

const DEFAULT_METHOD_BY_RESOURCE: Record<string, string> = {
  accounts: 'list',
  libraries: 'list',
  releases: 'list',
  namespaces: 'list',
  core_components: 'list',
  business_information_entities: 'get_top_level_asbiep',
  data_types: 'list',
  xbts: 'retrieve',
  code_lists: 'list',
  agency_id_lists: 'list',
  tags: 'list',
  context_categories: 'list',
  context_schemes: 'list',
  business_contexts: 'list',
};

export function generateStaticParams(): Array<{ resource: string }> {
  return Object.keys(DEFAULT_METHOD_BY_RESOURCE).map((resource) => ({ resource }));
}

export default async function MethodsIndexPage({
  params,
}: {
  params: Promise<{ resource: string }>;
}) {
  const { resource } = await params;
  const method = DEFAULT_METHOD_BY_RESOURCE[resource];
  if (!method) {
    redirect('/overview');
  }
  redirect(`/resources/${resource}/methods/${method}`);
}
