import { Injectable, HttpException, HttpStatus, ServiceUnavailableException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { map } from 'rxjs/operators';
import { catchError, firstValueFrom, Observable } from 'rxjs';
import { HttpService } from '@nestjs/axios';
import { AxiosError, AxiosResponse } from 'axios';
import { ComponentsService } from 'src/components/components.service';

@Injectable()
export class BieService {
    cachePath = "cache";
    backendServer = this.configService.get<string>('backend_server');
    metadataUrl = this.backendServer + this.configService.get<string>('bie_backend_endpoint');
    schemaUrlXsd = this.backendServer + this.configService.get<string>('components_xsd_backend_endpoint');
    localServer = this.configService.get<string>('local_server') + ':' + this.configService.get<string>('api_port');
    fs = require('fs');
    path = require('path');

    constructor(
        private readonly httpService: HttpService, private configService: ConfigService, private componentService: ComponentsService
    ) { }


    getFromCache(uuid: string, schemaType: string): string {
        var schema;
        try {
            const cacheFile = this.path.join(this.cachePath, schemaType, uuid + '.' + schemaType);
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

    writeToCache(uuid: string, schema: string, schemaType: string) {
        const cachePath = this.path.join(this.cachePath, schemaType);
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

    async mergeComponentTags(bieMetadata): Promise<JSON> {
        const componentsUrl = this.localServer + '/api/components';

        const merged = await firstValueFrom(
            this.httpService.get(componentsUrl,
                {
                    params:
                        { types: 'asccp' }
                }
            )
                .pipe(map(response => {
                    let components = response.data;

                    for (var i = 0; i < bieMetadata.length; i++) {
                        console.log(bieMetadata[i].den);
                        for (var j = 0; j < components.length; j++) {
                            if (bieMetadata[i].den == components[j].den) {
                                bieMetadata[i].componentTags = components[j].tagList;
                                break;
                            }
                        }

                    }
                    console.log(bieMetadata);
                    return bieMetadata;
                }))

                .pipe(
                    catchError((error: AxiosError) => {
                        console.log(error);
                        throw new HttpException('Error getting top level id', error.response.status);
                    }),
                )
        );

        return merged;

    }


    async getTopLevelId(uuid: string): Promise<bigint> {
        const bieUrl = this.localServer + '/api/bie';

        const topLevelAsbiepId = await firstValueFrom(
            this.httpService.get(bieUrl)

                .pipe(map(response => {
                    let bie = response.data;
                    for (var i = 0; i < bie.length; i++) {
                        if (bie[i].guid == uuid) {
                            console.log('mapped ' + uuid + ' ' + bie[i].topLevelAsbiepId);
                            return bie[i].topLevelAsbiepId;
                        }
                    }
                }))

                .pipe(
                    catchError((error: AxiosError) => {
                        console.log(error);
                        throw new HttpException('Error getting top level id', error.response.status);
                    }),
                )

        );
        return topLevelAsbiepId;
    }


    getBieSchema(uuid: string, schemaType = 'xsd') {

        var cachedSchema = this.getFromCache(uuid, schemaType);
        var cached = false;
        if (cachedSchema) {
            cached = true;
        }
        console.log("cached " + cached)

        const standalone = this.getTopLevelId(uuid)
            .then(resp => {
                var topLevelAsbiepId = resp;
                console.log("toplevelasbiepid" + topLevelAsbiepId);
                if (topLevelAsbiepId) {

                    const standaloneUrl = this.schemaUrlXsd;

                    const data =
                        this.httpService
                            .get
                            (standaloneUrl,
                                {
                                    params:
                                        { topLevelAsbiepId: topLevelAsbiepId }
                                }
                            )
                            .pipe(map(response => {
                                let standalone = response.data;
                                console.log(standalone);
                                const fs = require('fs');
                                console.log(topLevelAsbiepId);
                                this.writeToCache(uuid, standalone.toString(), schemaType);
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
                                        // `error.request` is an instance of XMLHttpRequest in the browser and an instance of
                                        // http.ClientRequest in node.js
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
            });

        return standalone;
    }


    async getAllBieMetadata(releaseVersion?: string, businessContexts?: string, den?: string, states?: string): Promise<JSON> {

        var pageSize = this.configService.get('bie_metadata_page_size');

        var releaseId;

        if (releaseVersion) {
            releaseId = await this.componentService.getReleaseId(releaseVersion);
        }
        else {
            releaseId =
                await firstValueFrom(
                    this.httpService.get(this.localServer + '/api/components/latest_release')
                        .pipe(map(response => {
                            return response.data;
                        })));

        }
        console.log("using releaseid" + releaseId);

        console.log("retrieving bie metadata");

        let axiosConfig = {
            params:
            {
                pageSize: pageSize,
                pageIndex: 0,
                sortActive: 'den',
                sortDirection: 'asc',
                businessContext: businessContexts,
                den: den,
                states: states,
                releaseIds: releaseId
            }
            ,
            validateStatus: function (status) {
                return status == 200; // Resolve only if the status code is 200
            }
        };

        const data =
            await firstValueFrom(
                this.httpService.get
                    (this.metadataUrl, axiosConfig)
                    .pipe(map(response => {
                        let bieList = response.data.list;
                        if (bieList) {
                            for (var i = 0; i < bieList.length; i++) {
                                bieList[i].branch = bieList[i].releaseNum;
                                delete bieList[i].releaseNum;
                            }

                            return this.mergeComponentTags(bieList);
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
                                if (error.response.status == 404) {
                                    throw new HttpException("Internal Endpoint Not Found", HttpStatus.SERVICE_UNAVAILABLE);
                                }
                                else
                                    throw new HttpException(error.message, error.response.status);
                            } else if (error.request) {
                                // The request was made but no response was received
                                // `error.request` is an instance of XMLHttpRequest in the browser and an instance of
                                // http.ClientRequest in node.js
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

}

