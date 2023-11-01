import { Injectable, HttpException, HttpStatus, ServiceUnavailableException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { map } from 'rxjs/operators';
import { catchError, firstValueFrom, Observable } from 'rxjs';
import { HttpService } from '@nestjs/axios';
import { AxiosError, AxiosResponse } from 'axios';


@Injectable()
export class ComponentsService {
    cachePath = "cache";
    backendServer = this.configService.get<string>('backend_server');
    metadataUrl = this.backendServer + this.configService.get<string>('components_backend_endpoint');
    schemaUrlXsd = this.backendServer + this.configService.get<string>('components_xsd_backend_endpoint');
    localServer = this.configService.get<string>('local_server') + ':' + this.configService.get<string>('api_port');
    releasesUrl = this.backendServer + this.configService.get<string>('releases_backend_endpoint');
    latestReleaseUrl = this.backendServer + this.configService.get<string>('latest_release_backend_endpoint');
    fs = require('fs');
    path = require('path');

    constructor(
        private readonly httpService: HttpService, private configService: ConfigService
    ) { }


    async getReleases(): Promise<JSON> {
        var releasesUrl = this.configService.get<string>('backend_server')
            + '/ext/releases';
        const releases = await firstValueFrom(
            this.httpService.get(releasesUrl)
                .pipe(map(response => {
                    let releases = response.data;
                    return releases;
                }))
                .pipe(
                    catchError((error: AxiosError) => {
                        console.log(error);
                        throw new HttpException('Could not get releases from Score', error.response.status);
                    }),
                )
        );
        return releases;
    }

    async getReleaseId(releaseVersion?: string): Promise<bigint> {
        var releaseId;
        if (!releaseVersion) {
            releaseId = await firstValueFrom(
                this.httpService.get(this.latestReleaseUrl)
                    .pipe(map(response => {
                        console.log(response.data);
                        return response.data;
                    })));
            console.log(releaseId);
        }
        else {
            console.log("looking up " + releaseVersion);
            releaseId = await firstValueFrom(
                this.httpService.get(this.localServer + '/api/components/releases')

                    .pipe(map(response => {
                        let releases = response.data.records;
                        for (var i = 0; i < releases.length; i++) {
                            const release = releases[i];
                            const releaseNum = release[2];
                            if (releaseNum == releaseVersion) {
                                const releaseId = release[0];
                                console.log('mapped ' + releaseVersion + ' ' + releaseId);
                                return releaseId;
                            }
                        }
                    }))

                    .pipe(
                        catchError((error: AxiosError) => {
                            console.log(error);
                            throw new HttpException('Error getting release id', error.response.status);
                        }),
                    )

            );

        }
        if (!releaseId) {
            throw new HttpException('Release not found', HttpStatus.NOT_FOUND);
        }
        return releaseId;
    }


    async getLatestRelease(): Promise<bigint> {
        const releaseId = await firstValueFrom(
            this.httpService.get(this.localServer + '/api/components/releases')
                .pipe(map(response => {
                    let releaseId = response.data.records[0][0];
                    return releaseId;
                }))

                .pipe(
                    catchError((error: AxiosError) => {
                        console.log(error);
                        throw new HttpException('Error getting latest release id', error.response.status);
                    }),
                )
        );
        return releaseId;
    }


    async getLatestReleaseVersion(): Promise<string> {
        const releaseVersion = await firstValueFrom(
            this.httpService.get(this.localServer + '/api/components/releases')
                .pipe(map(response => {
                    let releaseVersion = response.data.records[0][2];
                    return releaseVersion;
                }))

                .pipe(
                    catchError((error: AxiosError) => {
                        console.log(error);
                        throw new HttpException('Error getting latest release id', error.response.status);
                    }),
                )
        );
        return releaseVersion;
    }


    async getManifestId(uuid: string, releaseVersion: string, componentType: string): Promise<bigint> {
        const manifestId = await firstValueFrom(
            this.httpService.get(this.localServer + '/api/components?release=' + releaseVersion)
                .pipe(map(response => {
                    let components = response.data;
                    for (var i = 0; i < components.length; i++) {
                        const component = components[i];
                        if (component.guid == uuid && component.type == componentType) {
                            console.log('mapped ' + uuid + ' ' + components[i].manifestId);
                            return components[i].manifestId;
                        }
                    }
                }))
                .pipe(
                    catchError((error: AxiosError) => {
                        console.log(error);
                        throw new HttpException('Error getting manifest id', error.response.status);
                    }),
                )
        );
        return manifestId;
    }


    async getStandaloneComponent(uuid: string, schemaType = 'xsd', releaseVer?: string) {
        if (schemaType != 'xsd') { //&& schemaType!='json') { //TODO:  JSON
            throw new HttpException("Unsupported schema type", HttpStatus.BAD_REQUEST);
        }

        var releaseVersion = releaseVer;
        if (!releaseVersion) {
            releaseVersion = await this.getLatestReleaseVersion().then(relVer => { return relVer; });
        }

        var cachedSchema = this.getFromCache(uuid, schemaType, releaseVersion);
        if (cachedSchema) {
            console.log("returning cached schema");
            return cachedSchema;
        }
        else {
            const standalone = this.getManifestId(uuid, releaseVersion, 'ASCCP')
                .then(manifestId => {
                    if (manifestId) {
                        const data =
                            this.httpService
                                .get
                                (this.schemaUrlXsd,
                                    {
                                        params:
                                            { asccpManifestIdList: manifestId }
                                    }
                                )
                                .pipe(map(response => {
                                    let standalone = response.data;
                                    //console.log(standalone);
                                    this.writeToCache(uuid, standalone.toString(), schemaType, releaseVersion);
                                    return standalone;
                                }))
                                .pipe(
                                    catchError((error) => {
                                        if (error.response) {
                                            // The request was made and the server responded with a status code
                                            // that falls out of the range of 2xx
                                            console.log(error.response.data);
                                            console.log(error.response.status);
                                            console.log(error.response.headers);
                                            throw new HttpException(error.message, error.response.status);
                                        } else if (error.request) {
                                            // The request was made but no response was received
                                            console.log(error.request);
                                        } else {
                                            // Something happened in setting up the request that triggered an Error
                                            console.log('Error', error.message);
                                        }
                                        console.log(error.config);
                                        throw new HttpException('Could not retrieve data from Score', HttpStatus.SERVICE_UNAVAILABLE);
                                    }),

                                )

                            ;

                        return data;
                    }
                    else {
                        throw new HttpException('UUID not found or is not ASCCP', HttpStatus.NOT_FOUND);
                    }
                });
            return standalone;
        }

    }


    async getAllComponentsMetadata(tags?: string, releaseVersion?: string): Promise<JSON> {
        var pageSize = this.configService.get('component_metadata_page_size');
        var releaseId;

        if (releaseVersion) {
            releaseId = await this.getReleaseId(releaseVersion);
        }
        else {
            releaseId = await firstValueFrom(
                this.httpService.get(this.localServer + '/api/components/latest_release')
                    .pipe(map(response => {
                        return response.data;
                    })));
        }
        console.log("using releaseid" + releaseId);

        console.log("retrieving component metadata");

        let axiosConfig = {
            params:
            {
                releaseId: releaseId,
                //types: "asccp",
                asccpTypes: "Default",
                pageSize: pageSize,
                pageIndex: 0,
                sortActive: 'den',
                sortDirection: 'asc'
            }
            ,
            validateStatus: function (status) {
                return status == 200; // Resolve only if the status code is 200
            }
        };

        if (tags)
            axiosConfig.params['tags'] = tags;

        const data =
            await firstValueFrom(
                this.httpService.get
                    (this.metadataUrl, axiosConfig)
                    .pipe(map(response => {
                        let components = response.data.list;
                        if (components) {
                            for (var i = 0; i < components.length; i++) {
                                //delete components[i].manifestId;
                                delete components[i].basedManifestId;
                                delete components[i].module;
                                delete components[i].definitionSource;
                                delete components[i].sixDigitId;
                                let tagList = components[i].tagList;
                                if (tagList.length > 0) {
                                    delete components[i].tagList[0].tagId;
                                    delete components[i].tagList[0].textColor;
                                    delete components[i].tagList[0].backgroundColor;
                                }
                            }
                            return components;
                        }
                    }
                    ))
                    .pipe(
                        catchError((error) => {
                            if (error.response) {
                                // The request was made and the server responded with a status code
                                // that falls out of the range of 2xx
                                console.log(error.response.data);
                                console.log(error.response.status);
                                console.log(error.response.headers);
                                throw new HttpException(error.message, error.response.status);
                            } else if (error.request) {
                                // The request was made but no response was received
                                console.log(error.request);
                            } else {
                                // Something happened in setting up the request that triggered an Error
                                console.log('Error', error.message);
                            }
                            console.log(error.config);
                            throw new HttpException('Could not retrieve data from Score', HttpStatus.SERVICE_UNAVAILABLE);
                        }),
                    )
            )
            ;

        return data;
    }

    getFromCache(uuid: string, schemaType: string, releaseVersion: string): string {
        var schema;
        try {
            const cacheFile = this.path.join(this.cachePath, schemaType, releaseVersion, uuid + '.' + schemaType);
            schema = this.fs.readFileSync(cacheFile).toString();
            if (schema) {
                console.log("cache hit " + cacheFile);
            }
        }
        catch (error) {
            console.log("cache miss: " + error);
            return schema;
        }
        return schema;
    }

    writeToCache(uuid: string, schema: string, schemaType: string, releaseVersion: string) {
        const cachePath = this.path.join(this.cachePath, schemaType, releaseVersion);
        const cacheFile = this.path.join(cachePath, uuid + '.' + schemaType);
        if (!this.fs.existsSync(cachePath)) {
            this.fs.mkdirSync(cachePath, { recursive: true });
        }
        this.fs.writeFileSync(cacheFile, schema, (err) => {
            if (err) {
                console.log("Schema cache failed " + cacheFile);
                console.log(err);
            }
            else {
                console.log("Schema written to cache " + cacheFile);
            }
        }
        );
    }

}

