/** The page of a tree node that "Open in new tab" shows; a Data Type has its own route, an SC has no page. */
export function ccNodeUrl(node: { type: string, manifestId: any }): string | undefined {
  const type = node.type.toLowerCase();
  switch (type) {
    case 'dt':
      return '/data_type/' + node.manifestId;
    case 'dt_sc':
      return undefined;
    default:
      return '/core_component/' + type + '/' + node.manifestId;
  }
}
