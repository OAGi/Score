export declare class Settings {
    private _columns;
    private _sortParams;
    private _sortDirs;
    private _key;
    constructor(key: string);
    load(): void;
    save(): void;
    get columns(): Array<{
        id: string;
        name: string;
        isActive?: boolean;
    }>;
    get sortParams(): string[];
    get sortDirs(): string[];
    get key(): String;
    set columns(columns: Array<{
        id: string;
        name: string;
        isActive?: boolean;
    }>);
    set sortParams(sortParams: string[]);
    set sortDirs(sortDirs: string[]);
}
//# sourceMappingURL=utils.d.ts.map