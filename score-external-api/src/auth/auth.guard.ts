import { Injectable, CanActivate, ExecutionContext, HttpStatus, HttpException } from '@nestjs/common';
import { Observable, catchError, firstValueFrom, map } from 'rxjs';
import { ConfigService } from '@nestjs/config';
import { HttpService } from '@nestjs/axios';

@Injectable()
export class AuthGuard implements CanActivate {

    constructor(
        private readonly httpService: HttpService, private configService: ConfigService
    ) { }

    authUrl = this.configService.get<string>('backend_server') + this.configService.get<string>('auth_backend_endpoint');

    canActivate(
        context: ExecutionContext,
    ): boolean | Promise<boolean> | Observable<boolean> {
        const request = context.switchToHttp().getRequest();
        const token = request.headers['authorization'];

        if (token) {
            this.httpService.axiosRef.defaults.headers.common['authorization'] =
                token;
        }
        else {
            delete this.httpService.axiosRef.defaults.headers.common["authorization"];
        }
        var authenticated = this.authenticate(request).then(auth => { return auth; });

        if (!authenticated) {
            throw new HttpException("Not Authorized", HttpStatus.UNAUTHORIZED);
        }
        else {
            return authenticated;
        }

    }


    async authenticate(request): Promise<boolean> {
        const data =
            await firstValueFrom(
                this.httpService.get
                    (this.authUrl, {
                        validateStatus: function (status) {
                            console.log(status);
                            return status == 200; // Resolve only if the status code is 200
                        }
                    })
                    .pipe(map(response => {
                        //console.log(response);
                        console.log("authenticated");
                        return true;
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
                            throw new HttpException('Could not authenticate with Score', HttpStatus.UNAUTHORIZED);
                        }),
                    )
            );
        return data;
    }
}
