import {UserToken} from '../../authentication/domain/auth';
import {loadProperty, saveProperty} from '../../common/utility';

/** A type of core component that a list shows or leaves out. */
export interface CcFilterType {
  name: string;
  selected: boolean;
}

const CC_FILTER_TYPES_KEY = 'FilterTypes-CoreComponentPage';
const DT_FILTER_TYPES_KEY = 'FilterTypes-DataTypePage';

/** The types that the core component list shows until the user chooses. */
export function defaultCcFilterTypes(): CcFilterType[] {
  return [
    {name: 'ACC', selected: true},
    {name: 'ASCCP', selected: true},
    {name: 'BCCP', selected: true},
  ];
}

/** The types that the data type list shows until the user chooses. */
export function defaultDtFilterTypes(): CcFilterType[] {
  return [
    {name: 'CDT', selected: true},
    {name: 'BDT', selected: true},
  ];
}

/** The types that the user chose; the default ones if there are none, or if the default types have changed since. */
function loadFilterTypes(userToken: UserToken, key: string, defaults: CcFilterType[]): CcFilterType[] {
  const types: CcFilterType[] = JSON.parse(loadProperty(userToken, key, JSON.stringify(defaults)));
  return types.length === defaults.length ? types : defaults;
}

export function loadCcFilterTypes(userToken: UserToken): CcFilterType[] {
  return loadFilterTypes(userToken, CC_FILTER_TYPES_KEY, defaultCcFilterTypes());
}

export function saveCcFilterTypes(userToken: UserToken, types: CcFilterType[]) {
  saveProperty(userToken, CC_FILTER_TYPES_KEY, JSON.stringify(types));
}

export function loadDtFilterTypes(userToken: UserToken): CcFilterType[] {
  return loadFilterTypes(userToken, DT_FILTER_TYPES_KEY, defaultDtFilterTypes());
}
